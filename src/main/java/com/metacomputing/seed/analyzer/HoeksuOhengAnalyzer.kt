package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.util.OhengUtil

class HoeksuOhengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(nameInput: NameInput): HoeksuOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        // 성씨 한자 분석
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val strokes = hanjaDB.getHanjaStrokes(parts[0], parts[1], true)
                val oheng = OhengUtil.getOhengByStroke(strokes)
                ohengCount["${oheng}(${getHanja(oheng)})"] = ohengCount["${oheng}(${getHanja(oheng)})"]!! + 1
            }
        }

        // 이름 한자 분석
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val strokes = hanjaDB.getHanjaStrokes(char.toString(), hanjaChar, false)
            val oheng = OhengUtil.getOhengByStroke(strokes)
            ohengCount["${oheng}(${getHanja(oheng)})"] = ohengCount["${oheng}(${getHanja(oheng)})"]!! + 1
        }

        return HoeksuOheng(ohengDistribution = ohengCount)
    }

    private fun getHanja(oheng: String): String {
        return when(oheng) {
            "목" -> "木"
            "화" -> "火"
            "토" -> "土"
            "금" -> "金"
            "수" -> "水"
            else -> ""
        }
    }
}
