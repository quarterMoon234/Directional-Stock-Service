package net.directional.recruitment.stockindex.outbound

import jakarta.persistence.*

@Entity
@Table(name = "stock_index_constituent")
class StockIndexConstituentEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "stock_index_id", nullable = false)
    var stockIndexId: Long = 0L,

    @Column(name = "stock_short_code", nullable = false, length = 6)
    var stockShortCode: String = "",
)