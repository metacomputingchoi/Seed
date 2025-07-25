package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.database.StrokeMeaningDatabase

class SageokSuriAnalyzer {
    private val hanjaDB = HanjaDatabase()
    private val strokeDB = StrokeMeaningDatabase()

    fun analyze(nameInput: NameInput): SageokSuri {
        // 복성 처리를 위한 성씨 분해
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)

        // 성씨 획수 계산
        val surnameHoeksuList = if (surnamePairs.size > 1) {
            // 복성인 경우
            surnamePairs.map { pair ->
                val parts = pair.split("/")
                if (parts.size == 2) {
                    hanjaDB.getHanjaStrokes(parts[0], parts[1], true)
                } else {
                    0
                }
            }
        } else {
            // 단성인 경우
            listOf(hanjaDB.getHanjaStrokes(nameInput.surname, nameInput.surnameHanja, true))
        }

        // 이름 획수 계산
        val givenNameHoeksuList = nameInput.givenName.mapIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            hanjaDB.getHanjaStrokes(char.toString(), hanjaChar, false)
        }.toMutableList()

        // 전체 획수 리스트
        val totalHoeksuList = surnameHoeksuList + givenNameHoeksuList

        // 사격 계산
        return calculateSagyeok(totalHoeksuList, surnameHoeksuList.size)
    }

    private fun calculateSagyeok(hanjaHoeksuValues: List<Int>, surLength: Int): SageokSuri {
        val totalLength = hanjaHoeksuValues.size
        val nameLength = totalLength - surLength

        // 유효성 검사
        require(surLength >= 1) { "성은 최소 1자여야 합니다" }
        require(nameLength >= 1) { "이름은 최소 1자여야 합니다" }

        // 성씨와 이름 부분 분리
        val surHoeksu = hanjaHoeksuValues.subList(0, surLength)
        val nameHoeksu = hanjaHoeksuValues.subList(surLength, totalLength).toMutableList()

        // 이름이 1자인 경우 허수(0) 추가
        if (nameLength == 1) {
            nameHoeksu.add(0)
        }

        // 명상자와 명하자 분리 (일반화된 규칙)
        val myeongsangjaEndIdx = nameHoeksu.size / 2
        val myeongsangja = nameHoeksu.subList(0, myeongsangjaEndIdx).sum()
        val myeonghaja = nameHoeksu.subList(myeongsangjaEndIdx, nameHoeksu.size).sum()

        // 사격 계산
        val jeong = hanjaHoeksuValues.sum()  // 정격: 전체 합
        val won = nameHoeksu.sum()           // 원격: 이름 전체 합
        val i = surHoeksu.sum() + myeonghaja // 이격: 성씨전체 + 명하자
        val hyeong = surHoeksu.sum() + myeongsangja // 형격: 성씨전체 + 명상자

        // 각 격의 획수를 81 범위로 조정
        val wonAdjusted = adjustTo81(won)
        val hyeongAdjusted = adjustTo81(hyeong)
        val iAdjusted = adjustTo81(i)
        val jeongAdjusted = adjustTo81(jeong)

        // stroke_data.json에서 각 격의 정보 가져오기
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

    // 81 초과시 처리 (1~81 범위로 조정)
    private fun adjustTo81(value: Int): Int {
        return if (value <= 81) value
        else ((value - 1) % 81) + 1
    }
}
