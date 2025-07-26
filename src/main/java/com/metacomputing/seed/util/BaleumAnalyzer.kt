// util/BaleumAnalyzer.kt
package com.metacomputing.seed.util

object BaleumAnalyzer {

    fun calculateBaleumOhaeng(korean: String): String {
        if (korean.isEmpty()) return "土"

        val chosung = extractChosung(korean.first())

        return when (chosung) {
            'ㄱ', 'ㅋ' -> "木"
            'ㄴ', 'ㄷ', 'ㅌ', 'ㄹ' -> "火"
            'ㅇ', 'ㅎ' -> "土"
            'ㅅ', 'ㅈ', 'ㅊ' -> "金"
            'ㅁ', 'ㅂ', 'ㅍ' -> "水"
            'ㄲ' -> "木"
            'ㄸ' -> "火"
            'ㅃ' -> "水"
            'ㅆ' -> "金"
            'ㅉ' -> "金"
            else -> "土"
        }
    }

    fun calculateSoundEumyang(korean: String): Int {
        if (korean.isEmpty()) return 0

        val jungsung = extractJungsung(korean.first())
        val jungsungYang = when (jungsung) {
            'ㅏ', 'ㅑ', 'ㅐ', 'ㅒ', 'ㅗ', 'ㅛ', 'ㅘ', 'ㅙ', 'ㅚ' -> true
            'ㅓ', 'ㅕ', 'ㅔ', 'ㅖ', 'ㅜ', 'ㅠ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅢ', 'ㅡ', 'ㅣ' -> false
            else -> false
        }

        return if (jungsungYang) 1 else 0
    }

    fun calculateStrokeEumyang(strokes: Int): Int {
        return if (strokes % 2 == 1) 1 else 0
    }

    private fun extractChosung(char: Char): Char {
        if (!char.isKorean()) return ' '

        val code = char.code - 0xAC00
        val chosungIndex = code / (21 * 28)

        val chosungList = listOf(
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
            'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
        )

        return if (chosungIndex in chosungList.indices) chosungList[chosungIndex] else ' '
    }

    private fun extractJungsung(char: Char): Char {
        if (!char.isKorean()) return ' '

        val code = char.code - 0xAC00
        val jungsungIndex = (code % (21 * 28)) / 28

        val jungsungList = listOf(
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
        )

        return if (jungsungIndex in jungsungList.indices) jungsungList[jungsungIndex] else ' '
    }

    private fun Char.isKorean(): Boolean = this in '가'..'힣'
}