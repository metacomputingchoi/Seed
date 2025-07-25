package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*

class BaleumOhengAnalyzer {
    fun analyze(nameInput: NameInput): BaleumOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        // 전체 이름의 발음 분석
        val fullName = nameInput.surname + nameInput.givenName
        fullName.forEach { char ->
            when {
                isWoodSound(char) -> ohengCount["목(木)"] = ohengCount["목(木)"]!! + 1
                isFireSound(char) -> ohengCount["화(火)"] = ohengCount["화(火)"]!! + 1
                isEarthSound(char) -> ohengCount["토(土)"] = ohengCount["토(土)"]!! + 1
                isMetalSound(char) -> ohengCount["금(金)"] = ohengCount["금(金)"]!! + 1
                isWaterSound(char) -> ohengCount["수(水)"] = ohengCount["수(水)"]!! + 1
            }
        }

        return BaleumOheng(ohengDistribution = ohengCount)
    }

    // 훈민정음 원리에 따른 자음 분류
    private fun isWoodSound(char: Char): Boolean {
        val consonant = getInitialConsonant(char)
        return consonant in listOf('ㄱ', 'ㅋ')
    }

    private fun isFireSound(char: Char): Boolean {
        val consonant = getInitialConsonant(char)
        return consonant in listOf('ㄴ', 'ㄷ', 'ㄹ', 'ㅌ')
    }

    private fun isEarthSound(char: Char): Boolean {
        val consonant = getInitialConsonant(char)
        return consonant in listOf('ㅇ', 'ㅎ')
    }

    private fun isMetalSound(char: Char): Boolean {
        val consonant = getInitialConsonant(char)
        return consonant in listOf('ㅅ', 'ㅈ', 'ㅊ')
    }

    private fun isWaterSound(char: Char): Boolean {
        val consonant = getInitialConsonant(char)
        return consonant in listOf('ㅁ', 'ㅂ', 'ㅍ')
    }

    private fun getInitialConsonant(char: Char): Char {
        val code = char.code - 0xAC00
        if (code < 0 || code > 11171) return ' '

        val initialIndex = code / (21 * 28)
        val initials = arrayOf(
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 
            'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
        )

        return if (initialIndex in initials.indices) initials[initialIndex] else ' '
    }
}
