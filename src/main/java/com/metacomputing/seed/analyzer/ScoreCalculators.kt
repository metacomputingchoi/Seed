// analyzer/ScoreCalculators.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.*

interface ScoreCalculator {
    fun calculate(): ScoreDetail
}

class SageokSuriScoreCalculator(private val sageokSuri: SageokSuri) : ScoreCalculator {
    override fun calculate(): ScoreDetail {
        val fortunes = listOf(
            sageokSuri.wonGyeokFortune, sageokSuri.hyeongGyeokFortune,
            sageokSuri.iGyeokFortune, sageokSuri.jeongGyeokFortune
        )

        val scores = fortunes.map { fortune ->
            when {
                fortune.contains("최상운수") -> 25
                fortune.contains("상운수") -> 20
                fortune.contains("양운수") -> 15
                fortune.contains("흉운수") && !fortune.contains("최흉운수") -> 5
                fortune.contains("최흉운수") -> 0
                else -> 10
            }
        }

        return ScoreDetail(
            scores.sum(), 100,
            "원격: ${sageokSuri.wonGyeokFortune}, 형격: ${sageokSuri.hyeongGyeokFortune}, " +
            "이격: ${sageokSuri.iGyeokFortune}, 정격: ${sageokSuri.jeongGyeokFortune}",
            scores.count { it >= 15 } == 4
        )
    }
}

class OhaengScoreCalculator(
    private val ohaeng: OhaengData,
    private val surnameLength: Int,
    private val prefix: String,
    private val checkType: String
) : ScoreCalculator {
    override fun calculate(): ScoreDetail {
        val balance = OhaengRelationUtil.calculateBalanceScore(ohaeng.ohaengDistribution) * 0.5
        val array = OhaengRelationUtil.calculateArrayScore(ohaeng.arrangement) * 0.5

        val isPassed = when (checkType) {
            "sageok" -> PassFailUtil.checkSageokSuriOhaeng(ohaeng.arrangement)
            else -> PassFailUtil.checkOhaengSangSaeng(ohaeng.arrangement, surnameLength)
        }

        return ScoreDetail(
            (balance + array).toInt(), 100,
            "$prefix 균형도: ${balance.toInt()}/50, 배열: ${ohaeng.arrangement.joinToString("-")}",
            isPassed
        )
    }
}

class EumYangScoreCalculator(
    private val eumyang: EumYangData,
    private val surnameLength: Int,
    private val prefix: String
) : ScoreCalculator {
    override fun calculate(): ScoreDetail {
        val total = eumyang.eumCount + eumyang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "$prefix 데이터 없음", false)

        val ratio = minOf(eumyang.eumCount, eumyang.yangCount).toDouble() / total
        val ratioScore = when {
            ratio >= 0.4 -> 50
            ratio >= 0.3 -> 35
            ratio >= 0.2 -> 20
            else -> 10
        }

        val isPassed = PassFailUtil.checkEumYangHarmony(eumyang.arrangement, surnameLength)

        return ScoreDetail(
            (ratioScore + 40).coerceIn(0, 100), 100,
            "$prefix - 음${eumyang.eumCount}:양${eumyang.yangCount}, 배열: ${eumyang.arrangement.joinToString("")}",
            isPassed
        )
    }
}

class SajuNameOhaengScoreCalculator(
    private val sajuOhaeng: OhaengData,
    private val sajuNameOhaeng: OhaengData,
    private val jawonOhaeng: OhaengData
) : ScoreCalculator {
    override fun calculate(): ScoreDetail {
        val sajuZero = sajuOhaeng.ohaengDistribution.filter { it.value == 0 }.keys
        val finalZero = sajuNameOhaeng.ohaengDistribution.filter { it.value == 0 }.keys
        val zeroReduction = sajuZero.size - finalZero.size
        val jawonForZero = sajuZero.count { (jawonOhaeng.ohaengDistribution[it] ?: 0) > 0 }

        val isPassed = sajuZero.isEmpty() || jawonForZero == 0 || zeroReduction == jawonForZero
        val balance = OhaengRelationUtil.calculateBalanceScore(sajuNameOhaeng.ohaengDistribution)
        val complementScore = if (sajuZero.isNotEmpty() && jawonForZero > 0) {
            (zeroReduction.toDouble() / jawonForZero * 50).toInt()
        } else 30

        val score = (complementScore + balance * 0.5).toInt().coerceIn(0, 100)
        val reason = if (sajuZero.isNotEmpty()) {
            "0인 오행(${sajuZero.joinToString(",")}) 중 ${zeroReduction}개 보완, 균형도: ${balance}점"
        } else "0인 오행 없음, 균형도: ${balance}점"

        return ScoreDetail(score, 100, reason, isPassed)
    }
}