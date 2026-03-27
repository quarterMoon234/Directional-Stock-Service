package net.directional.recruitment.stockindex.app

import net.directional.recruitment.stockindex.outbound.StockIndexConstituentRepository
import net.directional.recruitment.stockindex.outbound.StockIndexRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class StockIndexDeleteService (
    private val stockIndexRepository: StockIndexRepository,
    private val stockIndexConstituentRepository: StockIndexConstituentRepository
) {
    @Transactional
    fun delete(id: Long) {
        if (!stockIndexRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "stock index not found")
        }
        stockIndexConstituentRepository.deleteByStockIndexId(id)
        stockIndexRepository.deleteById(id)
    }
}