package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SageokSuriEumYangAnalyzer {
    fun analyze(sageokSuri: SageokSuri): SageokSuriEumYang {
        var eumCount = 0
        var yangCount = 0
        val arrangement = mutableListOf<String>()

        // 이격 음양 (홀수=양, 짝수=음)
        if (sageokSuri.iGyeok % 2 == 0) {
            eumCount++
            arrangement.add("음")
        } else {
            yangCount++
            arrangement.add("양")
        }

        // 형격 음양
        if (sageokSuri.hyeongGyeok % 2 == 0) {
            eumCount++
            arrangement.add("음")
        } else {
            yangCount++
            arrangement.add("양")
        }

        // 원격 음양
        if (sageokSuri.wonGyeok % 2 == 0) {
            eumCount++
            arrangement.add("음")
        } else {
            yangCount++
            arrangement.add("양")
        }

        return SageokSuriEumYang(
            eumCount = eumCount,
            yangCount = yangCount,
            arrangement = arrangement  // 이격-형격-원격 순서
        )
    }
}
