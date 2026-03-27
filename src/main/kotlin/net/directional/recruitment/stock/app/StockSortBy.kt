package net.directional.recruitment.stock.app

enum class StockSortBy (
    val property: String
){
    SHORT_CODE("shortCode"),
    NAME_KR_SHORT("nameKrShort"),
    LISTED_AT("listedAt"),
    LISTED_SHARES("listedShares")
}