// analyzer/NameAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.OhaengRelationUtil
import com.metacomputing.seed.util.PassFailUtil

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

        // 상세 점수 계산
        val detailedScores = calculateDetailedScores(
            sageokSuri, sageokSuriOhaeng, sageokSuriEumYang,
            sajuOhaeng, sajuEumYang, hoeksuOhaeng, hoeksuEumYang,
            baleumOhaeng, baleumEumYang, sajuNameOhaeng, jawonOhaeng,
            nameInput
        )

        // 가중치를 적용한 총점 계산
        val totalScore = calculateWeightedTotalScore(detailedScores)

        return NameEvaluation(
            totalScore = totalScore,
            detailedScores = detailedScores,
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

    private fun calculateDetailedScores(
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
        jawonOhaeng: JawonOhaeng,
        nameInput: NameInput
    ): DetailedScores {
        return DetailedScores(
            sageokSuriScore = calculateSageokSuriScore(sageokSuri),
            sageokSuriOhaengScore = calculateSageokSuriOhaengScore(sageokSuriOhaeng),
            sageokSuriEumYangScore = calculateSageokSuriEumYangScore(sageokSuriEumYang),
            sajuEumYangScore = calculateSajuEumYangScore(sajuEumYang),
            hoeksuOhaengScore = calculateHoeksuOhaengScore(hoeksuOhaeng, nameInput),
            hoeksuEumYangScore = calculateHoeksuEumYangScore(hoeksuEumYang, nameInput),
            baleumOhaengScore = calculateBaleumOhaengScore(baleumOhaeng, nameInput),
            baleumEumYangScore = calculateBaleumEumYangScore(baleumEumYang, nameInput),
            sajuNameOhaengScore = calculateSajuNameOhaengScore(sajuOhaeng, sajuNameOhaeng, jawonOhaeng),
            jawonOhaengScore = calculateJawonOhaengScore(sajuOhaeng, jawonOhaeng)
        )
    }

    private fun calculateWeightedTotalScore(scores: DetailedScores): Int {
        // 가중치 정의 (성명학 중요도 기반)
        val weights = mapOf(
            "sageokSuri" to 0.20,
            "sajuNameOhaeng" to 0.20,
            "sajuEumYang" to 0.0,
            "hoeksuOhaeng" to 0.0,
            "hoeksuEumYang" to 0.15,
            "baleumOhaeng" to 0.15,
            "baleumEumYang" to 0.15,
            "sageokSuriOhaeng" to 0.15,
            "sageokSuriEumYang" to 0.0,
            "jawonOhaeng" to 0.0
        )

        val weightedScore =
            scores.sageokSuriScore.score * weights["sageokSuri"]!! +
                    scores.sajuNameOhaengScore.score * weights["sajuNameOhaeng"]!! +
                    scores.sajuEumYangScore.score * weights["sajuEumYang"]!! +
                    scores.hoeksuOhaengScore.score * weights["hoeksuOhaeng"]!! +
                    scores.hoeksuEumYangScore.score * weights["hoeksuEumYang"]!! +
                    scores.baleumOhaengScore.score * weights["baleumOhaeng"]!! +
                    scores.baleumEumYangScore.score * weights["baleumEumYang"]!! +
                    scores.sageokSuriOhaengScore.score * weights["sageokSuriOhaeng"]!! +
                    scores.sageokSuriEumYangScore.score * weights["sageokSuriEumYang"]!! +
                    scores.jawonOhaengScore.score * weights["jawonOhaeng"]!!

        return weightedScore.toInt().coerceIn(0, 100)
    }

    // 1. 사격수리 점수 계산 - isPassed 추가
    private fun calculateSageokSuriScore(sageokSuri: SageokSuri): ScoreDetail {
        var score = 0
        val reasons = mutableListOf<String>()
        var passCount = 0

        // 운세 등급별 점수 정의
        fun getFortuneScore(fortune: String): Int {
            return when {
                fortune.contains("최상운수") -> 25
                fortune.contains("상운수") -> 20
                fortune.contains("양운수") -> 15
                fortune.contains("흉운수") && !fortune.contains("최흉운수") -> 5
                fortune.contains("최흉운수") -> 0
                else -> 10
            }
        }

        // 각 격의 점수와 통과 여부 체크
        val fortunes = listOf(
            Pair("원격", sageokSuri.wonGyeokFortune),
            Pair("형격", sageokSuri.hyeongGyeokFortune),
            Pair("이격", sageokSuri.iGyeokFortune),
            Pair("정격", sageokSuri.jeongGyeokFortune)
        )

        fortunes.forEach { (name, fortune) ->
            val fortuneScore = getFortuneScore(fortune)
            score += fortuneScore
            reasons.add("$name: $fortune")

            // 양운수 이상이면 통과로 카운트
            if (fortuneScore >= 15) passCount++
        }

        // 4개 모두가 양운수 이상이어야 통과 (수정)
        val isPassed = passCount == 4

        return ScoreDetail(
            score = score,
            maxScore = 100,
            reason = reasons.joinToString(", "),
            isPassed = isPassed
        )
    }

    // 2. 사격수리오행 점수 계산 - 균형 체크 제거
    private fun calculateSageokSuriOhaengScore(sageokSuriOhaeng: SageokSuriOhaeng): ScoreDetail {
        // 균형도는 점수 계산에만 사용
        val balanceScore = OhaengRelationUtil.calculateBalanceScore(sageokSuriOhaeng.ohaengDistribution) * 0.5

        // 배열 상생상극 점수
        val arrayScore = OhaengRelationUtil.calculateArrayScore(sageokSuriOhaeng.arrangement) * 0.5

        // 상극이 없으면 통과 (균형은 체크하지 않음)
        val hasNoSangGeuk = !hasAnySangGeuk(sageokSuriOhaeng.arrangement)
        val isPassed = hasNoSangGeuk  // 균형 체크 제거

        val totalScore = (balanceScore + arrayScore).toInt()

        return ScoreDetail(
            score = totalScore,
            maxScore = 100,
            reason = "오행균형(${balanceScore.toInt()}/50), 배열조화(${arrayScore.toInt()}/50)",
            isPassed = isPassed
        )
    }

    // 상극 체크 헬퍼 메서드 수정
    private fun hasAnySangGeuk(arrangement: List<String>): Boolean {
        for (i in 0 until arrangement.size - 1) {
            // 같은 오행 연속은 상극으로 보지 않음
            if (arrangement[i] == arrangement[i + 1]) {
                continue
            }
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return true
            }
        }
        return false
    }

    // 3. 사격수리음양 - isPassed 추가
    private fun calculateSageokSuriEumYangScore(sageokSuriEumYang: SageokSuriEumYang): ScoreDetail {
        val total = sageokSuriEumYang.eumCount + sageokSuriEumYang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "데이터 없음", false)

        // 기존 점수 계산...
        val ratio = minOf(sageokSuriEumYang.eumCount, sageokSuriEumYang.yangCount).toDouble() / total
        val ratioScore = when {
            ratio >= 0.4 -> 50
            ratio >= 0.3 -> 35
            ratio >= 0.2 -> 20
            else -> 10
        }

        // isPassed: 음양 비율이 적절하면 통과 (2:8 이상)
        val isPassed = ratio >= 0.2

        val totalScore = ratioScore + 50 // 간단히 처리

        return ScoreDetail(
            score = totalScore,
            maxScore = 100,
            reason = "음${sageokSuriEumYang.eumCount}:양${sageokSuriEumYang.yangCount}",
            isPassed = isPassed
        )
    }

    // 4. 획수오행 - isPassed 추가
    private fun calculateHoeksuOhaengScore(hoeksuOhaeng: HoeksuOhaeng, nameInput: NameInput): ScoreDetail {
        val balanceScore = OhaengRelationUtil.calculateBalanceScore(hoeksuOhaeng.ohaengDistribution) * 0.5
        val arrayScore = OhaengRelationUtil.calculateArrayScore(hoeksuOhaeng.arrangement) * 0.5

        // 성씨 길이 계산
        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        // 상생 체크 (상극이 없어야 통과)
        val isPassed = PassFailUtil.checkOhaengSangSaeng(hoeksuOhaeng.arrangement, surnameLength)

        val totalScore = (balanceScore + arrayScore).toInt()

        return ScoreDetail(
            score = totalScore,
            maxScore = 100,
            reason = "획수오행 균형도: ${balanceScore.toInt()}/50, 배열: ${hoeksuOhaeng.arrangement.joinToString("-")}",
            isPassed = isPassed
        )
    }

    // 5. 획수음양 - isPassed 추가
    private fun calculateHoeksuEumYangScore(hoeksuEumYang: HoeksuEumYang, nameInput: NameInput): ScoreDetail {
        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        return calculateEumYangScoreWithPass(
            hoeksuEumYang.eumCount,
            hoeksuEumYang.yangCount,
            hoeksuEumYang.arrangement,
            surnameLength,
            "획수음양"
        )
    }

    // 6. 발음오행 - isPassed 추가
    private fun calculateBaleumOhaengScore(baleumOhaeng: BaleumOhaeng, nameInput: NameInput): ScoreDetail {
        val balanceScore = OhaengRelationUtil.calculateBalanceScore(baleumOhaeng.ohaengDistribution) * 0.5
        val arrayScore = OhaengRelationUtil.calculateArrayScore(baleumOhaeng.arrangement) * 0.5

        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        // 상생 체크
        val isPassed = PassFailUtil.checkOhaengSangSaeng(baleumOhaeng.arrangement, surnameLength)

        val totalScore = (balanceScore + arrayScore).toInt()

        return ScoreDetail(
            score = totalScore,
            maxScore = 100,
            reason = "발음오행 균형도: ${balanceScore.toInt()}/50, 배열: ${baleumOhaeng.arrangement.joinToString("-")}",
            isPassed = isPassed
        )
    }

    // 7. 발음음양 - isPassed 추가
    private fun calculateBaleumEumYangScore(baleumEumYang: BaleumEumYang, nameInput: NameInput): ScoreDetail {
        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        return calculateEumYangScoreWithPass(
            baleumEumYang.eumCount,
            baleumEumYang.yangCount,
            baleumEumYang.arrangement,
            surnameLength,
            "발음음양"
        )
    }

    // 8. 사주이름오행 점수 계산 - 올바른 균등분배 확인
    private fun calculateSajuNameOhaengScore(
        sajuOhaeng: SajuOhaeng,
        sajuNameOhaeng: SajuNameOhaeng,
        jawonOhaeng: JawonOhaeng
    ): ScoreDetail {
        // 자원오행이 실제로 사주의 부족한 오행을 보완하는지 확인
        val sajuZeroOhaengs = sajuOhaeng.ohaengDistribution.filter { it.value == 0 }.keys
        val finalZeroOhaengs = sajuNameOhaeng.ohaengDistribution.filter { it.value == 0 }.keys

        // 자원오행으로 0이 줄어든 개수
        val zeroReduction = sajuZeroOhaengs.size - finalZeroOhaengs.size

        // 자원오행이 0인 오행을 가지고 있는 개수
        val jawonForZero = sajuZeroOhaengs.count { zeroOhaeng ->
            (jawonOhaeng.ohaengDistribution[zeroOhaeng] ?: 0) > 0
        }

        // isPassed: 자원오행이 0인 오행을 보완할 수 있는 만큼 보완했는지
        val isPassed = when {
            sajuZeroOhaengs.isEmpty() -> true  // 0인 오행이 없으면 통과
            jawonForZero == 0 -> true  // 보완할 수 있는 자원오행이 없으면 어쩔 수 없이 통과
            else -> zeroReduction == jawonForZero  // 보완 가능한 만큼 보완했으면 통과
        }

        // 점수 계산
        val totalBalanceScore = OhaengRelationUtil.calculateBalanceScore(sajuNameOhaeng.ohaengDistribution)
        val complementScore = if (sajuZeroOhaengs.isNotEmpty() && jawonForZero > 0) {
            (zeroReduction.toDouble() / jawonForZero * 50).toInt()
        } else {
            30
        }

        val finalScore = (complementScore + totalBalanceScore * 0.5).toInt().coerceIn(0, 100)

        val reason = buildString {
            if (sajuZeroOhaengs.isNotEmpty()) {
                append("0인 오행(${sajuZeroOhaengs.joinToString(",")}) ")
                append("중 ${zeroReduction}개 보완")
            } else {
                append("0인 오행 없음")
            }
            append(", 균형도: ${totalBalanceScore}점")
        }

        return ScoreDetail(
            score = finalScore,
            maxScore = 100,
            reason = reason,
            isPassed = isPassed
        )
    }

    // 9. 자원오행 점수 계산 - isPassed 추가
    private fun calculateJawonOhaengScore(sajuOhaeng: SajuOhaeng, jawonOhaeng: JawonOhaeng): ScoreDetail {
        // 사주의 약한 오행을 보완하는 정도 평가
        val sajuValues = sajuOhaeng.ohaengDistribution.values
        val sajuAvg = sajuValues.average()

        var score = 70 // 기본 점수
        val reasons = mutableListOf<String>()
        var complementedWeak = 0

        // 사주에서 평균 이하인 오행 찾기
        val weakOhaengs = mutableListOf<String>()
        sajuOhaeng.ohaengDistribution.forEach { (ohaeng, count) ->
            if (count < sajuAvg) {
                weakOhaengs.add(ohaeng)
                val jawonCount = jawonOhaeng.ohaengDistribution[ohaeng] ?: 0
                if (jawonCount > 0) {
                    score += 10
                    complementedWeak++
                    reasons.add("${ohaeng} 보완")
                }
            }
        }

        // 자원오행 자체의 균형도
        val jawonBalance = if (jawonOhaeng.ohaengDistribution.values.sum() > 0) {
            val distribution = jawonOhaeng.ohaengDistribution.values
            val max = distribution.maxOrNull() ?: 0
            val min = distribution.minOrNull() ?: 0
            if (max - min <= 1) 10 else 0
        } else 0

        score = (score + jawonBalance).coerceIn(0, 100)

        // isPassed: 약한 오행의 50% 이상을 보완하면 통과
        val isPassed = if (weakOhaengs.isEmpty()) {
            true  // 약한 오행이 없으면 자동 통과
        } else {
            complementedWeak >= (weakOhaengs.size + 1) / 2  // 50% 이상 보완
        }

        return ScoreDetail(
            score = score,
            maxScore = 100,
            reason = if (reasons.isNotEmpty()) reasons.joinToString(", ") else "자원오행 기본점수",
            isPassed = isPassed
        )
    }

    private fun calculateSajuEumYangScore(sajuEumYang: SajuEumYang): ScoreDetail {
        val total = sajuEumYang.eumCount + sajuEumYang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "사주 데이터 없음", false)

        // 음양 비율 계산
        val ratio = minOf(sajuEumYang.eumCount, sajuEumYang.yangCount).toDouble() / total

        // 점수 계산 (균형잡힌 비율일수록 높은 점수)
        val score = when {
            ratio >= 0.4 -> 100  // 4:6 ~ 6:4 완벽한 균형
            ratio >= 0.375 -> 90  // 3:5 ~ 5:3
            ratio >= 0.25 -> 70   // 2:6 ~ 6:2
            ratio >= 0.125 -> 50  // 1:7 ~ 7:1
            else -> 30           // 0:8 ~ 8:0 극단적 불균형
        }

        // isPassed: 음양이 2:6 이상의 비율이면 통과
        val isPassed = ratio >= 0.25

        val reason = "사주음양 - 음${sajuEumYang.eumCount}:양${sajuEumYang.yangCount} (${String.format("%.0f", ratio * 100)}:${String.format("%.0f", (1-ratio) * 100)})"

        return ScoreDetail(
            score = score,
            maxScore = 100,
            reason = reason,
            isPassed = isPassed
        )
    }

    // 음양 점수 계산 공통 메서드 - Pass/Fail 포함
    private fun calculateEumYangScoreWithPass(
        eumCount: Int,
        yangCount: Int,
        arrangement: List<String>,
        surnameLength: Int,
        prefix: String
    ): ScoreDetail {
        val total = eumCount + yangCount
        if (total == 0) return ScoreDetail(0, 100, "$prefix 데이터 없음", false)

        // 기존 점수 계산
        val ratio = minOf(eumCount, yangCount).toDouble() / total
        val ratioScore = when {
            ratio >= 0.4 -> 50
            ratio >= 0.3 -> 35
            ratio >= 0.2 -> 20
            else -> 10
        }

        // Pass/Fail 판정
        val isPassed = PassFailUtil.checkEumYangHarmony(arrangement, surnameLength)

        var arrayScore = 40
        // 기존 배열 점수 계산...

        val totalScore = (ratioScore + arrayScore).coerceIn(0, 100)

        return ScoreDetail(
            score = totalScore,
            maxScore = 100,
            reason = "$prefix - 음$eumCount:양$yangCount, 배열: ${arrangement.joinToString("")}",
            isPassed = isPassed
        )
    }
}
