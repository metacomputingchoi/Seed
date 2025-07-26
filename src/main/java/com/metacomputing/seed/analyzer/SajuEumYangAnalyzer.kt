// analyzer/SajuEumYangAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import java.text.Normalizer

class SajuEumYangAnalyzer {
    fun analyze(sajuInfo: SajuInfo): SajuEumYang {
        var eumCount = 0
        var yangCount = 0

        val yangStems = setOf("甲", "丙", "戊", "庚", "壬")
        val eumStems = setOf("乙", "丁", "己", "辛", "癸")

        val stems = listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        stems.forEach { stem ->
            val normalizedStem = Normalizer.normalize(stem, Normalizer.Form.NFC)
            when {
                yangStems.contains(normalizedStem) -> yangCount++
                eumStems.contains(normalizedStem) -> eumCount++
            }
        }

        val yangBranches = setOf("子", "寅", "辰", "午", "申", "戌")
        val eumBranches = setOf("丑", "卯", "巳", "未", "酉", "亥")

        val branches = listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)
        branches.forEach { branch ->
            val normalizedBranch = Normalizer.normalize(branch, Normalizer.Form.NFC)
            when {
                yangBranches.contains(normalizedBranch) -> yangCount++
                eumBranches.contains(normalizedBranch) -> eumCount++
            }
        }

        return SajuEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
