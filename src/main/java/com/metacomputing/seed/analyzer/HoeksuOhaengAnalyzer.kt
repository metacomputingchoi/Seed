// analyzer/HoeksuOhaengAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import java.text.Normalizer

class HoeksuOhaengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    private fun normalizeString(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFC)
    }

    fun analyze(nameInput: NameInput): HoeksuOhaeng {
        val ohaengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        val arrangement = mutableListOf<String>()

        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val hanjaInfo = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val ohaeng = normalizeString(hanjaInfo?.strokeElement ?: "土")
                val key = convertOhaengKey(ohaeng)
                ohaengCount[key] = ohaengCount[key]!! + 1
                arrangement.add(convertToKorean(ohaeng))
            }
        }

        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
            val ohaeng = normalizeString(hanjaInfo?.strokeElement ?: "土")
            val key = convertOhaengKey(ohaeng)
            ohaengCount[key] = ohaengCount[key]!! + 1
            arrangement.add(convertToKorean(ohaeng))
        }

        return HoeksuOhaeng(
            ohaengDistribution = ohaengCount,
            arrangement = arrangement
        )
    }

    private fun convertOhaengKey(ohaeng: String): String {
        val normalized = Normalizer.normalize(ohaeng, Normalizer.Form.NFC)
        return when(normalized) {
            "木" -> "목(木)"
            "火" -> "화(火)"
            "土" -> "토(土)"
            "金" -> "금(金)"
            "水" -> "수(水)"
            else -> "토(土)"
        }
    }

    private fun convertToKorean(ohaeng: String): String {
        val normalized = Normalizer.normalize(ohaeng, Normalizer.Form.NFC)
        return when(normalized) {
            "木" -> "목"
            "火" -> "화"
            "土" -> "토"
            "金" -> "금"
            "水" -> "수"
            else -> "토"
        }
    }
}