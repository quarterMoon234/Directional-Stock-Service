package net.directional.recruitment.stockindex.inbound

import net.directional.recruitment.stockindex.app.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/stock-indices")
class StockIndexController(
    private val stockIndexCreateService: StockIndexCreateService,
    private val stockIndexQueryService: StockIndexQueryService,
    private val stockIndexUpdateService: StockIndexUpdateService,
    private val stockIndexDeleteService: StockIndexDeleteService,
    private val stockIndexPriceQueryService: StockIndexPriceQueryService,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createStockIndex(
        @RequestBody request: CreateStockIndexRequest
    ): CreateStockIndexResponse =
        stockIndexCreateService.create(request.toCommand())

    @GetMapping
    fun getStockIndices(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "NAME") sortBy: StockIndexSortBy,
        @RequestParam(defaultValue = "ASC") sortDirection: SortDirection
    ): List<StockIndexResponse> =
        stockIndexQueryService.getStockIndices(
            StockIndexQueryCondition(
                search = search,
                sortBy = sortBy,
                sortDirection = sortDirection
            ),
        )

    @PatchMapping("/{id}")
    fun updateStockIndex(
        @PathVariable id: Long,
        @RequestBody request: UpdateStockIndexRequest
    ): StockIndexResponse =
        stockIndexUpdateService.update(request.toCommand(id))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStockIndex(
        @PathVariable id: Long,
    ) {
        stockIndexDeleteService.delete(id)
    }

    @GetMapping("/prices")
    fun getStockIndexPrices(
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "NAME") sortBy: StockIndexPriceSortBy,
        @RequestParam(defaultValue = "ASC") sortDirection: SortDirection,
    ): List<StockIndexPriceResponse> =
        stockIndexPriceQueryService.getStockIndexPrices(
            StockIndexPriceQueryCondition(
                search = search,
                sortBy = sortBy,
                sortDirection = sortDirection,
            ),
        )
}