package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class BaleumEumYangAnalyzer {
    fun analyze(nameInput: NameInput): BaleumEumYang {
        var eumCount = 0
        var yangCount = 0

        // 전체 이름의 모음 음양 분석
        val fullName = nameInput.surname + nameInput.givenName
        fullName.forEach { char ->
            when (getVowelType(char)) {
                VowelType.YANG -> yangCount++
                VowelType.EUM -> eumCount++
                VowelType.NEUTRAL -> {} // 중성은 카운트하지 않음
            }
        }

        return BaleumEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }

    private enum class VowelType {
        YANG, EUM, NEUTRAL
    }

    private fun getVowelType(char: Char): VowelType {
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return VowelType.NEUTRAL

        val vowelIndex = (code % (21 * 28)) / 28

        // 양성 모음: ㅏ, ㅑ, ㅗ, ㅛ
        val yangVowels = setOf(0, 2, 8, 12)
        // 음성 모음: ㅓ, ㅕ, ㅜ, ㅠ, ㅡ, ㅣ
        val eumVowels = setOf(4, 6, 13, 17, 18, 20)

        return when {
            vowelIndex in yangVowels -> VowelType.YANG
            vowelIndex in eumVowels -> VowelType.EUM
            else -> VowelType.NEUTRAL
        }
    }
}
