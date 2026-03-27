package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.inbound.CreateStockIndexResponse
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentEntity
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexEntity
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.LocalDate

@Service
class StockIndexCreateService (
    private val stockIndexRepository: StockIndexRepository,
    private val stockIndexConstituentRepository: StockIndexConstituentRepository,
    private val stockRepository : StockRepository
) {
    @Transactional
    fun create(command: CreateStockIndexCommand): CreateStockIndexResponse {
        val name = command.name.trim()
        val nameEn = command.nameEn.trim()
        val baseIndex = command.baseIndex
        val stockShortCodes = command.stockShortCodes.map(String::trim).filter(String::isNotEmpty).distinct()

        if (name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (nameEn.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "nameEn must not be blank")
        }
        if (baseIndex <= BigDecimal.ZERO) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "baseIndex must be greater than zero")
        }
        if (stockShortCodes.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stockShortCodes must not be empty")
        }

        val existingStockCodes = stockRepository.findAllById((stockShortCodes))
            .map { it.shortCode }
            .toSet()

        val missingStockCodes = stockShortCodes.filterNot(existingStockCodes::contains)
        if (missingStockCodes.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "stocks not found: ${missingStockCodes.joinToString(",")}",
            )
        }

        val stockIndex = stockIndexRepository.save(
            StockIndexEntity(
                name = name,
                nameEn = nameEn,
                baseDate = LocalDate.now(),
                baseIndex = command.baseIndex,
                baseMarketCap = BigDecimal.ZERO,
            ),
        )

        val savedIndexId = stockIndex.id
            ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to create stock index")

        stockIndexConstituentRepository.saveAll(
            stockShortCodes.map { stockShortCode ->
                StockIndexConstituentEntity(
                    stockIndexId = savedIndexId,
                    stockShortCode = stockShortCode,
                )
            },
        )

        return CreateStockIndexResponse(
            id = savedIndexId,
            name = stockIndex.name,
            nameEn = stockIndex.nameEn,
            baseDate = stockIndex.baseDate,
            baseIndex = stockIndex.baseIndex,
            constituentCount = stockShortCodes.size
        )
    }
}