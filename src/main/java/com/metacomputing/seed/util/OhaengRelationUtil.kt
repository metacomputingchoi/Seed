// util/OhaengRelationUtil.kt
package com.metacomputing.seed.util

import com.metacomputing.seed.Constants

object OhaengRelationUtil {
    fun isSangSaeng(first: String, second: String): Boolean {
        val firstNum = Constants.OHAENG_MAP[first] ?: return false
        val secondNum = Constants.OHAENG_MAP[second] ?: return false
        return (firstNum + 1) % 5 == secondNum
    }

    fun isSangGeuk(first: String, second: String): Boolean {
        val firstNum = Constants.OHAENG_MAP[first] ?: return false
        val secondNum = Constants.OHAENG_MAP[second] ?: return false
        return ((firstNum + 2) % 5 == secondNum) || ((secondNum + 2) % 5 == firstNum)
    }

    fun getDetailedRelation(first: String, second: String) = when {
        first == second -> "동일"
        isSangSaeng(first, second) -> "상생"
        (Constants.OHAENG_MAP[first]!! + 2) % 5 == Constants.OHAENG_MAP[second] -> "정상극(${first}극$second)"
        (Constants.OHAENG_MAP[second]!! + 2) % 5 == Constants.OHAENG_MAP[first] -> "역상극(${second}극$first)"
        else -> "중립"
    }

    fun calculateArrayScore(arrangement: List<String>): Int {
        if (arrangement.size < 2) return 100

        var sangSaeng = 0
        var sangGeuk = 0
        var same = 0

        arrangement.zipWithNext().forEach { (a, b) ->
            when {
                isSangSaeng(a, b) -> sangSaeng++
                isSangGeuk(a, b) -> sangGeuk++
                a == b -> same++
            }
        }

        return (70 + sangSaeng * 15 - sangGeuk * 20 - same * 5).coerceIn(0, 100)
    }

    fun calculateBalanceScore(distribution: Map<String, Int>): Int {
        val total = distribution.values.sum()
        if (total == 0) return 0

        val avg = total / 5.0
        val deviation = distribution.values.sumOf { kotlin.math.abs(it - avg) }

        return when {
            deviation <= 2 -> 100
            deviation <= 4 -> 85
            deviation <= 6 -> 70
            deviation <= 8 -> 55
            deviation <= 10 -> 40
            else -> 25
        }
    }
}