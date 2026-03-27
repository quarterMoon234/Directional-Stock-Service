package net.directional.recruitment.stockindex.outbound

data class StockPriceResponse(
    val ticker: String,
    val open: Long,
    val high: Long,
    val low: Long,
    val close: Long,
    val change: Long,
)