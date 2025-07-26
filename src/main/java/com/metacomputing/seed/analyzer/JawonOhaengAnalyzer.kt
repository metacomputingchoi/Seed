// analyzer/JawonOhaengAnalyzer.kt - NFC normalize 적용
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import java.text.Normalizer

class JawonOhaengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(nameInput: NameInput): JawonOhaeng {
        val ohaengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)

            // NFC normalize 적용
            val sourceElement = normalizeString(hanjaInfo?.sourceElement ?: "土")

            val key = convertOhaengKey(sourceElement)
            ohaengCount[key] = ohaengCount[key]!! + 1
        }

        return JawonOhaeng(
            ohaengDistribution = ohaengCount
        )
    }

    private fun normalizeString(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFC)
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
}