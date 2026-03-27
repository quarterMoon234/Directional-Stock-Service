package net.directional.recruitment.stockindex.inbound

import java.math.BigDecimal
import java.time.LocalDate

data class StockIndexResponse (
    val id: Long,
    val name: String,
    val nameEn: String,
    val baseDate: LocalDate,
    val baseIndex: BigDecimal,
    val constituentCount: Int,
)