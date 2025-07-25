package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.OhengUtil

class SageokSuriOhengAnalyzer {
    fun analyze(sageokSuri: SageokSuri): SageokSuriOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        // 원격의 오행
        val wonOheng = OhengUtil.getOhengByStroke(sageokSuri.wonGyeok)
        ohengCount["${wonOheng}(${getHanja(wonOheng)})"] = ohengCount["${wonOheng}(${getHanja(wonOheng)})"]!! + 1

        // 형격의 오행
        val hyeongOheng = OhengUtil.getOhengByStroke(sageokSuri.hyeongGyeok)
        ohengCount["${hyeongOheng}(${getHanja(hyeongOheng)})"] = ohengCount["${hyeongOheng}(${getHanja(hyeongOheng)})"]!! + 1

        // 이격의 오행 (있을 경우)
        sageokSuri.iGyeok?.let {
            val iOheng = OhengUtil.getOhengByStroke(it)
            ohengCount["${iOheng}(${getHanja(iOheng)})"] = ohengCount["${iOheng}(${getHanja(iOheng)})"]!! + 1
        }

        // 정격의 오행
        val jeongOheng = OhengUtil.getOhengByStroke(sageokSuri.jeongGyeok)
        ohengCount["${jeongOheng}(${getHanja(jeongOheng)})"] = ohengCount["${jeongOheng}(${getHanja(jeongOheng)})"]!! + 1

        return SageokSuriOheng(ohengDistribution = ohengCount)
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
