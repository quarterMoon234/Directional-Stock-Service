package net.directional.recruitment.stockindex.outbound

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "stock_index_price")
class StockIndexPriceEntity(

    @Id
    @Column(name = "stock_index_id", nullable = false)
    var stockIndexId: Long = 0L,

    @Column(name = "open_price", nullable = false, precision = 19, scale = 4)
    var openPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "high_price", nullable = false, precision = 19, scale = 4)
    var highPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "low_price", nullable = false, precision = 19, scale = 4)
    var lowPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "close_price", nullable = false, precision = 19, scale = 4)
    var closePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "change_amount", nullable = false, precision = 19, scale = 4)
    var changeAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "change_rate", nullable = false, precision = 19, scale = 4)
    var changeRate: BigDecimal = BigDecimal.ZERO,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)