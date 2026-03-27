package net.directional.recruitment.stock.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface StockRepository :
    JpaRepository<StockEntity, String>,
    StockQueryRepository
