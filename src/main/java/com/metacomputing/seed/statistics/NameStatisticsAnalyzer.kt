package com.metacomputing.seed.statistics

import com.metacomputing.seed.model.*
import kotlin.math.roundToInt

class NameStatisticsAnalyzer {

    fun analyzePopularity(stats: NameStatistics): PopularityAnalysis {
        val allRanks = stats.yearlyPopularityRank.total

        if (allRanks.isEmpty()) {
            return PopularityAnalysis(
                highestRank = 0,
                highestRankYear = "없음",
                recentRank = 0,
                recentYear = "없음",
                trend = "데이터 없음"
            )
        }

        // 최고 순위 찾기 (낮은 숫자가 높은 순위)
        val bestEntry = allRanks.minByOrNull { it.value }!!

        // 최근 순위 찾기
        val recentEntry = allRanks.maxByOrNull { it.key }!!

        // 추세 분석
        val trend = analyzeTrend(allRanks)

        return PopularityAnalysis(
            highestRank = bestEntry.value,
            highestRankYear = bestEntry.key,
            recentRank = recentEntry.value,
            recentYear = recentEntry.key,
            trend = trend
        )
    }

    fun analyzeGenderDistribution(stats: NameStatistics): GenderDistribution {
        val maleBirths = stats.yearlyBirthCount.male.values.sum()
        val femaleBirths = stats.yearlyBirthCount.female.values.sum()
        val totalBirths = maleBirths + femaleBirths

        if (totalBirths == 0) {
            return GenderDistribution(
                malePercentage = 0.0,
                femalePercentage = 0.0,
                genderCharacteristic = "데이터 없음"
            )
        }

        val malePercentage = (maleBirths.toDouble() / totalBirths * 100).roundToInt().toDouble()
        val femalePercentage = (femaleBirths.toDouble() / totalBirths * 100).roundToInt().toDouble()

        val characteristic = when {
            malePercentage >= 80 -> "남성형"
            femalePercentage >= 80 -> "여성형"
            malePercentage in 40.0..60.0 && femalePercentage in 40.0..60.0 -> "중성형"
            malePercentage > femalePercentage -> "남성 선호형"
            else -> "여성 선호형"
        }

        return GenderDistribution(
            malePercentage = malePercentage,
            femalePercentage = femalePercentage,
            genderCharacteristic = characteristic
        )
    }

    fun analyzeBirthTrend(stats: NameStatistics): BirthTrend {
        val birthsByYear = stats.yearlyBirthCount.total
        val totalBirths = birthsByYear.values.sum()
        val yearCount = birthsByYear.size

        if (yearCount == 0) {
            return BirthTrend(
                totalBirths = 0,
                averagePerYear = 0.0,
                trend = "데이터 없음"
            )
        }

        val averagePerYear = totalBirths.toDouble() / yearCount
        val trend = analyzeBirthTrend(birthsByYear)

        return BirthTrend(
            totalBirths = totalBirths,
            averagePerYear = averagePerYear,
            trend = trend
        )
    }

    private fun analyzeTrend(yearlyData: Map<String, Int>): String {
        if (yearlyData.size < 2) return "데이터 부족"

        val sortedYears = yearlyData.keys.sorted()
        val recentYears = sortedYears.takeLast(3)

        if (recentYears.size < 2) return "변동"

        val recentValues = recentYears.map { yearlyData[it]!! }

        return when {
            recentValues.zipWithNext().all { (a, b) -> a > b } -> "상승"  // 순위가 낮아짐 = 인기 상승
            recentValues.zipWithNext().all { (a, b) -> a < b } -> "하락"
            recentValues.maxOrNull()!! - recentValues.minOrNull()!! < 100 -> "유지"
            else -> "변동"
        }
    }

    private fun analyzeBirthTrend(yearlyData: Map<String, Int>): String {
        if (yearlyData.size < 2) return "데이터 부족"

        val sortedYears = yearlyData.keys.sorted()
        val recentYears = sortedYears.takeLast(3)

        if (recentYears.size < 2) return "안정"

        val recentValues = recentYears.map { yearlyData[it]!! }

        return when {
            recentValues.zipWithNext().all { (a, b) -> a < b } -> "증가"
            recentValues.zipWithNext().all { (a, b) -> a > b } -> "감소"
            else -> "안정"
        }
    }
}
