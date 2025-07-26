// analyzer/SajuNameOhaengAnalyzer.kt - 완전히 다시 작성
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import java.text.Normalizer

class SajuNameOhaengAnalyzer {
    private val hanjaDB = HanjaDatabase()

    fun analyze(sajuOhaeng: SajuOhaeng, hoeksuOhaeng: HoeksuOhaeng, nameInput: NameInput): SajuNameOhaeng {
        // 사주오행 복사본 생성
        val combinedOhaeng = sajuOhaeng.ohaengDistribution.toMutableMap()

        // 자원오행을 사주오행에 더하기
        nameInput.givenName.forEachIndexed { index, char ->
            val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
            val hanjaInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)

            // NFC normalize 적용
            val sourceElement = normalizeString(hanjaInfo?.sourceElement ?: "土")

            val key = when(sourceElement) {
                "木" -> "목(木)"
                "火" -> "화(火)"
                "土" -> "토(土)"
                "金" -> "금(金)"
                "水" -> "수(水)"
                else -> "토(土)"
            }

            // 해당 오행에 1 더하기
            combinedOhaeng[key] = (combinedOhaeng[key] ?: 0) + 1
        }

        return SajuNameOhaeng(ohaengDistribution = combinedOhaeng)
    }

    private fun normalizeString(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFC)
    }
}