package net.directional.recruitment.stock.outbound

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "stock")
class StockEntity(
    @Id
    @Column(name = "short_code", nullable = false, length = 6)
    var shortCode: String = "",

    @Column(name = "standard_code", nullable = false, unique = true, length = 20)
    var standardCode: String = "",

    @Column(name = "name_kr", nullable = false)
    var nameKr: String = "",

    @Column(name = "name_kr_short", nullable = false)
    var nameKrShort: String = "",

    @Column(name = "name_en", nullable = false)
    var nameEn: String = "",

    @Column(name = "listed_at", nullable = false)
    var listedAt: LocalDate = LocalDate.of(1900, 1, 1),

    @Column(name = "market_type", nullable = false)
    var marketType: String = "",

    @Column(name = "security_type", nullable = false)
    var securityType: String = "",

    @Column(name = "affiliation", nullable = false)
    var affiliation: String = "",

    @Column(name = "stock_type", nullable = false)
    var stockType: String = "",

    @Column(name = "par_value", nullable = false)
    var parValue: String = "",

    @Column(name = "listed_shares", nullable = false)
    var listedShares: Long = 0,
)
