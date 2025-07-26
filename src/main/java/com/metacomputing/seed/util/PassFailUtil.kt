// util/PassFailUtil.kt
package com.metacomputing.seed.util

object PassFailUtil {

    fun checkEumYangFirstLast(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.isEmpty()) return false

        val firstChar = arrangement[0]
        val lastChar = arrangement.last()

        return firstChar != lastChar
    }

    fun checkEumYangHarmony(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true

        if (!checkEumYangFirstLast(arrangement, surnameLength)) return false

        val eumCount = arrangement.count { it == "음" }
        val yangCount = arrangement.count { it == "양" }

        return eumCount > 0 && yangCount > 0
    }

    fun checkOhaengSangSaeng(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true

        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return false
            }
        }

        var consecutiveCount = 1
        for (i in 1 until arrangement.size) {
            if (arrangement[i] == arrangement[i - 1]) {
                consecutiveCount++
                if (consecutiveCount >= 3) {
                    return false
                }
            } else {
                consecutiveCount = 1
            }
        }

        val firstChar = arrangement[0]
        val lastChar = arrangement.last()

        if (OhaengRelationUtil.isSangGeuk(firstChar, lastChar)) {
            return false
        }

        var sangSaengCount = 0
        var totalRelations = 0

        for (i in 0 until arrangement.size - 1) {
            if (arrangement[i] != arrangement[i + 1]) {
                totalRelations++
                if (OhaengRelationUtil.isSangSaeng(arrangement[i], arrangement[i + 1])) {
                    sangSaengCount++
                }
            }
        }

        if (totalRelations > 0) {
            val sangSaengRatio = sangSaengCount.toDouble() / totalRelations
            if (sangSaengRatio < 0.6) {
                return false
            }
        }

        val isPerfect = OhaengRelationUtil.isSangSaeng(firstChar, lastChar) &&
                !hasAnySangGeuk(arrangement)

        return true
    }

    fun checkSageokSuriOhaeng(arrangement: List<String>): Boolean {
        if (arrangement.size != 3) return false

        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return false
            }
        }

        if (OhaengRelationUtil.isSangGeuk(arrangement.last(), arrangement.first())) {
            return false
        }

        if (arrangement.toSet().size == 1) {
            return false
        }

        val isCircularSangSaeng = OhaengRelationUtil.isSangSaeng(arrangement[0], arrangement[1]) &&
                OhaengRelationUtil.isSangSaeng(arrangement[1], arrangement[2]) &&
                OhaengRelationUtil.isSangSaeng(arrangement[2], arrangement[0])

        return true
    }

    private fun hasAnySangGeuk(arrangement: List<String>): Boolean {
        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return true
            }
        }
        return false
    }
}