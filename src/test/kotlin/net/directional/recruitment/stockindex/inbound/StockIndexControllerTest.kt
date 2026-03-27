package net.directional.recruitment.stockindex.inbound
import net.directional.recruitment.stock.outbound.StockEntity
import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentEntity
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexEntity
import net.directional.recruitment.stockindex.outbound.PriceApiClient
import net.directional.recruitment.stockindex.outbound.StockIndexPriceEntity
import net.directional.recruitment.stockindex.outbound.StockIndexPriceRepository
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import net.directional.recruitment.stockindex.outbound.StockPriceResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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

    @Autowired
    private lateinit var stockIndexPriceRepository: StockIndexPriceRepository

    @MockitoBean
    private lateinit var priceApiClient: PriceApiClient

    @BeforeEach
    fun setUp() {
        stockIndexPriceRepository.deleteAll()
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

        given(priceApiClient.getPrices()).willReturn(
            listOf(
                stockPrice(ticker = "005930", open = 1000, high = 1010, low = 990, close = 1005, change = 5),
                stockPrice(ticker = "035720", open = 2000, high = 2020, low = 1980, close = 2010, change = 10),
                stockPrice(ticker = "263750", open = 3000, high = 3030, low = 2970, close = 2990, change = -10),
            ),
        )
    }

    // 유효한 종목 코드로 지수를 생성하면 지수와 구성종목이 함께 저장되어야 한다.
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

    // 구성종목이 비어 있으면 지수 생성 요청을 400으로 거부해야 한다.
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

    // 존재하지 않는 종목 코드가 포함되면 지수 생성 요청을 400으로 거부해야 한다.
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

    // 지수명 또는 영문지수명에 검색어가 포함된 지수만 조회되어야 한다.
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

    // 공백 검색어는 검색 조건이 없는 지수 목록 조회처럼 동작해야 한다.
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

    // 검색 결과가 없으면 빈 배열을 반환해야 한다.
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

    // 구성종목수 정렬 요청 시 지수 목록을 내림차순으로 반환해야 한다.
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

    // 허용되지 않은 지수 정렬 enum 값은 400으로 거부해야 한다.
    @Test
    fun `invalid sort enum returns bad request`() {
        mockMvc.perform(
            get("/stock-indices")
                .param("sortBy", "INVALID"),
        )
            .andExpect(status().isBadRequest)
    }

    // 지수 시세 조회는 기본적으로 지수명 오름차순으로 정렬되어야 한다.
    @Test
    fun `get prices returns stock index prices sorted by name by default`() {
        val betaIndexId = saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Growth",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )

        saveStockIndexPrice(
            stockIndexId = betaIndexId,
            closePrice = BigDecimal("1010.0000"),
            changeAmount = BigDecimal("10.0000"),
            changeRate = BigDecimal("1.0000"),
            openPrice = BigDecimal("1000.0000"),
            highPrice = BigDecimal("1015.0000"),
            lowPrice = BigDecimal("995.0000"),
        )
        saveStockIndexPrice(
            stockIndexId = alphaIndexId,
            closePrice = BigDecimal("1005.0000"),
            changeAmount = BigDecimal("5.0000"),
            changeRate = BigDecimal("0.5000"),
            openPrice = BigDecimal("1000.0000"),
            highPrice = BigDecimal("1008.0000"),
            lowPrice = BigDecimal("998.0000"),
        )

        mockMvc.perform(get("/stock-indices/prices"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Alpha Index"))
            .andExpect(jsonPath("$[1].name").value("Beta Index"))
    }

    // 지수 시세 조회도 지수명과 영문지수명에 대해 부분 검색이 가능해야 한다.
    @Test
    fun `get prices filters by keyword on name and english name`() {
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        val betaIndexId = saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Value",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )

        saveStockIndexPrice(stockIndexId = alphaIndexId)
        saveStockIndexPrice(stockIndexId = betaIndexId)

        mockMvc.perform(
            get("/stock-indices/prices")
                .param("search", "growth"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Alpha Index"))
    }

    // 지수 시세 조회에서 공백 검색어는 전체 조회처럼 동작해야 한다.
    @Test
    fun `blank price search behaves like an unfiltered request`() {
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        val betaIndexId = saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Value",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )

        saveStockIndexPrice(stockIndexId = alphaIndexId)
        saveStockIndexPrice(stockIndexId = betaIndexId)

        mockMvc.perform(
            get("/stock-indices/prices")
                .param("search", "   "),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    // 지수 시세 검색 결과가 없으면 빈 배열을 반환해야 한다.
    @Test
    fun `get prices returns empty array when no price summary matches search`() {
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )

        saveStockIndexPrice(stockIndexId = alphaIndexId)

        mockMvc.perform(
            get("/stock-indices/prices")
                .param("search", "non-existent-price-index"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    // 지수 시세는 전일대비 등락률 기준으로 정렬할 수 있어야 한다.
    @Test
    fun `get prices sorts by change rate descending`() {
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        val betaIndexId = saveStockIndex(
            name = "Beta Index",
            nameEn = "Beta Value",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )
        val gammaIndexId = saveStockIndex(
            name = "Gamma Index",
            nameEn = "Gamma Blend",
            baseDate = LocalDate.of(2026, 3, 3),
            stockShortCodes = listOf("005930", "035720", "263750"),
        )

        saveStockIndexPrice(stockIndexId = alphaIndexId, changeRate = BigDecimal("0.5000"))
        saveStockIndexPrice(stockIndexId = betaIndexId, changeRate = BigDecimal("1.2500"))
        saveStockIndexPrice(stockIndexId = gammaIndexId, changeRate = BigDecimal("-0.1000"))

        mockMvc.perform(
            get("/stock-indices/prices")
                .param("sortBy", "CHANGE_RATE")
                .param("sortDirection", "DESC"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Beta Index"))
            .andExpect(jsonPath("$[1].name").value("Alpha Index"))
            .andExpect(jsonPath("$[2].name").value("Gamma Index"))
    }

    // 아직 계산된 시세가 없는 지수는 시세 조회 결과에서 제외되어야 한다.
    @Test
    fun `get prices excludes stock indices without calculated price`() {
        val alphaIndexId = saveStockIndex(
            name = "Alpha Index",
            nameEn = "Alpha Growth",
            baseDate = LocalDate.of(2026, 3, 1),
            stockShortCodes = listOf("005930"),
        )
        saveStockIndex(
            name = "No Price Index",
            nameEn = "No Price Index",
            baseDate = LocalDate.of(2026, 3, 2),
            stockShortCodes = listOf("005930", "035720"),
        )

        saveStockIndexPrice(stockIndexId = alphaIndexId)

        mockMvc.perform(get("/stock-indices/prices"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Alpha Index"))
    }

    // 허용되지 않은 지수 시세 정렬 enum 값은 400으로 거부해야 한다.
    @Test
    fun `invalid price sort enum returns bad request`() {
        mockMvc.perform(
            get("/stock-indices/prices")
                .param("sortBy", "INVALID"),
        )
            .andExpect(status().isBadRequest)
    }

    // 지수 수정 시 이름과 구성종목이 새 요청 값으로 완전히 교체되어야 한다.
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

    // 존재하지 않는 지수 ID 수정 요청은 404를 반환해야 한다.
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

    // 지수 삭제 시 지수 본체와 구성종목 관계가 함께 제거되어야 한다.
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

    // 존재하지 않는 지수 ID 삭제 요청은 404를 반환해야 한다.
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

    private fun saveStockIndexPrice(
        stockIndexId: Long,
        closePrice: BigDecimal = BigDecimal("1000.0000"),
        changeAmount: BigDecimal = BigDecimal("10.0000"),
        changeRate: BigDecimal = BigDecimal("1.0000"),
        openPrice: BigDecimal = BigDecimal("990.0000"),
        highPrice: BigDecimal = BigDecimal("1010.0000"),
        lowPrice: BigDecimal = BigDecimal("980.0000"),
    ) {
        stockIndexPriceRepository.save(
            StockIndexPriceEntity(
                stockIndexId = stockIndexId,
                closePrice = closePrice,
                changeAmount = changeAmount,
                changeRate = changeRate,
                openPrice = openPrice,
                highPrice = highPrice,
                lowPrice = lowPrice,
                updatedAt = LocalDateTime.of(2026, 3, 27, 12, 0),
            ),
        )
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

    private fun stockPrice(
        ticker: String,
        open: Long,
        high: Long,
        low: Long,
        close: Long,
        change: Long,
    ): StockPriceResponse =
        StockPriceResponse(
            ticker = ticker,
            open = open,
            high = high,
            low = low,
            close = close,
            change = change,
        )
}
