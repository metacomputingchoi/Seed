// analyzer/SajuOhaengAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SajuOhaengAnalyzer {
    fun analyze(sajuInfo: SajuInfo): SajuOhaeng {
        val ohaengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        val stems = listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        stems.forEach { stem ->
            val ohaeng = when(stem) {
                "甲", "乙" -> "목(木)"
                "丙", "丁" -> "화(火)"
                "戊", "己" -> "토(土)"
                "庚", "辛" -> "금(金)"
                "壬", "癸" -> "수(水)"
                else -> "토(土)"
            }
            ohaengCount[ohaeng] = ohaengCount[ohaeng]!! + 1
        }

        val branches = listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)
        branches.forEach { branch ->
            val ohaeng = when(branch) {
                "子" -> "수(水)"
                "丑" -> "토(土)"
                "寅", "卯" -> "목(木)"
                "辰" -> "토(土)"
                "巳", "午" -> "화(火)"
                "未" -> "토(土)"
                "申", "酉" -> "금(金)"
                "戌" -> "토(土)"
                "亥" -> "수(水)"
                else -> "토(土)"
            }
            ohaengCount[ohaeng] = ohaengCount[ohaeng]!! + 1
        }

        return SajuOhaeng(ohaengDistribution = ohaengCount)
    }
}
