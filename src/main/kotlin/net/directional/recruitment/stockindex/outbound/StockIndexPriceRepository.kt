package net.directional.recruitment.stockindex.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface StockIndexPriceRepository :
    JpaRepository<StockIndexPriceEntity, Long>,
    StockIndexPriceQueryRepository