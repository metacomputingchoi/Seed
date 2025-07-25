package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

class BaleumEumYangAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(nameInput: NameInput): BaleumEumYang {
        var eumCount = 0
        var yangCount = 0
        val arrangement = mutableListOf<String>()

        // 성씨 발음 음양 분석
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val hanjaInfo = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val eumyang = hanjaInfo?.integratedInfo?.soundEumyang ?: 0
                if (eumyang == 0) {
                    eumCount++
                    arrangement.add("음")
                } else {
                    yangCount++
                    arrangement.add("양")
                }
            }
        }

        // 이름 발음 음양 분석
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
            val eumyang = hanjaInfo?.integratedInfo?.soundEumyang ?: 0
            if (eumyang == 0) {
                eumCount++
                arrangement.add("음")
            } else {
                yangCount++
                arrangement.add("양")
            }
        }

        return BaleumEumYang(
            eumCount = eumCount,
            yangCount = yangCount,
            arrangement = arrangement
        )
    }
}
