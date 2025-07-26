// statistics/NameStatisticsManager.kt
package com.metacomputing.seed.statistics

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.ResourceLoader
import kotlin.math.roundToInt

class NameStatisticsManager {
    private val statistics: Map<String, NameStatistics> by lazy {
        try {
            ResourceLoader.loadStatistics("name_to_stat_minified.json")
        } catch (e: Exception) {
            println("통계 파일 로드 실패: ${e.message}")
            emptyMap()
        }
    }

    fun loadStatistics() = statistics
    fun getAllStatistics() = statistics

    fun getStatistics(givenName: String): NameStatisticsResult? {
        val stats = statistics[givenName] ?: return null

        return NameStatisticsResult(
            givenName,
            analyzePopularity(stats),
            analyzeGenderDistribution(stats),
            analyzeBirthTrend(stats),
            stats.hanjaCombinations
        )
    }

    fun isValidHanjaCombination(givenName: String, givenNameHanja: String): Boolean {
        val stats = statistics[givenName] ?: return true
        return stats.hanjaCombinations.isEmpty() || givenNameHanja in stats.hanjaCombinations
    }

    fun getValidHanjaCombinations(givenName: String) =
        statistics[givenName]?.hanjaCombinations ?: emptyList()

    fun analyzePopularity(stats: NameStatistics): PopularityAnalysis {
        val ranks = stats.yearlyPopularityRank.total

        if (ranks.isEmpty()) {
            return PopularityAnalysis(0, "없음", 0, "없음", "데이터 없음")
        }

        val best = ranks.minByOrNull { it.value }!!
        val recent = ranks.maxByOrNull { it.key }!!

        return PopularityAnalysis(
            best.value, best.key,
            recent.value, recent.key,
            analyzeTrend(ranks)
        )
    }

    fun analyzeGenderDistribution(stats: NameStatistics): GenderDistribution {
        val male = stats.yearlyBirthCount.male.values.sum()
        val female = stats.yearlyBirthCount.female.values.sum()
        val total = male + female

        if (total == 0) {
            return GenderDistribution(0.0, 0.0, "데이터 없음")
        }

        val malePercent = (male.toDouble() / total * 100).roundToInt().toDouble()
        val femalePercent = (female.toDouble() / total * 100).roundToInt().toDouble()

        val characteristic = when {
            malePercent >= 80 -> "남성형"
            femalePercent >= 80 -> "여성형"
            malePercent in 40.0..60.0 -> "중성형"
            malePercent > femalePercent -> "남성 선호형"
            else -> "여성 선호형"
        }

        return GenderDistribution(malePercent, femalePercent, characteristic)
    }

    fun analyzeBirthTrend(stats: NameStatistics): BirthTrend {
        val births = stats.yearlyBirthCount.total
        val total = births.values.sum()
        val years = births.size

        if (years == 0) {
            return BirthTrend(0, 0.0, "데이터 없음")
        }

        return BirthTrend(total, total.toDouble() / years, analyzeBirthTrend(births))
    }

    private fun analyzeTrend(data: Map<String, Int>): String {
        if (data.size < 2) return "데이터 부족"

        val recent = data.keys.sorted().takeLast(3)
        if (recent.size < 2) return "변동"

        val values = recent.map { data[it]!! }
        return when {
            values.zipWithNext().all { (a, b) -> a > b } -> "상승"
            values.zipWithNext().all { (a, b) -> a < b } -> "하락"
            values.maxOrNull()!! - values.minOrNull()!! < 100 -> "유지"
            else -> "변동"
        }
    }

    private fun analyzeBirthTrend(data: Map<String, Int>): String {
        if (data.size < 2) return "데이터 부족"

        val recent = data.keys.sorted().takeLast(3)
        if (recent.size < 2) return "안정"

        val values = recent.map { data[it]!! }
        return when {
            values.zipWithNext().all { (a, b) -> a < b } -> "증가"
            values.zipWithNext().all { (a, b) -> a > b } -> "감소"
            else -> "안정"
        }
    }
}