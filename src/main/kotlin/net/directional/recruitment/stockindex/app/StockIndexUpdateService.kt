package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stock.outbound.StockRepository
import net.directional.recruitment.stockindex.inbound.StockIndexResponse
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentEntity
import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StockIndexUpdateService (
    private val stockIndexRepository: StockIndexRepository,
    private val stockIndexContinuationRepository: StockIndexConstituentRepository,
    private val stockRepository: StockRepository
) {
    @Transactional
    fun update(command: UpdateStockIndexCommand): StockIndexResponse {

        val name = command.name.trim()
        val nameEn = command.nameEn.trim()
        val stockShortCodes = command.stockShortCodes
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()

        if (name.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank")
        }
        if (nameEn.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "nameEn must not be blank")
        }
        if (stockShortCodes.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "stockShortCodes must not be empty")
        }

        val existingStockCodes = stockRepository.findAllById(stockShortCodes)
            .map { it.shortCode }
            .toSet()

        val missingStockCodes = stockShortCodes.filterNot(existingStockCodes::contains)
        if (missingStockCodes.isNotEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "stocks not found: ${missingStockCodes.joinToString(",")}",
            )
        }

        val stockIndex = stockIndexRepository.findById(command.id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "stock index not found")
            }

        stockIndex.name = name;
        stockIndex.nameEn = nameEn;

        stockIndexContinuationRepository.deleteByStockIndexId(command.id)
        stockIndexContinuationRepository.saveAll(
            stockShortCodes.map { stockShortCodes ->
                StockIndexConstituentEntity(
                    stockIndexId = command.id,
                    stockShortCode = stockShortCodes,
                )
            }
        )

        return StockIndexResponse(
            id = stockIndex.id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR),
            name = stockIndex.name,
            nameEn = stockIndex.nameEn,
            baseDate = stockIndex.baseDate,
            baseIndex = stockIndex.baseIndex,
            constituentCount = stockShortCodes.size
        )
    }
}