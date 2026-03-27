package net.directional.recruitment.stockindex.outbound

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("directional.price-api")
data class PriceApiProperties(
    val baseUrl: String,
    val username: String,
    val password: String,
    )