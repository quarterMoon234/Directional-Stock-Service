package net.directional.recruitment.stockindex.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface StockIndexRepository :
    JpaRepository<StockIndexEntity, Long>,
    StockIndexQueryRepository

