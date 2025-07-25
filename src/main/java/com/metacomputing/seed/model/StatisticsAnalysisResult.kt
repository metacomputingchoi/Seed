package com.metacomputing.seed.model

data class PopularityAnalysis(
    val highestRank: Int,
    val highestRankYear: String,
    val recentRank: Int,
    val recentYear: String,
    val trend: String  // "상승", "하락", "유지", "변동"
)

data class GenderDistribution(
    val malePercentage: Double,
    val femalePercentage: Double,
    val genderCharacteristic: String  // "남성형", "여성형", "중성형"
)

data class BirthTrend(
    val totalBirths: Int,
    val averagePerYear: Double,
    val trend: String  // "증가", "감소", "안정"
)
