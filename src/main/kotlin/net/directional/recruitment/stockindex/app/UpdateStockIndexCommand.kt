package net.directional.recruitment.stockindex.app

data class UpdateStockIndexCommand (
    val id: Long,
    val name: String,
    val nameEn: String,
    val stockShortCodes: List<String>
)