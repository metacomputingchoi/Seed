package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.util.StrokeCounter

class SageokSuriAnalyzer {
    private val strokeCounter = StrokeCounter()

    fun analyze(nameInput: NameInput): SageokSuri {
        // 성씨와 이름의 획수 계산
        val surnameStrokes = strokeCounter.countTotalStrokes(nameInput.surnameHanja)
        val givenNameStrokes = nameInput.givenNameHanja.map { 
            strokeCounter.countStrokes(it.toString()) 
        }

        // 원형이정 계산
        val wonGyeok = surnameStrokes  // 원격 = 성씨 획수
        val hyeongGyeok = givenNameStrokes.getOrNull(0) ?: 0  // 형격 = 이름 첫 글자
        val iGyeok = givenNameStrokes.getOrNull(1)  // 이격 = 이름 둘째 글자 (있을 경우)
        val jeongGyeok = surnameStrokes + givenNameStrokes.sum()  // 정격 = 총 획수

        return SageokSuri(
            wonGyeok = wonGyeok,
            wonGyeokFortune = determineFortune(wonGyeok),
            hyeongGyeok = hyeongGyeok,
            hyeongGyeokFortune = determineFortune(hyeongGyeok),
            iGyeok = iGyeok,
            iGyeokFortune = iGyeok?.let { determineFortune(it) },
            jeongGyeok = jeongGyeok,
            jeongGyeokFortune = determineFortune(jeongGyeok)
        )
    }

    private fun determineFortune(strokes: Int): String {
        // 81수리 길흉 판단
        val fortuneNumber = if (strokes > 81) strokes % 81 else strokes

        return when (fortuneNumber) {
            1, 3, 5, 6, 7, 8, 11, 13, 15, 16, 17, 18, 21, 23, 24, 25, 29, 31, 32, 33, 35, 37, 39, 41, 45, 47, 48, 52, 57, 61, 63, 65, 67, 68, 81 -> "대길(大吉)"
            9, 10, 12, 14, 19, 20, 22, 26, 27, 28, 30, 34, 36, 38, 40, 42, 43, 44, 46, 49, 50, 51, 53, 54, 55, 56, 58, 59, 60, 62, 64, 66, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80 -> "흉(凶)"
            else -> "길(吉)"
        }
    }
}
