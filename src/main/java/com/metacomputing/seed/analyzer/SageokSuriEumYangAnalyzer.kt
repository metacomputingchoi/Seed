package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class SageokSuriEumYangAnalyzer {
    fun analyze(sageokSuri: SageokSuri): SageokSuriEumYang {
        var eumCount = 0
        var yangCount = 0

        // 이격 음양 (홀수=양, 짝수=음)
        if (sageokSuri.iGyeok!! % 2 == 0) eumCount++ else yangCount++

        // 형격 음양
        if (sageokSuri.hyeongGyeok % 2 == 0) eumCount++ else yangCount++

        // 원격 음양
        if (sageokSuri.wonGyeok % 2 == 0) eumCount++ else yangCount++

        return SageokSuriEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
