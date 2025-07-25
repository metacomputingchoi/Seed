// BaleumOhengAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import java.text.Normalizer

class BaleumOhengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(nameInput: NameInput): BaleumOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        val arrangement = mutableListOf<String>()

        // 성씨 발음 오행 분석
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)

        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val hanjaInfo = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val oheng = normalizeString(hanjaInfo?.integratedInfo?.soundOheng ?: "土")

                val key = convertOhengKey(oheng)
                ohengCount[key] = ohengCount[key]!! + 1
                arrangement.add(convertToKorean(oheng))
            }
        }

        // 이름 발음 오행 분석
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
            val oheng = normalizeString(hanjaInfo?.integratedInfo?.soundOheng ?: "土")

            val key = convertOhengKey(oheng)
            ohengCount[key] = ohengCount[key]!! + 1
            arrangement.add(convertToKorean(oheng))
        }

        return BaleumOheng(
            ohengDistribution = ohengCount,
            arrangement = arrangement
        )
    }

    private fun normalizeString(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFC)
    }

    private fun convertOhengKey(oheng: String): String {
        return when(oheng) {
            "木" -> "목(木)"
            "火" -> "화(火)"
            "土" -> "토(土)"
            "金" -> "금(金)"
            "水" -> "수(水)"
            else -> "토(土)"
        }
    }

    private fun convertToKorean(oheng: String): String {
        return when(oheng) {
            "木" -> "목"
            "火" -> "화"
            "土" -> "토"
            "金" -> "금"
            "水" -> "수"
            else -> "토"
        }
    }
}