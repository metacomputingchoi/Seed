// util/hangul/HangulUtils.kt
package com.metacomputing.seed.util.hangul

import com.metacomputing.seed.domain.constants.HangulConstants

object HangulUtils {

    fun getInitialFromHangul(char: Char): Char? {
        return if (char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            val (cho, _, _) = char.toHangulDecomposition()
            HangulConstants.INITIALS[cho]
        } else null
    }

    fun extractInitials(text: String): String {
        return text.mapNotNull { getInitialFromHangul(it) }.joinToString("")
    }

    fun decomposeHangul(char: Char): HangulDecomposition {
        if (char !in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END) {
            throw IllegalArgumentException("한글이 아닙니다: $char")
        }

        val (cho, jung, jong) = char.toHangulDecomposition()

        return HangulDecomposition(
            initialConsonant = HangulConstants.INITIALS[cho],
            medialVowel = HangulConstants.MEDIALS[jung],
            finalConsonant = if (jong > 0) HangulConstants.FINALES[jong] else null  // String? 타입
        )
    }

    // 추가된 메서드들
    fun isHangul(char: Char): Boolean {
        return char in HangulConstants.HANGUL_START..HangulConstants.HANGUL_END
    }

    fun isInitialConsonant(char: Char): Boolean {
        return char in HangulConstants.INITIALS
    }

    fun isMedialVowel(char: Char): Boolean {
        return char in HangulConstants.MEDIALS
    }

    // 초성으로 시작하는지 확인
    fun startsWithInitial(text: String, initial: Char): Boolean {
        return text.isNotEmpty() && getInitialFromHangul(text[0]) == initial
    }

    // 한글 문자열인지 확인
    fun isHangulString(text: String): Boolean {
        return text.isNotEmpty() && text.all { isHangul(it) }
    }

    data class HangulDecomposition(
        val initialConsonant: Char,
        val medialVowel: Char,
        val finalConsonant: String?  // Char? -> String? 로 변경
    )
}