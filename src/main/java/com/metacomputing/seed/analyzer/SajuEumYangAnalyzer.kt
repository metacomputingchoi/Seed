package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SajuEumYangAnalyzer {
    fun analyze(sajuInfo: SajuInfo): SajuEumYang {
        var eumCount = 0
        var yangCount = 0

        // 천간의 음양 (갑병무경임=양, 을정기신계=음)
        val yangStems = setOf("甲", "丙", "戊", "庚", "壬")
        val eumStems = setOf("乙", "丁", "己", "辛", "癸")

        val stems = listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        stems.forEach { stem ->
            when {
                yangStems.contains(stem) -> yangCount++
                eumStems.contains(stem) -> eumCount++
            }
        }

        // 지지의 음양 (자인진오신술=양, 축묘사미유해=음)
        val yangBranches = setOf("子", "寅", "辰", "午", "申", "戌")
        val eumBranches = setOf("丑", "卯", "巳", "未", "酉", "亥")

        val branches = listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)
        branches.forEach { branch ->
            when {
                yangBranches.contains(branch) -> yangCount++
                eumBranches.contains(branch) -> eumCount++
            }
        }

        return SajuEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
