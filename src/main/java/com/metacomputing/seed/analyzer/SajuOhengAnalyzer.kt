package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.OhengUtil

class SajuOhengAnalyzer {
    fun analyze(sajuInfo: SajuInfo): SajuOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        // 천간의 오행 분석
        val stems = listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        stems.forEach { stem ->
            val oheng = OhengUtil.HEAVENLY_STEM_OHENG[stem] ?: "토"
            ohengCount["${oheng}(${getHanja(oheng)})"] = ohengCount["${oheng}(${getHanja(oheng)})"]!! + 1
        }

        // 지지의 오행 분석
        val branches = listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)
        branches.forEach { branch ->
            val oheng = OhengUtil.EARTHLY_BRANCH_OHENG[branch] ?: "토"
            ohengCount["${oheng}(${getHanja(oheng)})"] = ohengCount["${oheng}(${getHanja(oheng)})"]!! + 1
        }

        return SajuOheng(ohengDistribution = ohengCount)
    }

    private fun getHanja(oheng: String): String {
        return when(oheng) {
            "목" -> "木"
            "화" -> "火"
            "토" -> "土"
            "금" -> "金"
            "수" -> "水"
            else -> ""
        }
    }
}
