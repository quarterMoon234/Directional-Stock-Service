package net.directional.recruitment.stockindex.outbound

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import net.directional.recruitment.stockindex.app.SortDirection
import net.directional.recruitment.stockindex.app.StockIndexPriceQueryCondition
import net.directional.recruitment.stockindex.app.StockIndexPriceSortBy
import net.directional.recruitment.stockindex.app.StockIndexPriceSummary
import org.springframework.stereotype.Repository

@Repository
class StockIndexPriceQueryRepositoryImpl (
    private val jpaQueryFactory: JPAQueryFactory
) : StockIndexPriceQueryRepository {

    override fun findAllByCondition(condition: StockIndexPriceQueryCondition): List<StockIndexPriceSummary> {
        val stockIndex = QStockIndexEntity.stockIndexEntity
        val stockIndexPrice = QStockIndexPriceEntity.stockIndexPriceEntity
        val builder = BooleanBuilder()

        condition.search?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { keyword ->
                builder.and(
                    stockIndex.name.containsIgnoreCase(keyword)
                        .or(stockIndex.nameEn.containsIgnoreCase(keyword)),
                )
            }

        return jpaQueryFactory
            .select(
                Projections.constructor(
                    StockIndexPriceSummary::class.java,
                    stockIndex.id,
                    stockIndex.name,
                    stockIndex.nameEn,
                    stockIndexPrice.closePrice,
                    stockIndexPrice.changeAmount,
                    stockIndexPrice.changeRate,
                    stockIndexPrice.openPrice,
                    stockIndexPrice.highPrice,
                    stockIndexPrice.lowPrice,
                ),
            )
            .from(stockIndex)
            .join(stockIndexPrice).on(stockIndexPrice.stockIndexId.eq(stockIndex.id))
            .where(builder)
            .orderBy(condition.toOrderSpecifier(stockIndex, stockIndexPrice))
            .fetch()
    }

    private fun StockIndexPriceQueryCondition.toOrderSpecifier(
        stockIndex: QStockIndexEntity,
        stockIndexPrice: QStockIndexPriceEntity,
    ): OrderSpecifier<*> {
        val order =
            when (sortDirection) {
                SortDirection.ASC -> Order.ASC
                SortDirection.DESC -> Order.DESC
            }

        return when (sortBy) {
            StockIndexPriceSortBy.NAME -> OrderSpecifier(order, stockIndex.name)
            StockIndexPriceSortBy.CHANGE_RATE -> OrderSpecifier(order, stockIndexPrice.changeRate)
        }
    }
}