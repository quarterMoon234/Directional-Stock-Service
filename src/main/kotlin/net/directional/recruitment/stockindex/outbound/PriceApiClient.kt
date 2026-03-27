package net.directional.recruitment.stockindex.outbound

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class PriceApiClient (
    private val priceApiRestClient: RestClient,
    private val properties: PriceApiProperties
) {

    fun getPrices(): List<StockPriceResponse> {
        val token = issueToken()

        return priceApiRestClient.get()
            .uri("/v1/prices")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .retrieve()
            .body(object : ParameterizedTypeReference<List<StockPriceResponse>>() {})
            ?: emptyList()
    }

    private fun issueToken(): String =
        priceApiRestClient.post()
            .uri("/auth/token")
            .body(
                IssueTokenRequest(
                    username = properties.username,
                    password = properties.password,
                ),
            )
            .retrieve()
            .body(IssueTokenResponse::class.java)
            ?.token
            ?: error("failed to issue token")
}
