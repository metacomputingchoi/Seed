package com.metacomputing.seed.util

class StrokeCounter {
    fun countStrokes(hanja: String): Int {
        // TODO: 실제 한자 획수 계산 구현
        // 한자 획수 데이터베이스 필요

        // 임시 mock 구현
        return when (hanja) {
            "金" -> 8
            "木" -> 4
            "水" -> 4
            "火" -> 4
            "土" -> 3
            "民" -> 5
            "秀" -> 7
            "李" -> 7
            "王" -> 4
            "張" -> 11
            "朴" -> 6
            "崔" -> 11
            "鄭" -> 19
            "姜" -> 9
            "趙" -> 14
            "尹" -> 4
            "林" -> 8
            "韓" -> 17
            else -> 10  // 기본값
        }
    }

    fun countTotalStrokes(hanjaString: String): Int {
        return hanjaString.sumOf { countStrokes(it.toString()) }
    }
}
