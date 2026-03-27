package net.directional.recruitment.stockindex.outbound

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import net.directional.recruitment.stockindex.app.SortDirection
import net.directional.recruitment.stockindex.app.StockIndexQueryCondition
import net.directional.recruitment.stockindex.app.StockIndexSortBy
import net.directional.recruitment.stockindex.app.StockIndexSummary
import org.springframework.stereotype.Repository

@Repository
class StockIndexQueryRepositoryImpl (
    private val jpaQueryFactory: JPAQueryFactory
) : StockIndexQueryRepository {

    override fun findAllByCondition(condition: StockIndexQueryCondition): List<StockIndexSummary> {
        val stockIndex = QStockIndexEntity.stockIndexEntity
        val constituent = QStockIndexConstituentEntity.stockIndexConstituentEntity
        val builder = BooleanBuilder()

        condition.search?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { keyword ->
                builder.and(
                    stockIndex.name.containsIgnoreCase(keyword)
                        .or(stockIndex.nameEn.containsIgnoreCase(keyword))
                )
            }

        val constituentCount = constituent.id.count()

        return jpaQueryFactory
            .select(
                Projections.constructor(
                    StockIndexSummary::class.java,
                    stockIndex.id,
                    stockIndex.name,
                    stockIndex.nameEn,
                    stockIndex.baseDate,
                    stockIndex.baseIndex,
                    constituentCount.intValue(),
                ),
            )
            .from(stockIndex)
            .leftJoin(constituent).on(constituent.stockIndexId.eq(stockIndex.id))
            .where(builder)
            .groupBy(
                stockIndex.id,
                stockIndex.name,
                stockIndex.nameEn,
                stockIndex.baseDate,
                stockIndex.baseIndex,
            )
            .orderBy(condition.toOrderSpecifier(stockIndex, constituentCount))
            .fetch()
    }

    private fun StockIndexQueryCondition.toOrderSpecifier(
        stockIndex: QStockIndexEntity,
        constituentCount: NumberExpression<Long>,
    ): OrderSpecifier<*> {
        val order =
            when (sortDirection) {
                SortDirection.ASC -> Order.ASC
                SortDirection.DESC -> Order.DESC
            }

        return when (sortBy) {
            StockIndexSortBy.NAME -> OrderSpecifier(order, stockIndex.name)
            StockIndexSortBy.NAME_EN -> OrderSpecifier(order, stockIndex.nameEn)
            StockIndexSortBy.BASE_DATE -> OrderSpecifier(order, stockIndex.baseDate)
            StockIndexSortBy.CONSTITUENT_COUNT -> OrderSpecifier(order, constituentCount)
        }
    }
}