// model/DataClasses.kt
package com.metacomputing.seed.model

import com.metacomputing.mcalendar.TimePointResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

data class NameInput(
    val surname: String, val surnameHanja: String,
    val givenName: String, val givenNameHanja: String,
    val timePointResult: TimePointResult
)

data class NameEvaluation(
    val totalScore: Int, val detailedScores: DetailedScores,
    val sageokSuri: SageokSuri, val sageokSuriOhaeng: OhaengData,
    val sageokSuriEumYang: EumYangData, val sajuOhaeng: OhaengData,
    val sajuEumYang: EumYangData, val hoeksuOhaeng: OhaengData,
    val hoeksuEumYang: EumYangData, val baleumOhaeng: OhaengData,
    val baleumEumYang: EumYangData, val sajuNameOhaeng: OhaengData,
    val jawonOhaeng: OhaengData
)

data class DetailedScores(
    val sageokSuriScore: ScoreDetail, val sageokSuriOhaengScore: ScoreDetail,
    val sageokSuriEumYangScore: ScoreDetail, val sajuEumYangScore: ScoreDetail,
    val hoeksuOhaengScore: ScoreDetail, val hoeksuEumYangScore: ScoreDetail,
    val baleumOhaengScore: ScoreDetail, val baleumEumYangScore: ScoreDetail,
    val sajuNameOhaengScore: ScoreDetail, val jawonOhaengScore: ScoreDetail
)

data class ScoreDetail(val score: Int, val maxScore: Int, val reason: String, val isPassed: Boolean)

data class SageokSuri(
    val wonGyeok: Int, val wonGyeokFortune: String, val wonGyeokMeaning: String,
    val hyeongGyeok: Int, val hyeongGyeokFortune: String, val hyeongGyeokMeaning: String,
    val iGyeok: Int, val iGyeokFortune: String, val iGyeokMeaning: String,
    val jeongGyeok: Int, val jeongGyeokFortune: String, val jeongGyeokMeaning: String
)

data class OhaengData(
    val ohaengDistribution: Map<String, Int>,
    val arrangement: List<String> = emptyList()
)

data class EumYangData(
    val eumCount: Int, val yangCount: Int,
    val arrangement: List<String> = emptyList()
)

data class SajuInfo(
    val yearStem: String, val yearBranch: String,
    val monthStem: String, val monthBranch: String,
    val dayStem: String, val dayBranch: String,
    val hourStem: String, val hourBranch: String
)

data class NameBlock(val korean: String, val hanja: String) {
    val isKoreanEmpty = korean == "_" || korean.isEmpty()
    val isHanjaEmpty = hanja == "_" || hanja.isEmpty()
    val isCompleteKorean = korean.length == 1 && korean[0] in '가'..'힣'
    val isChosungOnly = korean.length == 1 && korean[0] in "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"
    val isJungsungOnly = korean.length == 1 && korean[0] in "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
}

data class NameQuery(val surnameBlocks: List<NameBlock>, val nameBlocks: List<NameBlock>)

@Serializable
data class HanjaInfo(
    @SerialName("char_ko") val charKo: String,
    @SerialName("hanja") val hanja: String,
    @SerialName("meaning") val meaning: String,
    @SerialName("strokes") val strokes: String,
    @SerialName("stroke_element") val strokeElement: String,
    @SerialName("radical") val radical: String,
    @SerialName("source_element") val sourceElement: String
)

@Serializable
data class StrokeData(@SerialName("stroke_meanings") val strokeMeanings: Map<String, StrokeMeaning>)

@Serializable
data class StrokeMeaning(
    val number: Int, val title: String, val summary: String,
    @SerialName("detailed_explanation") val detailedExplanation: String,
    @SerialName("positive_aspects") val positiveAspects: String,
    @SerialName("caution_points") val cautionPoints: String,
    @SerialName("personality_traits") val personalityTraits: List<String>,
    @SerialName("suitable_career") val suitableCareer: List<String>,
    @SerialName("life_period_influence") val lifePeriodInfluence: String?,
    @SerialName("special_characteristics") val specialCharacteristics: String?,
    @SerialName("challenge_period") val challengePeriod: String?,
    @SerialName("opportunity_area") val opportunityArea: String?,
    @SerialName("lucky_level") val luckyLevel: String
)

@Serializable
data class NameStatistics(
    @SerialName("similar_names") val similarNames: List<String> = emptyList(),
    @SerialName("연도별 인기 순위") val yearlyPopularityRank: YearlyData = YearlyData(),
    @SerialName("연도별 출생아 수") val yearlyBirthCount: YearlyData = YearlyData(),
    @SerialName("hanja_combinations") val hanjaCombinations: List<String> = emptyList()
)

@Serializable
data class YearlyData(
    @SerialName("전체") val total: Map<String, Int> = emptyMap(),
    @SerialName("남자") val male: Map<String, Int> = emptyMap(),
    @SerialName("여자") val female: Map<String, Int> = emptyMap()
)

data class PopularityAnalysis(
    val highestRank: Int, val highestRankYear: String,
    val recentRank: Int, val recentYear: String, val trend: String
)

data class GenderDistribution(
    val malePercentage: Double, val femalePercentage: Double,
    val genderCharacteristic: String
)

data class BirthTrend(
    val totalBirths: Int, val averagePerYear: Double, val trend: String
)

data class NameStatisticsResult(
    val name: String, val popularity: PopularityAnalysis,
    val genderDistribution: GenderDistribution, val birthTrend: BirthTrend,
    val hanjaCombinations: List<String>
)