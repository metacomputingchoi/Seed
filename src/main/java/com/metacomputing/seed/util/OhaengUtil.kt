package com.metacomputing.seed.util

object OhaengUtil {
    // 천간의 오행
    val HEAVENLY_STEM_OHENG = mapOf(
        "甲" to "목", "乙" to "목",
        "丙" to "화", "丁" to "화",
        "戊" to "토", "己" to "토",
        "庚" to "금", "辛" to "금",
        "壬" to "수", "癸" to "수"
    )

    // 지지의 오행
    val EARTHLY_BRANCH_OHENG = mapOf(
        "子" to "수", "丑" to "토", "寅" to "목", "卯" to "목",
        "辰" to "토", "巳" to "화", "午" to "화", "未" to "토",
        "申" to "금", "酉" to "금", "戌" to "토", "亥" to "수"
    )

    // 지지의 지장간 (본기, 중기, 여기)
    val EARTHLY_BRANCH_HIDDEN_STEMS = mapOf(
        "子" to listOf("癸"),           // 수
        "丑" to listOf("己", "癸", "辛"), // 토, 수, 금
        "寅" to listOf("甲", "丙", "戊"), // 목, 화, 토
        "卯" to listOf("乙"),           // 목
        "辰" to listOf("戊", "乙", "癸"), // 토, 목, 수
        "巳" to listOf("丙", "戊", "庚"), // 화, 토, 금
        "午" to listOf("丁", "己"),      // 화, 토
        "未" to listOf("己", "丁", "乙"), // 토, 화, 목
        "申" to listOf("庚", "壬", "戊"), // 금, 수, 토
        "酉" to listOf("辛"),           // 금
        "戌" to listOf("戊", "辛", "丁"), // 토, 금, 화
        "亥" to listOf("壬", "甲")       // 수, 목
    )

    // 지지의 지장간을 오행으로 변환
    fun getHiddenStemOhaeng(branch: String): List<String> {
        val hiddenStems = EARTHLY_BRANCH_HIDDEN_STEMS[branch] ?: emptyList()
        return hiddenStems.map { stem -> HEAVENLY_STEM_OHENG[stem] ?: "토" }
    }

    // 획수별 오행
    fun getOhaengByStroke(stroke: Int): String {
        val lastDigit = stroke % 10
        return when (lastDigit) {
            1, 2 -> "목"
            3, 4 -> "화"
            5, 6 -> "토"
            7, 8 -> "금"
            9, 0 -> "수"
            else -> "토"
        }
    }
}
