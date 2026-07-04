package com.familyexpensetracker.utils

import kotlin.math.abs

object IndianNumberFormatter {
    fun formatAmount(amount: Double): String {
        val absAmount = abs(amount)
        val formatted = "%.2f".format(absAmount)
        val dotIndex = formatted.indexOf('.')
        val intPart = formatted.substring(0, dotIndex)
        val decPart = formatted.substring(dotIndex + 1)
        return "₹${applyIndianGrouping(intPart)}.$decPart"
    }

    private fun applyIndianGrouping(intPart: String): String {
        if (intPart.length <= 3) return intPart
        val last3 = intPart.takeLast(3)
        var remaining = intPart.dropLast(3)
        val groups = mutableListOf<String>()
        while (remaining.isNotEmpty()) {
            val takeCount = minOf(2, remaining.length)
            groups.add(0, remaining.takeLast(takeCount))
            remaining = remaining.dropLast(takeCount)
        }
        return "${groups.joinToString(",")},$last3"
    }
}
