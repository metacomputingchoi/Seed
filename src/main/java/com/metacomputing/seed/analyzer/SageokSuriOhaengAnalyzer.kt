package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SageokSuriOhaengAnalyzer {
    fun analyze(sageokSuri: SageokSuri): SageokSuriOhaeng {
        val ohaengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        val arrangement = mutableListOf<String>()

        // 이격의 오행 (끝자리로 계산)
        val iLastDigit = sageokSuri.iGyeok % 10
        val iOhaeng = getOhaengByLastDigit(iLastDigit)
        ohaengCount[iOhaeng] = ohaengCount[iOhaeng]!! + 1
        arrangement.add(iOhaeng.substring(0, 1))  // "목(木)" -> "목"

        // 형격의 오행 (끝자리로 계산)
        val hyeongLastDigit = sageokSuri.hyeongGyeok % 10
        val hyeongOhaeng = getOhaengByLastDigit(hyeongLastDigit)
        ohaengCount[hyeongOhaeng] = ohaengCount[hyeongOhaeng]!! + 1
        arrangement.add(hyeongOhaeng.substring(0, 1))

        // 원격의 오행 (끝자리로 계산)
        val wonLastDigit = sageokSuri.wonGyeok % 10
        val wonOhaeng = getOhaengByLastDigit(wonLastDigit)
        ohaengCount[wonOhaeng] = ohaengCount[wonOhaeng]!! + 1
        arrangement.add(wonOhaeng.substring(0, 1))

        return SageokSuriOhaeng(
            ohaengDistribution = ohaengCount,
            arrangement = arrangement  // 이격-형격-원격 순서
        )
    }

    private fun getOhaengByLastDigit(lastDigit: Int): String {
        return when (lastDigit) {
            1, 2 -> "목(木)"
            3, 4 -> "화(火)"
            5, 6 -> "토(土)"
            7, 8 -> "금(金)"
            9, 0 -> "수(水)"
            else -> "토(土)"
        }
    }
}
