package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stockindex.inbound.StockIndexPriceResponse
import net.directional.recruitment.stockindex.outbound.StockIndexPriceRepository
import org.springframework.stereotype.Service

@Service
class StockIndexPriceQueryService(
    private val stockIndexPriceRepository: StockIndexPriceRepository,
) {
    fun getStockIndexPrices(condition: StockIndexPriceQueryCondition): List<StockIndexPriceResponse> =
        stockIndexPriceRepository.findAllByCondition(condition).map { summary ->
            StockIndexPriceResponse(
                stockIndexId = summary.stockIndexId,
                name = summary.name,
                closePrice = summary.closePrice,
                changeAmount = summary.changeAmount,
                changeRate = summary.changeRate,
                openPrice = summary.openPrice,
                highPrice = summary.highPrice,
                lowPrice = summary.lowPrice,
            )
        }
}