package net.directional.recruitment.stock.inbound

import net.directional.recruitment.stock.outbound.StockEntity
import net.directional.recruitment.stock.outbound.StockRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:stock-controller-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
@AutoConfigureMockMvc
class StockControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var stockRepository: StockRepository

    @BeforeEach
    fun setUp() {
        stockRepository.deleteAll()
        stockRepository.saveAll(
            listOf(
                stock(
                    shortCode = "005930",
                    standardCode = "KR7005930003",
                    nameKr = "삼성전자보통주",
                    nameKrShort = "삼성전자",
                    nameEn = "Samsung Electronics",
                    listedAt = LocalDate.of(1975, 6, 11),
                    marketType = "KOSPI",
                    securityType = "주권",
                    affiliation = "",
                    stockType = "보통주",
                    parValue = "100",
                    listedShares = 5969782550,
                ),
                stock(
                    shortCode = "035720",
                    standardCode = "KR7035720002",
                    nameKr = "카카오보통주",
                    nameKrShort = "카카오",
                    nameEn = "Kakao Corp",
                    listedAt = LocalDate.of(1999, 11, 23),
                    marketType = "KOSPI",
                    securityType = "주권",
                    affiliation = "",
                    stockType = "보통주",
                    parValue = "100",
                    listedShares = 445157427,
                ),
                stock(
                    shortCode = "263750",
                    standardCode = "KR7263750002",
                    nameKr = "펄어비스보통주",
                    nameKrShort = "펄어비스",
                    nameEn = "Pearl Abyss",
                    listedAt = LocalDate.of(2017, 9, 14),
                    marketType = "KOSDAQ",
                    securityType = "주권",
                    affiliation = "우량기업부",
                    stockType = "보통주",
                    parValue = "100",
                    listedShares = 64238571,
                ),
            ),
        )
    }

    @Test
    fun `search returns matching stocks by keyword`() {
        mockMvc.perform(
            get("/stocks")
                .param("search", "Samsung"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].shortCode").value("005930"))
            .andExpect(jsonPath("$[0].nameKrShort").value("삼성전자"))
    }

    @Test
    fun `blank search behaves like an unfiltered request`() {
        mockMvc.perform(
            get("/stocks")
                .param("search", "   "),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `search returns empty array when no stock matches`() {
        mockMvc.perform(
            get("/stocks")
                .param("search", "non-existent-stock"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `sort returns stocks in requested order`() {
        mockMvc.perform(
            get("/stocks")
                .param("sortBy", "LISTED_SHARES")
                .param("sortDirection", "DESC"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].shortCode").value("005930"))
            .andExpect(jsonPath("$[1].shortCode").value("035720"))
            .andExpect(jsonPath("$[2].shortCode").value("263750"))
    }

    @Test
    fun `invalid sort enum returns bad request`() {
        mockMvc.perform(
            get("/stocks")
                .param("sortBy", "INVALID"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `filter returns stocks matching all conditions`() {
        mockMvc.perform(
            get("/stocks")
                .param("marketType", "KOSDAQ")
                .param("securityType", "주권")
                .param("affiliation", "우량기업부")
                .param("stockType", "보통주"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].shortCode").value("263750"))
            .andExpect(jsonPath("$[0].marketType").value("KOSDAQ"))
    }

    @Test
    fun `multiple market types are combined with OR semantics`() {
        mockMvc.perform(
            get("/stocks")
                .param("marketType", "KOSPI", "KOSDAQ"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    private fun stock(
        shortCode: String,
        standardCode: String,
        nameKr: String,
        nameKrShort: String,
        nameEn: String,
        listedAt: LocalDate,
        marketType: String,
        securityType: String,
        affiliation: String,
        stockType: String,
        parValue: String,
        listedShares: Long,
    ): StockEntity =
        StockEntity(
            shortCode = shortCode,
            standardCode = standardCode,
            nameKr = nameKr,
            nameKrShort = nameKrShort,
            nameEn = nameEn,
            listedAt = listedAt,
            marketType = marketType,
            securityType = securityType,
            affiliation = affiliation,
            stockType = stockType,
            parValue = parValue,
            listedShares = listedShares,
        )
}
