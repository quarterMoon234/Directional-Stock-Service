package net.directional.recruitment.stock.outbound

import org.springframework.data.jpa.repository.JpaRepository

interface StockJpaRepository : JpaRepository<StockJpaEntity, String>
