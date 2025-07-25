package com.metacomputing.seed.model

data class NameEvaluation(
    val totalScore: Int,
    val sageokSuri: SageokSuri,
    val sageokSuriOheng: SageokSuriOheng,
    val sageokSuriEumYang: SageokSuriEumYang,
    val sajuOheng: SajuOheng,
    val sajuEumYang: SajuEumYang,
    val hoeksuOheng: HoeksuOheng,
    val hoeksuEumYang: HoeksuEumYang,
    val baleumOheng: BaleumOheng,
    val baleumEumYang: BaleumEumYang,
    val sajuNameOheng: SajuNameOheng  // 사주이름오행 추가
)

// 사격수리 - 원형이정
data class SageokSuri(
    val wonGyeok: Int,          // 원격(元格)
    val wonGyeokFortune: String,
    val wonGyeokMeaning: String,
    val hyeongGyeok: Int,       // 형격(亨格)
    val hyeongGyeokFortune: String,
    val hyeongGyeokMeaning: String,
    val iGyeok: Int,            // 이격(利格)
    val iGyeokFortune: String,
    val iGyeokMeaning: String,
    val jeongGyeok: Int,        // 정격(貞格)
    val jeongGyeokFortune: String,
    val jeongGyeokMeaning: String
)

// 사격수리 오행
data class SageokSuriOheng(
    val ohengDistribution: Map<String, Int>,  // 목, 화, 토, 금, 수의 분포
    val arrangement: List<String>  // 이격-형격-원격 순서
)

// 사격수리 음양
data class SageokSuriEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>  // 이격-형격-원격 순서
)

// 사주 오행
data class SajuOheng(
    val ohengDistribution: Map<String, Int>
)

// 사주 음양
data class SajuEumYang(
    val eumCount: Int,
    val yangCount: Int
)

// 획수 오행
data class HoeksuOheng(
    val ohengDistribution: Map<String, Int>,
    val arrangement: List<String>  // 성명 순서
)

// 획수 음양
data class HoeksuEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>  // 성명 순서
)

// 발음 오행
data class BaleumOheng(
    val ohengDistribution: Map<String, Int>,
    val arrangement: List<String>  // 성명 순서
)

// 발음 음양
data class BaleumEumYang(
    val eumCount: Int,
    val yangCount: Int,
    val arrangement: List<String>  // 성명 순서
)

// 사주이름오행 (사주 + 이름 획수오행)
data class SajuNameOheng(
    val ohengDistribution: Map<String, Int>
)
