// analyzer/SageokSuriAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.database.StrokeMeaningDatabase

class SageokSuriAnalyzer {
    private val hanjaDB = HanjaDatabase()
    private val strokeDB = StrokeMeaningDatabase()

    fun analyze(nameInput: NameInput): SageokSuri {
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)

        val surnameHoeksuList = if (surnamePairs.size > 1) {
            surnamePairs.map { pair ->
                val parts = pair.split("/")
                if (parts.size == 2) {
                    hanjaDB.getHanjaStrokes(parts[0], parts[1], true)
                } else {
                    0
                }
            }
        } else {
            listOf(hanjaDB.getHanjaStrokes(nameInput.surname, nameInput.surnameHanja, true))
        }

        val givenNameHoeksuList = nameInput.givenName.mapIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            hanjaDB.getHanjaStrokes(char.toString(), hanjaChar, false)
        }.toMutableList()

        val totalHoeksuList = surnameHoeksuList + givenNameHoeksuList

        return calculateSagyeok(totalHoeksuList, surnameHoeksuList.size)
    }

    private fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): SageokSuri {
        val totalLength = hanjaHoeksuValues.size
        val nameLength = totalLength - surLength

        require(surLength >= 1) { "성은 최소 1자여야 합니다" }
        require(nameLength >= 1) { "이름은 최소 1자여야 합니다" }

        val surHoeksu = hanjaHoeksuValues.subList(0, surLength)
        val nameHoeksu = hanjaHoeksuValues.subList(surLength, totalLength).toMutableList()

        if (nameLength == 1) {
            nameHoeksu.add(0)
        }

        val myeongsangjaEndIdx = nameHoeksu.size / 2
        val myeongsangja = nameHoeksu.subList(0, myeongsangjaEndIdx).sum()
        val myeonghaja = nameHoeksu.subList(myeongsangjaEndIdx, nameHoeksu.size).sum()

        val jeong = hanjaHoeksuValues.sum()
        val won = nameHoeksu.sum()
        val i = surHoeksu.sum() + myeonghaja
        val hyeong = surHoeksu.sum() + myeongsangja

        val wonAdjusted = adjustTo81(won)
        val hyeongAdjusted = adjustTo81(hyeong)
        val iAdjusted = adjustTo81(i)
        val jeongAdjusted = adjustTo81(jeong)

        val wonMeaning = strokeDB.getStrokeMeaning(wonAdjusted)
        val hyeongMeaning = strokeDB.getStrokeMeaning(hyeongAdjusted)
        val iMeaning = strokeDB.getStrokeMeaning(iAdjusted)
        val jeongMeaning = strokeDB.getStrokeMeaning(jeongAdjusted)

        return SageokSuri(
            wonGyeok = wonAdjusted,
            wonGyeokFortune = wonMeaning?.luckyLevel ?: "불명",
            wonGyeokMeaning = wonMeaning?.summary ?: "",
            hyeongGyeok = hyeongAdjusted,
            hyeongGyeokFortune = hyeongMeaning?.luckyLevel ?: "불명",
            hyeongGyeokMeaning = hyeongMeaning?.summary ?: "",
            iGyeok = iAdjusted,
            iGyeokFortune = iMeaning?.luckyLevel ?: "불명",
            iGyeokMeaning = iMeaning?.summary ?: "",
            jeongGyeok = jeongAdjusted,
            jeongGyeokFortune = jeongMeaning?.luckyLevel ?: "불명",
            jeongGyeokMeaning = jeongMeaning?.summary ?: ""
        )
    }

    private fun adjustTo81(value: Int): Int {
        return if (value <= 81) value
        else ((value - 1) % 81) + 1
    }
}
