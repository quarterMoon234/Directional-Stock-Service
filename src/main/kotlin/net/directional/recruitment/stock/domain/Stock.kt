package net.directional.recruitment.stock.domain

import java.time.LocalDate

class Stock (
    val shortCode: String,
    val standardCode: String,
    val nameKr: String,
    val nameKrShort: String,
    val nameEn: String,
    val listedAt: LocalDate,
    val marketType: String,
    val securityType: String,
    val affiliation: String,
    val stockType: String,
    val parValue: String,
    val listedShares: Long,
)
