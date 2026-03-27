package net.directional.recruitment.stockindex.outbound

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "stock_index")
class StockIndexEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "name_en", nullable = false)
    var nameEn: String = "",

    @Column(name = "base_date", nullable = false)
    var baseDate: LocalDate = LocalDate.now(),

    @Column(name = "base_index", nullable = false, precision = 19, scale = 4)
    var baseIndex: BigDecimal = BigDecimal.ZERO,

    @Column(name = "base_market_cap", nullable = false, precision = 19, scale = 4)
    var baseMarketCap: BigDecimal = BigDecimal.ZERO,
)