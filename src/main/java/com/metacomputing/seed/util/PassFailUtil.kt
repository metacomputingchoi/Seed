// util/PassFailUtil.kt
package com.metacomputing.seed.util

object PassFailUtil {
    fun checkEumYangHarmony(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true
        if (arrangement.first() == arrangement.last()) return false

        val eumCount = arrangement.count { it == "음" }
        val yangCount = arrangement.count { it == "양" }
        return eumCount > 0 && yangCount > 0
    }

    fun checkOhaengSangSaeng(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true

        // 상극 체크
        arrangement.zipWithNext().forEach { (a, b) ->
            if (OhaengRelationUtil.isSangGeuk(a, b)) return false
        }

        // 3개 이상 연속 동일 체크
        var consecutive = 1
        for (i in 1 until arrangement.size) {
            if (arrangement[i] == arrangement[i-1]) {
                if (++consecutive >= 3) return false
            } else consecutive = 1
        }

        // 처음과 끝 상극 체크
        if (OhaengRelationUtil.isSangGeuk(arrangement.first(), arrangement.last())) return false

        // 상생 비율 체크
        val relations = arrangement.zipWithNext().filter { (a, b) -> a != b }
        if (relations.isNotEmpty()) {
            val sangSaengRatio = relations.count { (a, b) -> OhaengRelationUtil.isSangSaeng(a, b) }.toDouble() / relations.size
            if (sangSaengRatio < 0.6) return false
        }

        return true
    }

    fun checkSageokSuriOhaeng(arrangement: List<String>): Boolean {
        if (arrangement.size != 3) return false

        // 상극 체크
        arrangement.zipWithNext().forEach { (a, b) ->
            if (OhaengRelationUtil.isSangGeuk(a, b)) return false
        }

        // 순환 상극 체크
        if (OhaengRelationUtil.isSangGeuk(arrangement.last(), arrangement.first())) return false

        // 모두 동일한 경우 제외
        return arrangement.toSet().size != 1
    }
}