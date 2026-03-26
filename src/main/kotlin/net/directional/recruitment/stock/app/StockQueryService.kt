package net.directional.recruitment.stock.app

import net.directional.recruitment.stock.domain.Stock
import net.directional.recruitment.stock.outbound.StockJpaEntity
import net.directional.recruitment.stock.outbound.StockJpaRepository
import org.springframework.stereotype.Service

@Service
class StockQueryService (
    private val stockJpaRepository: StockJpaRepository,
) {
    fun getStocks(): List<Stock>
    = stockJpaRepository.findAll().map { it.toDomain() }

    private fun StockJpaEntity.toDomain(): Stock =
        Stock(
            shortCode = shortCode,
            standardCode = standardCode,
            nameKr = nameKr,
            nameKrShort = nameKrShort,
            nameEn = nameEn,
            listedAt = listedAt,
            marketType = marketType,
            securityType = securityType,
            affiliation = affiliation,
            stockType = stockType,
            parValue = parValue,
            listedShares = listedShares,
        )
}
