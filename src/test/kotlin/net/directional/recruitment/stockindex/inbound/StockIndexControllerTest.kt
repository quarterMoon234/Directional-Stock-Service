package net.directional.recruitment.stockindex.inbound
import net.directional.recruitment.stock.outbound.StockEntity
import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentEntity
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexEntity
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest(
    properties = [
        "spring.datasource.url=jdbc:h2:mem:stock-index-controller-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop",
    ],
)
@AutoConfigureMockMvc
class StockIndexControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var stockRepository: StockRepository

    @Autowired
    private lateinit var stockIndexRepository: StockIndexRepository

    @Autowired
    private lateinit var stockIndexConstituentRepository: StockIndexConstituentRepository

    @BeforeEach
    fun setUp() {
        stockIndexConstituentRepository.deleteAll()
        stockIndexRepository.deleteAll()
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
    fun `create creates stock index with requested constituents`() {
        val response = mockMvc.perform(
            post("/stock-indices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(
                        "name" to "K-Tech Index",
                        "nameEn" to "K-Tech Index",
                        "baseIndex" to BigDecimal("1000.0"),
                        "stockShortCodes" to listOf("005930", "035720"),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.name").value("K-Tech Index"))
            .andExpect(jsonPath("$.nameEn").value("K-Tech Index"))
            .andExpect(jsonPath("$.constituentCount").value(2))
            .andReturn()
            .response
            .contentAsString

        val createdId = "\"id\"\\s*:\\s*(\\d+)".toRegex()
            .find(response)
            ?.groupValues
            ?.get(1)
            ?.toLong()
            ?: error("created id not found in response: $response")
        assertThat(stockIndexRepository.existsById(createdId)).isTrue()
        assertThat(stockIndexConstituentRepository.findAll().count { it.stockIndexId == createdId }).isEqualTo(2)
    }

    @Test
    fun `create returns bad request when stock codes are empty`() {
        mockMvc.perform(
            post("/stock-indices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(
                        "name" to "Invalid Index",
                        "nameEn" to "Invalid Index",
                        "baseIndex" to BigDecimal("1000.0"),
                        "stockShortCodes" to emptyList<String>(),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `create returns bad request when stock code does not exist`() {
        mockMvc.perform(
            post("/stock-indices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(
                        "name" to "Invalid Index",
                        "nameEn" to "Invalid Index",
                        "baseIndex" to BigDecimal("1000.0"),
                        "stockShortCodes" to listOf("005930", "999999"),
                    ),
                ),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `get filters stock indices by search keyword`() {
        saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Value",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )

        mockMvc.perform(
            get("/stock-indices")
                .param("search", "growth"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Alpha Index"))
    }

    @Test
    fun `blank search behaves like an unfiltered request`() {
        saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Value",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )
        saveStockIndex(
            name = "Gamma Index",
            nameEn = "Gamma Blend",
            baseDate = LocalDate.of(2026, 3, 3),
            stockShortCodes = listOf("005930", "035720", "263750"),
        )

        mockMvc.perform(
            get("/stock-indices")
                .param("search", "   "),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(3))
    }

    @Test
    fun `get returns empty array when no index matches search`() {
        saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )

        mockMvc.perform(
            get("/stock-indices")
                .param("search", "non-existent-index"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `get sorts stock indices by constituent count descending`() {
        saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        saveStockIndex(
            name = "Bravo Index",
            nameEn = "Bravo Growth",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )
        saveStockIndex(
            name = "Charlie Index",
            nameEn = "Charlie Growth",
            baseDate = LocalDate.of(2026, 3, 3),
            stockShortCodes = listOf("005930", "035720", "263750"),
        )

        mockMvc.perform(
            get("/stock-indices")
                .param("sortBy", "CONSTITUENT_COUNT")
                .param("sortDirection", "DESC"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Charlie Index"))
            .andExpect(jsonPath("$[1].name").value("Bravo Index"))
            .andExpect(jsonPath("$[2].name").value("Alpha Index"))
    }

    @Test
    fun `invalid sort enum returns bad request`() {
        mockMvc.perform(
            get("/stock-indices")
                .param("sortBy", "INVALID"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `update replaces stock index name and constituents`() {
        val stockIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )

        mockMvc.perform(
            patch("/stock-indices/{id}", stockIndexId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(
                        "name" to "Updated Index",
                        "nameEn" to "Updated Growth",
                        "stockShortCodes" to listOf("035720", "263750"),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(stockIndexId))
            .andExpect(jsonPath("$.name").value("Updated Index"))
            .andExpect(jsonPath("$.nameEn").value("Updated Growth"))
            .andExpect(jsonPath("$.constituentCount").value(2))

        val updatedStockIndex = stockIndexRepository.findById(stockIndexId).orElseThrow()
        val updatedStockCodes = stockIndexConstituentRepository.findAll()
            .filter { it.stockIndexId == stockIndexId }
            .map { it.stockShortCode }

        assertThat(updatedStockIndex.name).isEqualTo("Updated Index")
        assertThat(updatedStockIndex.nameEn).isEqualTo("Updated Growth")
        assertThat(updatedStockCodes).containsExactlyInAnyOrder("035720", "263750")
    }

    @Test
    fun `update returns not found for unknown stock index id`() {
        mockMvc.perform(
            patch("/stock-indices/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    requestBody(
                        "name" to "Updated Index",
                        "nameEn" to "Updated Growth",
                        "stockShortCodes" to listOf("005930"),
                    ),
                ),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `delete removes stock index and constituents`() {
        val stockIndexId = saveStockIndex(
            name = "Delete Target",
            nameEn = "Delete Target",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930", "035720"),
        )

        mockMvc.perform(
            delete("/stock-indices/{id}", stockIndexId),
        )
            .andExpect(status().isNoContent)

        assertThat(stockIndexRepository.existsById(stockIndexId)).isFalse()
        assertThat(stockIndexConstituentRepository.findAll().any { it.stockIndexId == stockIndexId }).isFalse()
    }

    @Test
    fun `delete returns not found for unknown stock index id`() {
        mockMvc.perform(
            delete("/stock-indices/{id}", 999999L),
        )
            .andExpect(status().isNotFound)
    }

    private fun saveStockIndex(
        name: String,
        nameEn: String,
        baseDate: LocalDate,
        stockShortCodes: List<String>,
    ): Long {
        val stockIndex = stockIndexRepository.save(
            StockIndexEntity(
                name = name,
                nameEn = nameEn,
                baseDate = baseDate,
                baseIndex = BigDecimal("1000.0"),
                baseMarketCap = BigDecimal.ZERO,
            ),
        )

        val stockIndexId = stockIndex.id ?: error("stock index id must not be null")
        stockIndexConstituentRepository.saveAll(
            stockShortCodes.map { stockShortCode ->
                StockIndexConstituentEntity(
                    stockIndexId = stockIndexId,
                    stockShortCode = stockShortCode,
                )
            },
        )

        return stockIndexId
    }

    private fun requestBody(vararg fields: Pair<String, Any?>): String =
        fields.joinToString(
            prefix = "{",
            postfix = "}",
            separator = ",",
        ) { (key, value) ->
            "\"$key\":${toJsonValue(value)}"
        }

    private fun toJsonValue(value: Any?): String =
        when (value) {
            null -> "null"
            is String -> "\"${value.replace("\"", "\\\"")}\""
            is BigDecimal -> value.toPlainString()
            is Number, is Boolean -> value.toString()
            is List<*> -> value.joinToString(prefix = "[", postfix = "]", separator = ",") { toJsonValue(it) }
            else -> error("unsupported json value: $value")
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
