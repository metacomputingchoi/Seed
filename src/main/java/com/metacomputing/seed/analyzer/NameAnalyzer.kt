// analyzer/NameAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class NameAnalyzer {
    private val sajuAnalyzer = SajuAnalyzer()
    private val sageokSuriAnalyzer = SageokSuriAnalyzer()
    private val sageokSuriOhaengAnalyzer = SageokSuriOhaengAnalyzer()
    private val sageokSuriEumYangAnalyzer = SageokSuriEumYangAnalyzer()
    private val sajuOhaengAnalyzer = SajuOhaengAnalyzer()
    private val sajuEumYangAnalyzer = SajuEumYangAnalyzer()
    private val hoeksuOhaengAnalyzer = HoeksuOhaengAnalyzer()
    private val hoeksuEumYangAnalyzer = HoeksuEumYangAnalyzer()
    private val baleumOhaengAnalyzer = BaleumOhaengAnalyzer()
    private val baleumEumYangAnalyzer = BaleumEumYangAnalyzer()
    private val sajuNameOhaengAnalyzer = SajuNameOhaengAnalyzer()

    private val jawonOhaengAnalyzer = JawonOhaengAnalyzer()

    fun analyze(nameInput: NameInput): NameEvaluation {
        val sajuInfo = sajuAnalyzer.extractSajuInfo(nameInput.timePointResult)

        val sageokSuri = sageokSuriAnalyzer.analyze(nameInput)
        val sageokSuriOhaeng = sageokSuriOhaengAnalyzer.analyze(sageokSuri)
        val sageokSuriEumYang = sageokSuriEumYangAnalyzer.analyze(sageokSuri)
        val sajuOhaeng = sajuOhaengAnalyzer.analyze(sajuInfo)
        val sajuEumYang = sajuEumYangAnalyzer.analyze(sajuInfo)
        val hoeksuOhaeng = hoeksuOhaengAnalyzer.analyze(nameInput)
        val hoeksuEumYang = hoeksuEumYangAnalyzer.analyze(nameInput)
        val baleumOhaeng = baleumOhaengAnalyzer.analyze(nameInput)
        val baleumEumYang = baleumEumYangAnalyzer.analyze(nameInput)
        val sajuNameOhaeng = sajuNameOhaengAnalyzer.analyze(sajuOhaeng, hoeksuOhaeng, nameInput)
        val jawonOhaeng = jawonOhaengAnalyzer.analyze(nameInput)

        val totalScore = calculateTotalScore(
            sageokSuri, sageokSuriOhaeng, sageokSuriEumYang,
            sajuOhaeng, sajuEumYang, hoeksuOhaeng, hoeksuEumYang,
            baleumOhaeng, baleumEumYang, sajuNameOhaeng, jawonOhaeng
        )

        return NameEvaluation(
            totalScore = totalScore,
            sageokSuri = sageokSuri,
            sageokSuriOhaeng = sageokSuriOhaeng,
            sageokSuriEumYang = sageokSuriEumYang,
            sajuOhaeng = sajuOhaeng,
            sajuEumYang = sajuEumYang,
            hoeksuOhaeng = hoeksuOhaeng,
            hoeksuEumYang = hoeksuEumYang,
            baleumOhaeng = baleumOhaeng,
            baleumEumYang = baleumEumYang,
            sajuNameOhaeng = sajuNameOhaeng,
            jawonOhaeng = jawonOhaeng
        )
    }

    private fun calculateTotalScore(
        sageokSuri: SageokSuri,
        sageokSuriOhaeng: SageokSuriOhaeng,
        sageokSuriEumYang: SageokSuriEumYang,
        sajuOhaeng: SajuOhaeng,
        sajuEumYang: SajuEumYang,
        hoeksuOhaeng: HoeksuOhaeng,
        hoeksuEumYang: HoeksuEumYang,
        baleumOhaeng: BaleumOhaeng,
        baleumEumYang: BaleumEumYang,
        sajuNameOhaeng: SajuNameOhaeng,
        jawonOhaeng: JawonOhaeng
    ): Int {
        var score = 50

        val sageokScore = calculateSageokScore(sageokSuri)
        score += sageokScore

        val ohaengScore = calculateOhaengBalanceScore(sajuNameOhaeng, hoeksuOhaeng, baleumOhaeng)
        score += ohaengScore

        val eumyangScore = calculateEumYangBalanceScore(sajuEumYang, hoeksuEumYang, baleumEumYang)
        score += eumyangScore

        return score.coerceIn(0, 100)
    }

    private fun calculateSageokScore(sageokSuri: SageokSuri): Int {
        var score = 0

        if (sageokSuri.wonGyeokFortune.contains("상운수") || sageokSuri.wonGyeokFortune.contains("대길")) score += 10
        else if (sageokSuri.wonGyeokFortune.contains("길")) score += 5

        if (sageokSuri.hyeongGyeokFortune.contains("상운수") || sageokSuri.hyeongGyeokFortune.contains("대길")) score += 10
        else if (sageokSuri.hyeongGyeokFortune.contains("길")) score += 5

        if (sageokSuri.iGyeokFortune.contains("상운수") || sageokSuri.iGyeokFortune.contains("대길")) score += 10
        else if (sageokSuri.iGyeokFortune.contains("길")) score += 5

        if (sageokSuri.jeongGyeokFortune.contains("상운수") || sageokSuri.jeongGyeokFortune.contains("대길")) score += 10
        else if (sageokSuri.jeongGyeokFortune.contains("길")) score += 5

        return score
    }

    private fun calculateOhaengBalanceScore(
        sajuNameOhaeng: SajuNameOhaeng,
        hoeksuOhaeng: HoeksuOhaeng,
        baleumOhaeng: BaleumOhaeng
    ): Int {
        val distribution = sajuNameOhaeng.ohaengDistribution.values
        val max = distribution.maxOrNull() ?: 0
        val min = distribution.minOrNull() ?: 0

        return when (max - min) {
            0, 1, 2 -> 30
            3, 4 -> 20
            5, 6 -> 10
            else -> 0
        }
    }

    private fun calculateEumYangBalanceScore(
        sajuEumYang: SajuEumYang,
        hoeksuEumYang: HoeksuEumYang,
        baleumEumYang: BaleumEumYang
    ): Int {
        val totalEum = sajuEumYang.eumCount + hoeksuEumYang.eumCount + baleumEumYang.eumCount
        val totalYang = sajuEumYang.yangCount + hoeksuEumYang.yangCount + baleumEumYang.yangCount
        val diff = kotlin.math.abs(totalEum - totalYang)

        return when (diff) {
            0, 1, 2 -> 30
            3, 4 -> 20
            5, 6 -> 10
            else -> 0
        }
    }
}
