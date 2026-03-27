package net.directional.recruitment.stockindex.inbound

import net.directional.recruitment.stockindex.app.UpdateStockIndexCommand

data class UpdateStockIndexRequest (
    val name: String,
    val nameEn: String,
    val stockShortCodes: List<String>
) {
    fun toCommand(id: Long): UpdateStockIndexCommand =
        UpdateStockIndexCommand(
            id = id,
            name = name,
            nameEn = nameEn,
            stockShortCodes = stockShortCodes
        )
}