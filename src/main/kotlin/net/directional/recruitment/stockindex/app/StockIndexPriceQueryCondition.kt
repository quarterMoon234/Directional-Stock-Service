package net.directional.recruitment.stockindex.app

data class StockIndexPriceQueryCondition(
    val search: String? = null,
    val sortBy: StockIndexPriceSortBy = StockIndexPriceSortBy.NAME,
    val sortDirection: SortDirection = SortDirection.ASC,
)