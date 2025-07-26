// SajuNameOhaengAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase

class SajuNameOhaengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(sajuOhaeng: SajuOhaeng, hoeksuOhaeng: HoeksuOhaeng, nameInput: NameInput): SajuNameOhaeng {
        val combinedOhaeng = mutableMapOf<String, Int>()

        // 사주 오행 복사
        sajuOhaeng.ohaengDistribution.forEach { (key, value) ->
            combinedOhaeng[key] = value
        }

        // 성씨를 제외한 이름의 획수 오행만 추가
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        val surnameCount = surnamePairs.size

        // 성씨 부분을 제외한 이름 부분의 오행만 추가
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