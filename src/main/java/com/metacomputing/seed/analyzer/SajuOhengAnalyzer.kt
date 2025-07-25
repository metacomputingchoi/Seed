package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.OhengUtil

class SajuOhengAnalyzer {
    fun analyze(sajuInfo: SajuInfo): SajuOheng {
        val ohengCount = mutableMapOf(
            "木" to 0,
            "火" to 0,
            "土" to 0,
            "金" to 0,
            "水" to 0
        )

        // 천간의 오행 분석
        val stems = listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        stems.forEach { stem ->
            val oheng = when(stem) {
                "甲", "乙" -> "木"
                "丙", "丁" -> "火"
                "戊", "己" -> "土"
                "庚", "辛" -> "金"
                "壬", "癸" -> "水"
                else -> "土"
            }
            ohengCount[oheng] = ohengCount[oheng]!! + 1
        }

        // 지지의 오행 분석
        val branches = listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)
        branches.forEach { branch ->
            val oheng = when(branch) {
                "子" -> "水"
                "丑" -> "土"
                "寅", "卯" -> "木"
                "辰" -> "土"
                "巳", "午" -> "火"
                "未" -> "土"
                "申", "酉" -> "金"
                "戌" -> "土"
                "亥" -> "水"
                else -> "土"
            }
            ohengCount[oheng] = ohengCount[oheng]!! + 1
        }

        // 오행 순서에 맞게 정렬된 맵 반환
        val orderedOhengCount = linkedMapOf<String, Int>()
        listOf("木", "火", "土", "金", "水").forEach { oheng ->
            orderedOhengCount[oheng] = ohengCount[oheng]!!
        }

        return SajuOheng(ohengDistribution = orderedOhengCount)
    }
}