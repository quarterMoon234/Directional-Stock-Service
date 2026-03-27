package net.directional.recruitment.stockindex.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface StockIndexConstituentRepository : JpaRepository<StockIndexConstituentEntity, Long> {
    fun countByStockIndexId(stockIndexId: Long): Long
    fun deleteByStockIndexId(stockIndexId: Long)
    fun findAllByStockIndexIdIn(stockIndexIds: List<Long>): List<StockIndexConstituentEntity>
}