package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

class HoeksuEumYangAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(nameInput: NameInput): HoeksuEumYang {
        var eumCount = 0
        var yangCount = 0

        // 성씨 한자 분석
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val strokes = hanjaDB.getHanjaStrokes(parts[0], parts[1], true)
                if (strokes % 2 == 0) eumCount++ else yangCount++
            }
        }

        // 이름 한자 분석
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val strokes = hanjaDB.getHanjaStrokes(char.toString(), hanjaChar, false)
            if (strokes % 2 == 0) eumCount++ else yangCount++
        }

        return HoeksuEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
