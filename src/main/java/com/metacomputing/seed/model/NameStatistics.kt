// model/NameStatistics.kt
package com.metacomputing.seed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class NameStatistics(
    @SerialName("similar_names")
    val similarNames: List<String> = emptyList(),

    @SerialName("연도별 인기 순위")
    val yearlyPopularityRank: YearlyData = YearlyData(),

    @SerialName("연도별 출생아 수")
    val yearlyBirthCount: YearlyData = YearlyData(),

    @SerialName("hanja_combinations")
    val hanjaCombinations: List<String> = emptyList()
)

@Serializable
data class YearlyData(
    @SerialName("전체")
    val total: Map<String, Int> = emptyMap(),

    @SerialName("남자")
    val male: Map<String, Int> = emptyMap(),

    @SerialName("여자")
    val female: Map<String, Int> = emptyMap()
)
