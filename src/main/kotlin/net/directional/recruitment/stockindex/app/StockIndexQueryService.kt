package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stockindex.inbound.StockIndexResponse
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import org.springframework.stereotype.Service

@Service
class StockIndexQueryService(
    private val stockIndexRepository: StockIndexRepository,
) {
    fun getStockIndices(condition: StockIndexQueryCondition): List<StockIndexResponse> =
        stockIndexRepository.findAllByCondition(condition).map { summary ->
            StockIndexResponse(
                id = summary.id,
                name = summary.name,
                nameEn = summary.nameEn,
                baseDate = summary.baseDate,
                baseIndex = summary.baseIndex,
                constituentCount = summary.constituentCount,
            )
        }
}
