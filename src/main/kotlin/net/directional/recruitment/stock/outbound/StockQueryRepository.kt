package net.directional.recruitment.stock.outbound

import net.directional.recruitment.stock.app.StockQueryCondition

interface StockQueryRepository {
    fun findAllByCondition(condition: StockQueryCondition): List<StockEntity>
}