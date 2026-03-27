package net.directional.recruitment.stockindex.inbound

import net.directional.recruitment.stockindex.app.CreateStockIndexCommand
import java.math.BigDecimal

data class CreateStockIndexRequest(
    val name: String,
    val nameEn: String,
    val baseIndex: BigDecimal,
    val stockShortCodes: List<String>
) {
    fun toCommand(): CreateStockIndexCommand =
        CreateStockIndexCommand(
            name = name,
            nameEn = nameEn,
            baseIndex = baseIndex,
            stockShortCodes = stockShortCodes
        )
}