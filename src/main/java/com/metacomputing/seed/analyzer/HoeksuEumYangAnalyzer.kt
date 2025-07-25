package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.StrokeCounter

class HoeksuEumYangAnalyzer {
    private val strokeCounter = StrokeCounter()

    fun analyze(nameInput: NameInput): HoeksuEumYang {
        var eumCount = 0
        var yangCount = 0

        // 성씨와 이름의 각 글자별 획수 계산
        val allHanja = nameInput.surnameHanja + nameInput.givenNameHanja
        allHanja.forEach { hanja ->
            val strokes = strokeCounter.countStrokes(hanja.toString())
            if (strokes % 2 == 0) eumCount++
            else yangCount++
        }

        return HoeksuEumYang(
            eumCount = eumCount,
            yangCount = yangCount
        )
    }
}
