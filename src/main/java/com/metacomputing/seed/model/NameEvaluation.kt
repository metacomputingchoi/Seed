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
    val baleumEumYang: BaleumEumYang
)

// 사격수리 - 원형이정
data class SageokSuri(
    val wonGyeok: Int,          // 원격(元格) - 성씨 획수
    val wonGyeokFortune: String,
    val hyeongGyeok: Int,       // 형격(亨格) - 이름 첫 글자 획수
    val hyeongGyeokFortune: String,
    val iGyeok: Int?,           // 이격(利格) - 이름 둘째 글자 획수 (두 글자 이름일 경우 nullable)
    val iGyeokFortune: String?,
    val jeongGyeok: Int,        // 정격(貞格) - 총 획수
    val jeongGyeokFortune: String
)

// 사격수리 오행
data class SageokSuriOheng(
    val ohengDistribution: Map<String, Int>  // 목, 화, 토, 금, 수의 분포
)

// 사격수리 음양
data class SageokSuriEumYang(
    val eumCount: Int,
    val yangCount: Int
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
    val ohengDistribution: Map<String, Int>
)

// 획수 음양
data class HoeksuEumYang(
    val eumCount: Int,
    val yangCount: Int
)

// 발음 오행
data class BaleumOheng(
    val ohengDistribution: Map<String, Int>
)

// 발음 음양
data class BaleumEumYang(
    val eumCount: Int,
    val yangCount: Int
)
