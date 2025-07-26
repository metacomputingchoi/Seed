// model/NameEvaluation.kt
package com.metacomputing.seed.model

data class NameEvaluation(
    val totalScore: Int,
    val sageokSuri: SageokSuri,
    val sageokSuriOhaeng: SageokSuriOhaeng,
    val sageokSuriEumYang: SageokSuriEumYang,
    val sajuOhaeng: SajuOhaeng,
    val sajuEumYang: SajuEumYang,
    val hoeksuOhaeng: HoeksuOhaeng,
    val hoeksuEumYang: HoeksuEumYang,
    val baleumOhaeng: BaleumOhaeng,
    val baleumEumYang: BaleumEumYang,
    val sajuNameOhaeng: SajuNameOhaeng,
    val jawonOhaeng: JawonOhaeng
)

data class SageokSuri(
    val wonGyeok: Int,
    val wonGyeokFortune: String,
    val wonGyeokMeaning: String,
    val hyeongGyeok: Int,
    val hyeongGyeokFortune: String,
    val hyeongGyeokMeaning: String,
    val iGyeok: Int,
    val iGyeokFortune: String,
    val iGyeokMeaning: String,
    val jeongGyeok: Int,
    val jeongGyeokFortune: String,
    val jeongGyeokMeaning: String
)

data class SageokSuriOhaeng(
    val ohaengDistribution: Map<String, Int>,
    val arrangement: List<String>
)

data class SageokSuriEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>
)

data class SajuOhaeng(
    val ohaengDistribution: Map<String, Int>
)

data class SajuEumYang(
    val eumCount: Int,
    val yangCount: Int
)

data class HoeksuOhaeng(
    val ohaengDistribution: Map<String, Int>,
    val arrangement: List<String>
)

data class HoeksuEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>
)

data class BaleumOhaeng(
    val ohaengDistribution: Map<String, Int>,
    val arrangement: List<String>
)

data class BaleumEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>
)

data class SajuNameOhaeng(
    val ohaengDistribution: Map<String, Int>
)

data class JawonOhaeng(
    val ohaengDistribution: Map<String, Int>
)
