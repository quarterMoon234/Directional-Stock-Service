package net.directional.recruitment.stockindex.app

import java.math.BigDecimal
import java.time.LocalDate

data class StockIndexSummary (
    val id: Long,
    val name: String,
    val nameEn: String,
    val baseDate: LocalDate,
    val baseIndex: BigDecimal,
    val constituentCount: Int
)