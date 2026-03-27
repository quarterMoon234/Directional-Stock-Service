package net.directional.recruitment.stockindex.outbound

import net.directional.recruitment.stockindex.app.StockIndexPriceQueryCondition
import net.directional.recruitment.stockindex.app.StockIndexPriceSummary

interface StockIndexPriceQueryRepository {
    fun findAllByCondition(condition: StockIndexPriceQueryCondition): List<StockIndexPriceSummary>
}