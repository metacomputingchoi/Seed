package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.StrokeCounter
import com.metacomputing.seed.util.OhengUtil

class HoeksuOhengAnalyzer {
    private val strokeCounter = StrokeCounter()

    fun analyze(nameInput: NameInput): HoeksuOheng {
        val ohengCount = mutableMapOf(
            "목(木)" to 0,
            "화(火)" to 0,
            "토(土)" to 0,
            "금(金)" to 0,
            "수(水)" to 0
        )

        // 성씨와 이름의 각 글자별 획수 계산
        val allHanja = nameInput.surnameHanja + nameInput.givenNameHanja
        allHanja.forEach { hanja ->
            val strokes = strokeCounter.countStrokes(hanja.toString())
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
