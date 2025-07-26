// analyzer/SajuNameOhaengAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

class SajuNameOhaengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(sajuOhaeng: SajuOhaeng, hoeksuOhaeng: HoeksuOhaeng, nameInput: NameInput): SajuNameOhaeng {
        val combinedOhaeng = mutableMapOf<String, Int>()

        sajuOhaeng.ohaengDistribution.forEach { (key, value) ->
            combinedOhaeng[key] = value
        }

        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        val surnameCount = surnamePairs.size

        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
            val ohaeng = hanjaInfo?.sourceElement?: "土"
            val key = when(ohaeng) {
                "木" -> "목(木)"
                "火" -> "화(火)"
                "土" -> "토(土)"
                "金" -> "금(金)"
                "水" -> "수(水)"
                else -> "토(土)"
            }
            combinedOhaeng[key] = (combinedOhaeng[key] ?: 0) + 1
        }

        return SajuNameOhaeng(ohaengDistribution = combinedOhaeng)
    }
}