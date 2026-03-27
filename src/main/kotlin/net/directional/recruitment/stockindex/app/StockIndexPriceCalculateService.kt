package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.outbound.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@Service
class StockIndexPriceCalculateService(
    private val stockIndexRepository: StockIndexRepository,
    private val stockIndexConstituentRepository: StockIndexConstituentRepository,
    private val stockIndexPriceRepository: StockIndexPriceRepository,
    private val stockRepository: StockRepository,
    private val priceApiClient: PriceApiClient,
) {
    @Transactional
    fun calculateAll() {
        val pricesByTicker = priceApiClient.getPrices().associateBy { it.ticker }
        val stockIndices = stockIndexRepository.findAll()

        stockIndices.forEach { stockIndex ->
            val stockIndexId = stockIndex.id ?: return@forEach
            val constituents = stockIndexConstituentRepository.findAllByStockIndexId(stockIndexId)
            val stocks = stockRepository.findAllById(constituents.map { it.stockShortCode })

            if (stocks.isEmpty() || stockIndex.baseMarketCap <= BigDecimal.ZERO) {
                return@forEach
            }

            val openMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
                val price = pricesByTicker[stock.shortCode] ?: return@fold acc
                acc + BigDecimal.valueOf(price.open) * BigDecimal.valueOf(stock.listedShares)
            }

            val highMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
                val price = pricesByTicker[stock.shortCode] ?: return@fold acc
                acc + BigDecimal.valueOf(price.high) * BigDecimal.valueOf(stock.listedShares)
            }

            val lowMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
                val price = pricesByTicker[stock.shortCode] ?: return@fold acc
                acc + BigDecimal.valueOf(price.low) * BigDecimal.valueOf(stock.listedShares)
            }
            val closeMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
                val price = pricesByTicker[stock.shortCode] ?: return@fold acc
                acc + BigDecimal.valueOf(price.close) * BigDecimal.valueOf(stock.listedShares)
            }

            val previousCloseMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
                val price = pricesByTicker[stock.shortCode] ?: return@fold acc
                val previousClose = BigDecimal.valueOf(price.close - price.change)
                acc + previousClose * BigDecimal.valueOf(stock.listedShares)
            }

            val openPrice = marketCapToIndex(openMarketCap, stockIndex.baseMarketCap, stockIndex.baseIndex)
            val highPrice = marketCapToIndex(highMarketCap, stockIndex.baseMarketCap, stockIndex.baseIndex)
            val lowPrice = marketCapToIndex(lowMarketCap, stockIndex.baseMarketCap, stockIndex.baseIndex)
            val closePrice = marketCapToIndex(closeMarketCap, stockIndex.baseMarketCap, stockIndex.baseIndex)
            val previousClosePrice =
                marketCapToIndex(previousCloseMarketCap, stockIndex.baseMarketCap, stockIndex.baseIndex)

            val changeAmount = closePrice.subtract(previousClosePrice)
            val changeRate =
                if (previousClosePrice.compareTo(BigDecimal.ZERO) == 0) {
                    BigDecimal.ZERO
                } else {
                    changeAmount
                        .divide(previousClosePrice, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal("100"))
                        .setScale(4, RoundingMode.HALF_UP)
                }

            stockIndexPriceRepository.save(
                StockIndexPriceEntity(
                    stockIndexId = stockIndexId,
                    openPrice = openPrice,
                    highPrice = highPrice,
                    lowPrice = lowPrice,
                    closePrice = closePrice,
                    changeAmount = changeAmount.setScale(4, RoundingMode.HALF_UP),
                    changeRate = changeRate,
                    updatedAt = LocalDateTime.now(),
                ),
            )
        }
    }

    private fun marketCapToIndex(
        currentMarketCap: BigDecimal,
        baseMarketCap: BigDecimal,
        baseIndex: BigDecimal,
    ): BigDecimal =
        currentMarketCap
            .divide(baseMarketCap, 8, RoundingMode.HALF_UP)
            .multiply(baseIndex)
            .setScale(4, RoundingMode.HALF_UP)
}
