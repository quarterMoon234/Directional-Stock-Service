package net.directional.recruitment.stock.app

data class StockQueryCondition(
    val search: String? = null,
    val marketTypes: List<String> = emptyList(),
    val securityTypes: List<String> = emptyList(),
    val affiliations: List<String> = emptyList(),
    val stockTypes: List<String> = emptyList(),
    val sortBy: StockSortBy = StockSortBy.SHORT_CODE,
    val sortDirection: SortDirection = SortDirection.ASC,
)