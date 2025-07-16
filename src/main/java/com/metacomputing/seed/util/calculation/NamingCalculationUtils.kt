// util/calculation/NamingCalculationUtils.kt
package com.metacomputing.seed.util.calculation

import com.metacomputing.seed.domain.constants.NamingCalculationConstants
import com.metacomputing.seed.domain.model.name.Sagyeok

object NamingCalculationUtils {

    fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): Sagyeok {
        val totalLength = hanjaHoeksuValues.size
        val nameLength = totalLength - surLength

        // 유효성 검사
        require(surLength >= 1) { "성은 최소 1자여야 합니다" }
        require(nameLength >= 1) { "이름은 최소 1자여야 합니다" }

        // 성씨와 이름 부분 분리
        val surHoeksu = hanjaHoeksuValues.subList(0, surLength)
        val nameHoeksu = hanjaHoeksuValues.subList(surLength, totalLength).toMutableList()

        // 이름이 1자인 경우 허수(0) 추가
        if (nameLength == 1) {
            nameHoeksu.add(0)
        }

        // 명상자와 명하자 분리 (일반화된 규칙)
        val myeongsangjaEndIdx = nameHoeksu.size / 2
        val myeongsangja = nameHoeksu.subList(0, myeongsangjaEndIdx).sum()
        val myeonghaja = nameHoeksu.subList(myeongsangjaEndIdx, nameHoeksu.size).sum()

        // 사격 계산
        val jeong = hanjaHoeksuValues.sum()  // 정격: 전체 합
        val won = nameHoeksu.sum()           // 원격: 이름 전체 합
        val i = surHoeksu.sum() + myeonghaja // 이격: 성씨전체 + 명하자
        val hyeong = surHoeksu.sum() + myeongsangja // 형격: 성씨전체 + 명상자

        // 81 초과시 처리 (1~81 범위로 조정)
        fun adjustTo81(value: Int): Int {
            return if (value <= 81) value
            else ((value - 1) % 81) + 1
        }

        return Sagyeok(
            won = adjustTo81(won),
            hyeong = adjustTo81(hyeong),
            i = adjustTo81(i),
            jeong = adjustTo81(jeong)
        )
    }

    fun calculateSagyeokScore(
        sagyeok: Sagyeok,
        strokeMeanings: Map<Int, Map<String, Any>>
    ): SagyeokScoreResult {
        val weights = mapOf(
            "형격" to 0.35,
            "정격" to 0.30,
            "원격" to 0.20,
            "이격" to 0.15
        )

        val luckyLevelScores = mapOf(
            "최상운수" to 100,
            "상운수" to 80,
            "양운수" to 60,
            "흉운수" to 30,
            "최흉운수" to 0
        )

        val wonScore = getLuckyLevelScore(sagyeok.won, strokeMeanings, luckyLevelScores)
        val hyeongScore = getLuckyLevelScore(sagyeok.hyeong, strokeMeanings, luckyLevelScores)
        val iScore = getLuckyLevelScore(sagyeok.i, strokeMeanings, luckyLevelScores)
        val jeongScore = getLuckyLevelScore(sagyeok.jeong, strokeMeanings, luckyLevelScores)

        val totalScore = (wonScore * weights["원격"]!! +
                hyeongScore * weights["형격"]!! +
                iScore * weights["이격"]!! +
                jeongScore * weights["정격"]!!).toInt()

        return SagyeokScoreResult(
            totalScore = totalScore,
            hyeongScore = hyeongScore,
            jeongScore = jeongScore,
            wonScore = wonScore,
            iScore = iScore,
            breakdown = mapOf(
                "원격" to (wonScore * weights["원격"]!!).toInt(),
                "형격" to (hyeongScore * weights["형격"]!!).toInt(),
                "이격" to (iScore * weights["이격"]!!).toInt(),
                "정격" to (jeongScore * weights["정격"]!!).toInt()
            )
        )
    }

    fun calculateMixedSagyeokScore(
        sagyeok: Sagyeok,
        sagyeokScoringWeight: Float,
        strokeMeanings: Map<Int, Map<String, Any>>? = null
    ): Float {
        // 기존 방식 점수 (길한 획수 개수)
        val oldScore = countGilhanHoeksu(sagyeok.getValues()).toFloat()

        // 가중치가 0이면 기존 방식만 사용
        if (sagyeokScoringWeight == 0.0f || strokeMeanings == null || strokeMeanings.isEmpty()) {
            return oldScore
        }

        // 새로운 방식 점수
        val newScoreResult = calculateSagyeokScore(sagyeok, strokeMeanings)

        // 100점 만점을 4점 만점으로 변환 (oldScore는 모두 4점인 경우만 얻어내고 있음)
        val newScore = (newScoreResult.totalScore / 25.0).toFloat()

        // 가중 평균 계산
        val mixedScore = (oldScore * (1 - sagyeokScoringWeight) + newScore * sagyeokScoringWeight).toFloat()

        return mixedScore
    }

    fun getMinScore(
        isComplexSurnameSingleName: Boolean,
        surLength: Int,
        nameLength: Int
    ): Int {
        return when {
            isComplexSurnameSingleName -> NamingCalculationConstants.MinScore.COMPLEX_SURNAME_SINGLE_NAME
            surLength == 1 && nameLength == 1 -> NamingCalculationConstants.MinScore.SINGLE_SURNAME_SINGLE_NAME
            else -> NamingCalculationConstants.MinScore.DEFAULT
        }
    }

    fun isComplexSurnameSingleName(surLength: Int, nameLength: Int): Boolean {
        return surLength >= 2 && nameLength == 1
    }

    fun countGilhanHoeksu(values: List<Int>): Int {
        return values.count { it in NamingCalculationConstants.GILHAN_HOEKSU }
    }

    private fun getLuckyLevelScore(
        number: Int,
        strokeMeanings: Map<Int, Map<String, Any>>,
        luckyLevelScores: Map<String, Int>
    ): Int {
        val meaning = strokeMeanings[number]
        if (meaning == null) {
            return 50
        }
        val luckyLevel = meaning["lucky_level"] as? String
        if (luckyLevel == null) {
            return 50
        }
        val score = luckyLevelScores[luckyLevel] ?: 50
        return score
    }
}

data class SagyeokScoreResult(
    val totalScore: Int,
    val hyeongScore: Int,
    val jeongScore: Int,
    val wonScore: Int,
    val iScore: Int,
    val breakdown: Map<String, Int>
)