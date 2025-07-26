// Constants.kt
package com.metacomputing.seed

import java.text.Normalizer

object Constants {
    val OHAENG_MAP = mapOf("목" to 0, "화" to 1, "토" to 2, "금" to 3, "수" to 4)
    val OHAENG_KOREAN = mapOf("木" to "목", "火" to "화", "土" to "토", "金" to "금", "水" to "수")
    val OHAENG_FULL = mapOf("木" to "목(木)", "火" to "화(火)", "土" to "토(土)", "金" to "금(金)", "水" to "수(水)")

    val YANG_STEMS = setOf("甲", "丙", "戊", "庚", "壬")
    val EUM_STEMS = setOf("乙", "丁", "己", "辛", "癸")
    val YANG_BRANCHES = setOf("子", "寅", "辰", "午", "申", "戌")
    val EUM_BRANCHES = setOf("丑", "卯", "巳", "未", "酉", "亥")

    val STEM_OHAENG = mapOf(
        "甲" to "목(木)", "乙" to "목(木)", "丙" to "화(火)", "丁" to "화(火)",
        "戊" to "토(土)", "己" to "토(土)", "庚" to "금(金)", "辛" to "금(金)",
        "壬" to "수(水)", "癸" to "수(水)"
    )

    val BRANCH_OHAENG = mapOf(
        "子" to "수(水)", "丑" to "토(土)", "寅" to "목(木)", "卯" to "목(木)",
        "辰" to "토(土)", "巳" to "화(火)", "午" to "화(火)", "未" to "토(土)",
        "申" to "금(金)", "酉" to "금(金)", "戌" to "토(土)", "亥" to "수(水)"
    )

    val CHOSUNG_OHAENG = mapOf(
        'ㄱ' to "木", 'ㅋ' to "木", 'ㄲ' to "木",
        'ㄴ' to "火", 'ㄷ' to "火", 'ㅌ' to "火", 'ㄹ' to "火", 'ㄸ' to "火",
        'ㅇ' to "土", 'ㅎ' to "土",
        'ㅅ' to "金", 'ㅈ' to "金", 'ㅊ' to "金", 'ㅆ' to "金", 'ㅉ' to "金",
        'ㅁ' to "水", 'ㅂ' to "水", 'ㅍ' to "水", 'ㅃ' to "水"
    )

    val CHOSUNG_LIST = listOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
        "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )

    val JUNGSUNG_LIST = listOf(
        "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ",
        "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
    )

    val YANG_JUNGSUNG = setOf('ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ')
}

fun String.normalize() = Normalizer.normalize(this, Normalizer.Form.NFC)
fun String.toOhaengKorean() = Constants.OHAENG_KOREAN[this.normalize()] ?: "토"
fun String.toOhaengFull() = Constants.OHAENG_FULL[this.normalize()] ?: "토(土)"
fun Int.toEumYang() = if (this % 2 == 0) "음" else "양"
fun Int.toOhaengByLastDigit() = when (this % 10) {
    1, 2 -> "목(木)"
    3, 4 -> "화(火)"
    5, 6 -> "토(土)"
    7, 8 -> "금(金)"
    9, 0 -> "수(水)"
    else -> "토(土)"
}

fun Char.extractChosung() = if (this in '가'..'힣') {
    Constants.CHOSUNG_LIST.getOrNull((this.code - 0xAC00) / (21 * 28)) ?: ""
} else ""

fun Char.extractJungsung() = if (this in '가'..'힣') {
    Constants.JUNGSUNG_LIST.getOrNull(((this.code - 0xAC00) % (21 * 28)) / 28) ?: ""
} else ""