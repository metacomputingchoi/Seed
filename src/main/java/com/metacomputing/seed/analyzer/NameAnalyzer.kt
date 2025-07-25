package com.metacomputing.seed.analyzer

import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.model.*

class NameAnalyzer {
    private val sajuAnalyzer = SajuAnalyzer()
    private val sageokSuriAnalyzer = SageokSuriAnalyzer()
    private val sageokSuriOhengAnalyzer = SageokSuriOhengAnalyzer()
    private val sageokSuriEumYangAnalyzer = SageokSuriEumYangAnalyzer()
    private val sajuOhengAnalyzer = SajuOhengAnalyzer()
    private val sajuEumYangAnalyzer = SajuEumYangAnalyzer()
    private val hoeksuOhengAnalyzer = HoeksuOhengAnalyzer()
    private val hoeksuEumYangAnalyzer = HoeksuEumYangAnalyzer()
    private val baleumOhengAnalyzer = BaleumOhengAnalyzer()
    private val baleumEumYangAnalyzer = BaleumEumYangAnalyzer()

    fun analyze(nameInput: NameInput, timePointResult: TimePointResult): NameEvaluation {
        // 사주 정보 추출
        val sajuInfo = sajuAnalyzer.extractSajuInfo(timePointResult)

        // 각 분석 수행
        val sageokSuri = sageokSuriAnalyzer.analyze(nameInput)
        val sageokSuriOheng = sageokSuriOhengAnalyzer.analyze(sageokSuri)
        val sageokSuriEumYang = sageokSuriEumYangAnalyzer.analyze(sageokSuri)
        val sajuOheng = sajuOhengAnalyzer.analyze(sajuInfo)
        val sajuEumYang = sajuEumYangAnalyzer.analyze(sajuInfo)
        val hoeksuOheng = hoeksuOhengAnalyzer.analyze(nameInput)
        val hoeksuEumYang = hoeksuEumYangAnalyzer.analyze(nameInput)
        val baleumOheng = baleumOhengAnalyzer.analyze(nameInput)
        val baleumEumYang = baleumEumYangAnalyzer.analyze(nameInput)

        // 종합 점수 계산
        val totalScore = calculateTotalScore(
            sageokSuri, sageokSuriOheng, sageokSuriEumYang,
            sajuOheng, sajuEumYang, hoeksuOheng, hoeksuEumYang,
            baleumOheng, baleumEumYang
        )

        return NameEvaluation(
            totalScore = totalScore,
            sageokSuri = sageokSuri,
            sageokSuriOheng = sageokSuriOheng,
            sageokSuriEumYang = sageokSuriEumYang,
            sajuOheng = sajuOheng,
            sajuEumYang = sajuEumYang,
            hoeksuOheng = hoeksuOheng,
            hoeksuEumYang = hoeksuEumYang,
            baleumOheng = baleumOheng,
            baleumEumYang = baleumEumYang
        )
    }

    private fun calculateTotalScore(
        sageokSuri: SageokSuri,
        sageokSuriOheng: SageokSuriOheng,
        sageokSuriEumYang: SageokSuriEumYang,
        sajuOheng: SajuOheng,
        sajuEumYang: SajuEumYang,
        hoeksuOheng: HoeksuOheng,
        hoeksuEumYang: HoeksuEumYang,
        baleumOheng: BaleumOheng,
        baleumEumYang: BaleumEumYang
    ): Int {
        // TODO: 실제 종합 점수 계산 로직 구현
        return 75  // 임시값
    }
}
