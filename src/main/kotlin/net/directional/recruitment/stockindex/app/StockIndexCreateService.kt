package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.inbound.CreateStockIndexResponse
import net.directional.recruitment.stockindex.outbound.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

@Service
class StockIndexCreateService (
    private val stockIndexRepository: StockIndexRepository,
    private val stockIndexConstituentRepository: StockIndexConstituentRepository,
    private val stockRepository : StockRepository,
    private val priceApiClient: PriceApiClient
) {
    @Transactional
    fun create(command: CreateStockIndexCommand): CreateStockIndexResponse {
        val name = command.name.trim()
        val nameEn = command.nameEn.trim()
        val baseIndex = command.baseIndex
        val stockShortCodes = command.stockShortCodes.map(String::trim).filter(String::isNotEmpty).distinct()

        if (name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (nameEn.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "nameEn must not be blank")
        }
        if (baseIndex <= BigDecimal.ZERO) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "baseIndex must be greater than zero")
        }
        if (stockShortCodes.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stockShortCodes must not be empty")
        }

        val stocks = stockRepository.findAllById(stockShortCodes)
        val existingStockCodes = stocks.map { it.shortCode }.toSet()

        val missingStockCodes = stockShortCodes.filterNot(existingStockCodes::contains)
        if (missingStockCodes.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "stocks not found: ${missingStockCodes.joinToString(",")}",
            )
        }

        val pricesByTicker = priceApiClient.getPrices()
            .associateBy { it.ticker }

        val missingPrices = stockShortCodes.filterNot(pricesByTicker::containsKey)
        if (missingPrices.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "prices not found: ${missingPrices.joinToString(",")}",
            )
        }

        val baseMarketCap = stocks.fold(BigDecimal.ZERO) { acc, stock ->
            val price = pricesByTicker[stock.shortCode]
                ?: throw ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "price not found: ${stock.shortCode}",
                )

            acc + BigDecimal.valueOf(price.open) * BigDecimal.valueOf(stock.listedShares)
        }

        val stockIndex = stockIndexRepository.save(
            StockIndexEntity(
                name = name,
                nameEn = nameEn,
                baseDate = LocalDate.now(),
                baseIndex = command.baseIndex,
                baseMarketCap = baseMarketCap,
            ),
        )

        val savedIndexId = stockIndex.id
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to create stock index")

        stockIndexConstituentRepository.saveAll(
            stockShortCodes.map { stockShortCode ->
                StockIndexConstituentEntity(
                    stockIndexId = savedIndexId,
                    stockShortCode = stockShortCode,
                )
            },
        )

        return CreateStockIndexResponse(
            id = savedIndexId,
            name = stockIndex.name,
            nameEn = stockIndex.nameEn,
            baseDate = stockIndex.baseDate,
            baseIndex = stockIndex.baseIndex,
            constituentCount = stockShortCodes.size
        )
    }
}