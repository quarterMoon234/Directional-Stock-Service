package net.directional.recruitment.stockindex.outbound

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(PriceApiProperties::class)
class PriceApiConfig {
    @Bean
    fun priceApiRestClient(
        properties: PriceApiProperties
    ): RestClient =
        RestClient.builder()
            .baseUrl(properties.baseUrl)
            .build()
}
