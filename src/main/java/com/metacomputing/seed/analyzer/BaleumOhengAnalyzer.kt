package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

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

        // 성씨 발음 오행 분석
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val hanjaInfo = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val oheng = hanjaInfo?.integratedInfo?.soundOheng ?: "土"
                val key = convertOhengKey(oheng)
                ohengCount[key] = ohengCount[key]!! + 1
            }
        }

        // 이름 발음 오행 분석
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
            val oheng = hanjaInfo?.integratedInfo?.soundOheng ?: "土"
            val key = convertOhengKey(oheng)
            ohengCount[key] = ohengCount[key]!! + 1
        }

        return BaleumOheng(ohengDistribution = ohengCount)
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
}
