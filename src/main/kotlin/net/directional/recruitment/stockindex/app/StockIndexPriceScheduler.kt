package net.directional.recruitment.stockindex.app

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class StockIndexPriceScheduler (
    private val stockIndexPriceCalculateService: StockIndexPriceCalculateService
) {
    @Scheduled(fixedRate = 15 * 60 * 1000)
    fun calculatePrices() {
        stockIndexPriceCalculateService.calculateAll()
    }
}
