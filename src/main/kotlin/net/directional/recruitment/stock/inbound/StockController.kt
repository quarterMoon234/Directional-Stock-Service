package net.directional.recruitment.stock.inbound

import net.directional.recruitment.stock.app.SortDirection
import net.directional.recruitment.stock.app.StockQueryCondition
import net.directional.recruitment.stock.app.StockQueryService
import net.directional.recruitment.stock.app.StockSortBy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stocks")
class StockController(
    private val stockQueryService: StockQueryService,
) {
    @GetMapping
    fun getStocks(
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) marketType: List<String>?,
        @RequestParam(required = false) securityType: List<String>?,
        @RequestParam(required = false) affiliation: List<String>?,
        @RequestParam(required = false) stockType: List<String>?,
        @RequestParam(defaultValue = "SHORT_CODE") sortBy: StockSortBy,
        @RequestParam(defaultValue = "ASC") sortDirection: SortDirection,
    ): List<StockResponse> =
        stockQueryService.getStocks(
            StockQueryCondition(
                search = search,
                marketTypes = marketType.orEmpty(),
                securityTypes = securityType.orEmpty(),
                affiliations = affiliation.orEmpty(),
                stockTypes = stockType.orEmpty(),
                sortBy = sortBy,
                sortDirection = sortDirection,
            ),
        ).map(StockResponse::from)
}
