// util/OhaengRelationUtil.kt
package com.metacomputing.seed.util

object OhaengRelationUtil {

    private val ohaengToNumber = mapOf(
        "목" to 0, "화" to 1, "토" to 2, "금" to 3, "수" to 4
    )

    private val numberToOhaeng = mapOf(
        0 to "목", 1 to "화", 2 to "토", 3 to "금", 4 to "수"
    )

    fun isSangSaeng(first: String, second: String): Boolean {
        val firstNum = ohaengToNumber[first] ?: return false
        val secondNum = ohaengToNumber[second] ?: return false
        return (firstNum + 1) % 5 == secondNum
    }

    fun isSangGeuk(first: String, second: String): Boolean {
        val firstNum = ohaengToNumber[first] ?: return false
        val secondNum = ohaengToNumber[second] ?: return false

        // 정상극: first가 second를 극한다
        val isNormalGeuk = (firstNum + 2) % 5 == secondNum

        // 역상극: second가 first를 극한다
        val isReverseGeuk = (secondNum + 2) % 5 == firstNum

        return isNormalGeuk || isReverseGeuk
    }

    // 더 명확한 상극 체크 메서드들
    fun isNormalGeuk(first: String, second: String): Boolean {
        val firstNum = ohaengToNumber[first] ?: return false
        val secondNum = ohaengToNumber[second] ?: return false
        return (firstNum + 2) % 5 == secondNum
    }

    fun isReverseGeuk(first: String, second: String): Boolean {
        val firstNum = ohaengToNumber[first] ?: return false
        val secondNum = ohaengToNumber[second] ?: return false
        return (secondNum + 2) % 5 == firstNum
    }

    fun getDetailedRelation(first: String, second: String): String {
        return when {
            first == second -> "동일"
            isSangSaeng(first, second) -> "상생"
            isNormalGeuk(first, second) -> "정상극(${first}극${second})"
            isReverseGeuk(first, second) -> "역상극(${second}극${first})"
            else -> "중립"
        }
    }

    fun calculateArrayScore(arrangement: List<String>): Int {
        if (arrangement.size < 2) return 100

        var sangSaengCount = 0
        var sangGeukCount = 0
        var sameCount = 0

        for (i in 0 until arrangement.size - 1) {
            when {
                isSangSaeng(arrangement[i], arrangement[i + 1]) -> sangSaengCount++
                isSangGeuk(arrangement[i], arrangement[i + 1]) -> sangGeukCount++
                arrangement[i] == arrangement[i + 1] -> sameCount++
            }
        }

        val baseScore = 70
        val score = baseScore + (sangSaengCount * 15) - (sangGeukCount * 20) - (sameCount * 5)
        return score.coerceIn(0, 100)
    }

    fun calculateBalanceScore(distribution: Map<String, Int>): Int {
        val values = distribution.values
        val total = values.sum()
        if (total == 0) return 0

        val avg = total.toDouble() / 5
        var deviation = 0.0

        distribution.forEach { (ohaeng, count) ->
            deviation += Math.abs(count - avg)
        }

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