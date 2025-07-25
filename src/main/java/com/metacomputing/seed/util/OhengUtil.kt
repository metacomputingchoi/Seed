package com.metacomputing.seed.util

object OhengUtil {
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

    // 획수별 오행
    fun getOhengByStroke(stroke: Int): String {
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
