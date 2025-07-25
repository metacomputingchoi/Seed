package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SageokSuriEumYangAnalyzer {
    fun analyze(sageokSuri: SageokSuri): SageokSuriEumYang {
        var eumCount = 0
        var yangCount = 0

        // 원격 음양
        if (sageokSuri.wonGyeok % 2 == 0) eumCount++ else yangCount++

        // 형격 음양
        if (sageokSuri.hyeongGyeok % 2 == 0) eumCount++ else yangCount++

        // 이격 음양 (있을 경우)
        sageokSuri.iGyeok?.let {
            if (it % 2 == 0) eumCount++ else yangCount++
        }

        // 정격 음양
        if (sageokSuri.jeongGyeok % 2 == 0) eumCount++ else yangCount++

        return SageokSuriEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
