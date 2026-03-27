package net.directional.recruitment.stockindex.outbound

import net.directional.recruitment.stockindex.app.StockIndexQueryCondition
import net.directional.recruitment.stockindex.app.StockIndexSummary

interface StockIndexQueryRepository {
    fun findAllByCondition(condition: StockIndexQueryCondition): List<StockIndexSummary>
}