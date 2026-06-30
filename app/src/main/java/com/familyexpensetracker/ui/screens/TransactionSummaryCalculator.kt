package com.familyexpensetracker.ui.screens

import com.familyexpensetracker.data.model.CategorySummary
import com.familyexpensetracker.data.model.MonthSummary
import com.familyexpensetracker.data.model.SubcategoryGroup
import com.familyexpensetracker.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionSummaryCalculator(
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
) {

    fun calculateCategorySummaries(transactions: List<Transaction>): List<CategorySummary> {
        return transactions
            .groupBy { it.category }
            .map { (category, txns) ->
                CategorySummary(
                    category = category,
                    totalAmount = txns.sumOf { it.amount },
                    transactionCount = txns.size,
                    subcategories = calculateSubcategoryGroups(txns)
                )
            }
            .sortedByDescending { kotlin.math.abs(it.totalAmount) }
    }

    fun calculateSubcategoryGroups(transactions: List<Transaction>): List<SubcategoryGroup> {
        return transactions
            .groupBy { it.subcategory }
            .map { (subcategory, txns) ->
                SubcategoryGroup(
                    subcategory = subcategory,
                    totalAmount = txns.sumOf { it.amount },
                    transactionCount = txns.size,
                    transactions = txns.sortedByDescending { it.date }
                )
            }
            .sortedByDescending { kotlin.math.abs(it.totalAmount) }
    }

    fun calculateMonthSummaries(transactions: List<Transaction>): List<MonthSummary> {
        return transactions
            .mapNotNull { txn ->
                runCatching {
                    val date = dateFormat.parse(txn.date) ?: return@mapNotNull null
                    val cal = Calendar.getInstance().apply { time = date }
                    Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), txn.amount)
                }.getOrNull()
            }
            .groupBy { (year, month, _) -> Pair(year, month) }
            .map { (yearMonth, triples) ->
                MonthSummary(
                    year = yearMonth.first,
                    month = yearMonth.second,
                    totalAmount = triples.sumOf { it.third },
                    transactionCount = triples.size
                )
            }
            .sortedWith(compareByDescending<MonthSummary> { it.year }.thenByDescending { it.month })
    }

    fun calculateFYMonthSummaries(transactions: List<Transaction>, fyStartYear: Int): List<MonthSummary> {
        // Build all 12 FY months in order: Apr(3)..Dec(11) of fyStartYear, then Jan(0)..Mar(2) of fyStartYear+1
        val fyMonths = (Calendar.APRIL..Calendar.DECEMBER).map { Pair(fyStartYear, it) } +
            (Calendar.JANUARY..Calendar.MARCH).map { Pair(fyStartYear + 1, it) }

        val parsed = transactions.mapNotNull { txn ->
            runCatching {
                val date = dateFormat.parse(txn.date) ?: return@mapNotNull null
                val cal = Calendar.getInstance().apply { time = date }
                Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), txn.amount)
            }.getOrNull()
        }
        val grouped = parsed.groupBy { (year, month, _) -> Pair(year, month) }

        return fyMonths.map { (year, month) ->
            val triples = grouped[Pair(year, month)] ?: emptyList()
            MonthSummary(
                year = year,
                month = month,
                totalAmount = triples.sumOf { it.third },
                transactionCount = triples.size
            )
        }
    }
}
