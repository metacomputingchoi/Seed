// util/SageokSuriOptimizer.kt
package com.metacomputing.seed.util

import com.metacomputing.seed.database.StrokeMeaningDatabase

object SageokSuriOptimizer {
    private val strokeDB = StrokeMeaningDatabase()
    private val validStrokeNumbers = mutableSetOf<Int>()
    private val validNameStrokeCombinations = mutableMapOf<List<Int>, Set<List<Int>>>()

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

    fun getValidNameStrokeCombinations(surnameStrokes: List<Int>, nameLength: Int): Set<List<Int>> {
        val key = surnameStrokes
        val cacheName = "${key}_${nameLength}"

        return validNameStrokeCombinations.getOrPut(key) {
            val validCombinations = mutableSetOf<List<Int>>()
            val surnameTotal = surnameStrokes.sum()

            generateAndTestCombinations(
                surnameTotal,
                surnameStrokes,
                nameLength,
                mutableListOf(),
                validCombinations
            )

            validCombinations
        }.filter { it.size == nameLength }.toSet()
    }

    private fun generateAndTestCombinations(
        surnameTotal: Int,
        surnameStrokes: List<Int>,
        targetLength: Int,
        currentCombination: MutableList<Int>,
        validCombinations: MutableSet<List<Int>>
    ) {
        if (currentCombination.size == targetLength) {
            if (isValidCombination(surnameTotal, currentCombination)) {
                validCombinations.add(currentCombination.toList())
            }
            return
        }

        for (strokes in 1..30) {
            currentCombination.add(strokes)
            generateAndTestCombinations(surnameTotal, surnameStrokes, targetLength, currentCombination, validCombinations)
            currentCombination.removeAt(currentCombination.size - 1)
        }
    }

    private fun isValidCombination(surnameTotal: Int, nameStrokes: List<Int>): Boolean {
        val adjustedNameStrokes = nameStrokes.toMutableList()
        if (adjustedNameStrokes.size == 1) {
            adjustedNameStrokes.add(0)
        }

        val myeongsangjaEndIdx = adjustedNameStrokes.size / 2
        val myeongsangja = adjustedNameStrokes.subList(0, myeongsangjaEndIdx).sum()
        val myeonghaja = adjustedNameStrokes.subList(myeongsangjaEndIdx, adjustedNameStrokes.size).sum()

        val won = adjustTo81(adjustedNameStrokes.sum())
        val hyeong = adjustTo81(surnameTotal + myeongsangja)
        val i = adjustTo81(surnameTotal + myeonghaja)
        val jeong = adjustTo81(surnameTotal + nameStrokes.sum())

        return won in validStrokeNumbers &&
                hyeong in validStrokeNumbers &&
                i in validStrokeNumbers &&
                jeong in validStrokeNumbers
    }

    private fun adjustTo81(value: Int) = if (value <= 81) value else ((value - 1) % 81) + 1

    fun isValidStrokeList(surnameStrokes: List<Int>, nameStrokes: List<Int>): Boolean {
        return isValidCombination(surnameStrokes.sum(), nameStrokes)
    }
}