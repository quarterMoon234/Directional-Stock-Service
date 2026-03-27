package net.directional.recruitment.stock.app

import net.directional.recruitment.stock.outbound.StockEntity
import net.directional.recruitment.stock.outbound.StockRepository
import org.springframework.stereotype.Service

@Service
class StockQueryService(
    private val stockRepository: StockRepository,
) {
    fun getStocks(condition: StockQueryCondition): List<StockEntity> =
        stockRepository.findAllByCondition(condition)
}
