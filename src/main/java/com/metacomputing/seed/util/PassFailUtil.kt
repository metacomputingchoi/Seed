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

        arrangement.zipWithNext().forEach { (a, b) ->
            if (OhaengRelationUtil.isSangGeuk(a, b)) return false
        }

        var consecutive = 1
        for (i in 1 until arrangement.size) {
            if (arrangement[i] == arrangement[i-1]) {
                if (++consecutive >= 3) return false
            } else consecutive = 1
        }

        if (OhaengRelationUtil.isSangGeuk(arrangement.first(), arrangement.last())) return false

        val relations = arrangement.zipWithNext().filter { (a, b) -> a != b }
        if (relations.isNotEmpty()) {
            val sangSaengRatio = relations.count { (a, b) -> OhaengRelationUtil.isSangSaeng(a, b) }.toDouble() / relations.size
            if (sangSaengRatio < 0.6) return false
        }

        return true
    }

    fun checkSageokSuriOhaeng(arrangement: List<String>): Boolean {
        if (arrangement.size != 3) return false

        arrangement.zipWithNext().forEach { (a, b) ->
            if (OhaengRelationUtil.isSangGeuk(a, b)) return false
        }

        if (OhaengRelationUtil.isSangGeuk(arrangement.last(), arrangement.first())) return false

        return arrangement.toSet().size != 1
    }
}