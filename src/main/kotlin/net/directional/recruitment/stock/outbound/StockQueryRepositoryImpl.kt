package net.directional.recruitment.stock.outbound

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import net.directional.recruitment.stock.app.SortDirection
import net.directional.recruitment.stock.app.StockQueryCondition
import net.directional.recruitment.stock.app.StockSortBy
import org.springframework.stereotype.Repository

@Repository
class StockQueryRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory,
) : StockQueryRepository {

    override fun findAllByCondition(condition: StockQueryCondition): List<StockEntity> {
        val stock = QStockEntity.stockEntity
        val builder = BooleanBuilder()

        condition.search?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { keyword ->
                builder.and(
                    stock.shortCode.containsIgnoreCase(keyword)
                        .or(stock.nameKr.containsIgnoreCase(keyword))
                        .or(stock.nameKrShort.containsIgnoreCase(keyword))
                        .or(stock.nameEn.containsIgnoreCase(keyword))
                )
            }

        if (condition.marketTypes.isNotEmpty()) {
            builder.and(stock.marketType.`in`(condition.marketTypes))
        }
        if (condition.securityTypes.isNotEmpty()) {
            builder.and(stock.securityType.`in`(condition.securityTypes))
        }
        if (condition.affiliations.isNotEmpty()) {
            builder.and(stock.affiliation.`in`(condition.affiliations))
        }
        if (condition.stockTypes.isNotEmpty()) {
            builder.and(stock.stockType.`in`(condition.stockTypes))
        }

        return jpaQueryFactory
            .selectFrom(stock)
            .where(builder)
            .orderBy(condition.toOrderSpecifier(stock))
            .fetch()
    }

    private fun StockQueryCondition.toOrderSpecifier(
        stock: QStockEntity,
    ): OrderSpecifier<*> {
        val order =
            when (sortDirection) {
                SortDirection.ASC -> Order.ASC
                SortDirection.DESC -> Order.DESC
            }

        return when (sortBy) {
            StockSortBy.SHORT_CODE -> OrderSpecifier(order, stock.shortCode)
            StockSortBy.NAME_KR_SHORT -> OrderSpecifier(order, stock.nameKrShort)
            StockSortBy.LISTED_AT -> OrderSpecifier(order, stock.listedAt)
            StockSortBy.LISTED_SHARES -> OrderSpecifier(order, stock.listedShares)
        }
    }
}
