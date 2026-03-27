package net.directional.recruitment.stockindex.app

import java.math.BigDecimal

data class StockIndexPriceSummary(
    val stockIndexId: Long,
    val name: String,
    val nameEn: String,
    val closePrice: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal,
    val openPrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
)