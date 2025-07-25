package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

class SageokSuriAnalyzer {
    private val hanjaDB = HanjaDatabase()

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

        return SageokSuri(
            wonGyeok = adjustTo81(won),
            wonGyeokFortune = determineFortune(adjustTo81(won)),
            hyeongGyeok = adjustTo81(hyeong),
            hyeongGyeokFortune = determineFortune(adjustTo81(hyeong)),
            iGyeok = adjustTo81(i),
            iGyeokFortune = determineFortune(adjustTo81(i)),
            jeongGyeok = adjustTo81(jeong),
            jeongGyeokFortune = determineFortune(adjustTo81(jeong))
        )
    }

    // 81 초과시 처리 (1~81 범위로 조정)
    private fun adjustTo81(value: Int): Int {
        return if (value <= 81) value
        else ((value - 1) % 81) + 1
    }

    private fun determineFortune(strokes: Int): String {
        // 81수리 길흉 판단
        return when (strokes) {
            1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25, 29, 31, 32, 33, 35, 37, 39, 41, 45, 47, 48, 52, 57, 61, 63, 65, 67, 68, 81 -> "대길(大吉)"
            9, 10, 12, 14, 19, 20, 22, 26, 27, 28, 30, 34, 36, 38, 40, 42, 43, 44, 46, 49, 50, 51, 53, 54, 55, 56, 58, 59, 60, 62, 64, 66, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80 -> "흉(凶)"
            else -> "길(吉)"
        }
    }
}
