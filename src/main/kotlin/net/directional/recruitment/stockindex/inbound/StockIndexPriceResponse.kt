package net.directional.recruitment.stockindex.inbound

import java.math.BigDecimal

data class StockIndexPriceResponse(
    val stockIndexId: Long,
    val name: String,
    val closePrice: BigDecimal,
    val changeAmount: BigDecimal,
    val changeRate: BigDecimal,
    val openPrice: BigDecimal,
    val highPrice: BigDecimal,
    val lowPrice: BigDecimal,
)