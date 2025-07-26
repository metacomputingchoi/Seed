// model/StatisticsAnalysisResult.kt
package com.metacomputing.seed.model

data class PopularityAnalysis(
    val highestRank: Int,
    val highestRankYear: String,
    val recentRank: Int,
    val recentYear: String,
    val trend: String
)

data class GenderDistribution(
    val malePercentage: Double,
    val femalePercentage: Double,
    val genderCharacteristic: String
)

data class BirthTrend(
    val totalBirths: Int,
    val averagePerYear: Double,
    val trend: String
)
