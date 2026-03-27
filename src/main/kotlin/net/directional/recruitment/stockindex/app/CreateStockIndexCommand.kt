package net.directional.recruitment.stockindex.app

import java.math.BigDecimal

data class CreateStockIndexCommand(
    val name: String,
    val nameEn: String,
    val baseIndex: BigDecimal,
    val stockShortCodes: List<String>
)