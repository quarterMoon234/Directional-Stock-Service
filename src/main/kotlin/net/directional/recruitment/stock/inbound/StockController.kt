package net.directional.recruitment.stock.inbound

import net.directional.recruitment.stock.app.StockQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/stocks")
class StockController(
    private val stockQueryService: StockQueryService,
) {
    @GetMapping
    fun getStocks(): List<StockResponse> =
        stockQueryService.getStocks().map(StockResponse::from)
}
