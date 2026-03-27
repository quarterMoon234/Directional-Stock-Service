package net.directional.recruitment.stockindex.app

data class StockIndexQueryCondition(
    val search: String? = null,
    val sortBy: StockIndexSortBy = StockIndexSortBy.NAME,
    val sortDirection: SortDirection = SortDirection.ASC
)