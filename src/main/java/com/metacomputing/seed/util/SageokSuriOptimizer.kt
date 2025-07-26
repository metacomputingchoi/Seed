// util/SageokSuriOptimizer.kt
package com.metacomputing.seed.util

import com.metacomputing.seed.database.StrokeMeaningDatabase

object SageokSuriOptimizer {
    private val strokeDB = StrokeMeaningDatabase()
    private val validStrokeNumbers = mutableSetOf<Int>()
    private val validNameStrokeCombinations = mutableMapOf<Pair<Int, Int>, Set<Pair<Int, Int>>>()

    init {
        for (strokes in 1..81) {
            val meaning = strokeDB.getStrokeMeaning(strokes)
            val fortune = meaning?.luckyLevel ?: ""
            if (fortune.contains("최상운수") || fortune.contains("상운수") ||
                (fortune.contains("양운수") && !fortune.contains("흉운수"))) {
                validStrokeNumbers.add(strokes)
            }
        }
    }

    fun getValidNameStrokeCombinations(surnameStroke1: Int, surnameStroke2: Int = 0): Set<Pair<Int, Int>> {
        val key = surnameStroke1 to surnameStroke2

        return validNameStrokeCombinations.getOrPut(key) {
            val validCombinations = mutableSetOf<Pair<Int, Int>>()
            val totalSurnameStroke = surnameStroke1 + surnameStroke2

            for (name1 in 1..30) {
                for (name2 in 0..30) {
                    val won = adjustTo81(name1 + name2)
                    val hyeong = adjustTo81(totalSurnameStroke + name1)
                    val i = adjustTo81(totalSurnameStroke + name2)
                    val jeong = adjustTo81(totalSurnameStroke + name1 + name2)

                    if (won in validStrokeNumbers &&
                        hyeong in validStrokeNumbers &&
                        i in validStrokeNumbers &&
                        jeong in validStrokeNumbers) {
                        validCombinations.add(name1 to name2)
                    }
                }
            }

            validCombinations
        }
    }

    private fun adjustTo81(value: Int) = if (value <= 81) value else ((value - 1) % 81) + 1

    fun isValidStroke(strokes: Int): Boolean = strokes in validStrokeNumbers
}