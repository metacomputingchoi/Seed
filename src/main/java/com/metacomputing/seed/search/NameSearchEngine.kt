// search/NameSearchEngine.kt
package com.metacomputing.seed.search

import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.statistics.NameStatisticsManager
import com.metacomputing.seed.*
import com.metacomputing.seed.util.SageokSuriOptimizer

class NameSearchEngine(
    private val hanjaDB: HanjaDatabase,
    private val statsManager: NameStatisticsManager
) {
    private val validCombinationsByLength = mutableMapOf<Pair<List<Int>, Int>, Set<List<Int>>>()

    private val validCombinations: Set<NameCombination> by lazy {
        statsManager.getAllStatistics().flatMap { (givenName, stats) ->
            stats.hanjaCombinations
                .filter { it.length == givenName.length }
                .map { NameCombination(givenName, it) }
        }.toSet()
    }

    fun search(query: NameQuery): List<SearchResult> {
        val surnameCandidates = findSurnameCandidates(query.surnameBlocks)
        if (surnameCandidates.isEmpty()) return emptyList()

        if (query.nameBlocks.isEmpty()) {
            return surnameCandidates.map {
                SearchResult(it.korean, it.hanja, "", "")
            }
        }

        val nameLength = query.nameBlocks.size

        return surnameCandidates.flatMap { surname ->
            val surnameStrokes = getSurnameStrokes(surname)

            val validStrokeCombinations = getOrComputeValidCombinations(surnameStrokes, nameLength)

            validCombinations
                .filter { matchesNameQuery(it, query.nameBlocks) }
                .filter { hasValidStrokes(it, validStrokeCombinations) }
                .map { SearchResult(surname.korean, surname.hanja, it.korean, it.hanja) }
        }
    }

    private fun getOrComputeValidCombinations(surnameStrokes: List<Int>, nameLength: Int): Set<List<Int>> {
        val key = surnameStrokes to nameLength
        return validCombinationsByLength.getOrPut(key) {
            SageokSuriOptimizer.getValidNameStrokeCombinations(surnameStrokes, nameLength)
        }
    }

    private fun getSurnameStrokes(surname: SurnameCandidate): List<Int> {
        return hanjaDB.getSurnamePairs(surname.korean, surname.hanja).map { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) hanjaDB.getHanjaStrokes(parts[0], parts[1], true) else 0
        }
    }

    private fun hasValidStrokes(
        combination: NameCombination,
        validStrokeCombinations: Set<List<Int>>
    ): Boolean {
        val strokes = combination.korean.indices.map { i ->
            hanjaDB.getHanjaStrokes(
                combination.korean[i].toString(),
                combination.hanja[i].toString(),
                false
            )
        }

        return strokes in validStrokeCombinations
    }

    private fun matchesNameQuery(combination: NameCombination, blocks: List<NameBlock>): Boolean {
        if (combination.korean.length != blocks.size) return false

        return blocks.indices.all { i ->
            val block = blocks[i]
            val korean = combination.korean[i].toString()
            val hanja = combination.hanja[i].toString()

            matchesCharacter(korean, block) && (block.isHanjaEmpty || block.hanja == hanja)
        }
    }

    private fun matchesCharacter(korean: String, block: NameBlock) = when {
        block.isKoreanEmpty -> true
        block.isCompleteKorean -> korean == block.korean
        block.isChosungOnly -> korean.firstOrNull()?.extractChosung() == block.korean
        block.isJungsungOnly -> korean.firstOrNull()?.extractJungsung() == block.korean
        else -> false
    }

    private fun findSurnameCandidates(blocks: List<NameBlock>): List<SurnameCandidate> {
        if (blocks.isEmpty()) return emptyList()

        return if (blocks.size == 1) {
            val block = blocks[0]
            if (!block.isKoreanEmpty && !block.isHanjaEmpty &&
                hanjaDB.isSurname("${block.korean}/${block.hanja}")) {
                listOf(SurnameCandidate(block.korean, block.hanja))
            } else emptyList()
        } else {
            val (first, second) = blocks
            if (isValidDoubleSurname(first, second)) {
                listOf(SurnameCandidate("${first.korean}${second.korean}",
                    "${first.hanja}${second.hanja}"))
            } else emptyList()
        }
    }

    private fun isValidDoubleSurname(first: NameBlock, second: NameBlock) =
        !first.isKoreanEmpty && !first.isHanjaEmpty &&
                !second.isKoreanEmpty && !second.isHanjaEmpty &&
                first.korean.length == 1 && first.hanja.length == 1 &&
                second.korean.length == 1 && second.hanja.length == 1 &&
                hanjaDB.isSurname("${first.korean}${second.korean}/${first.hanja}${second.hanja}")
}

data class NameCombination(val korean: String, val hanja: String)
data class SurnameCandidate(val korean: String, val hanja: String)