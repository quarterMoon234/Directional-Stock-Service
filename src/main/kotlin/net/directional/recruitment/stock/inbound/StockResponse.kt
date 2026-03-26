package net.directional.recruitment.stock.inbound

import net.directional.recruitment.stock.domain.Stock
import java.time.LocalDate

data class StockResponse(
    val shortCode: String,
    val standardCode: String,
    val nameKr: String,
    val nameKrShort: String,
    val nameEn: String,
    val listedAt: LocalDate,
    val marketType: String,
    val securityType: String,
    val affiliation: String,
    val stockType: String,
    val parValue: String,
    val listedShares: Long,
) {
    companion object {
        fun from(stock: Stock): StockResponse =
            StockResponse(
                shortCode = stock.shortCode,
                standardCode = stock.standardCode,
                nameKr = stock.nameKr,
                nameKrShort = stock.nameKrShort,
                nameEn = stock.nameEn,
                listedAt = stock.listedAt,
                marketType = stock.marketType,
                securityType = stock.securityType,
                affiliation = stock.affiliation,
                stockType = stock.stockType,
                parValue = stock.parValue,
                listedShares = stock.listedShares,
            )
    }
}
