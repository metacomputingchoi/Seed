// search/NameSearchEngine.kt
package com.metacomputing.seed.search

import com.metacomputing.seed.model.NameBlock
import com.metacomputing.seed.model.NameQuery
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.statistics.NameStatisticsLoader

class NameSearchEngine(
    private val hanjaDB: HanjaDatabase,
    private val statsLoader: NameStatisticsLoader = NameStatisticsLoader()
) {

    private val allValidNameCombinations: Set<NameCombination> by lazy {
        val combinations = mutableSetOf<NameCombination>()
        val stats = statsLoader.loadStatistics()

        stats.forEach { (givenName, nameStats) ->

            if (nameStats.hanjaCombinations.isNotEmpty()) {
                nameStats.hanjaCombinations.forEach { hanja ->
                    if (hanja.length == givenName.length) {
                        combinations.add(NameCombination(givenName, hanja))
                    }
                }
            }

        }

        combinations
    }

    fun search(query: NameQuery): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        val surnameCandidates = findSurnameCandidates(query.surnameBlocks)
        if (surnameCandidates.isEmpty()) return emptyList()

        if (query.nameBlocks.isEmpty()) {
            surnameCandidates.forEach { surname ->
                results.add(SearchResult(
                    surname = surname.korean,
                    surnameHanja = surname.hanja,
                    givenName = "",
                    givenNameHanja = ""
                ))
            }
            return results
        }

        val matchingNames = allValidNameCombinations.filter { combination ->
            matchesNameQuery(combination, query.nameBlocks)
        }

        for (surname in surnameCandidates) {
            for (nameCombination in matchingNames) {
                results.add(SearchResult(
                    surname = surname.korean,
                    surnameHanja = surname.hanja,
                    givenName = nameCombination.korean,
                    givenNameHanja = nameCombination.hanja
                ))
            }
        }

        return results
    }

    private fun matchesNameQuery(combination: NameCombination, blocks: List<NameBlock>): Boolean {

        if (combination.korean.length != blocks.size) return false

        return blocks.indices.all { index ->
            val block = blocks[index]
            val koreanChar = combination.korean[index].toString()
            val hanjaChar = combination.hanja[index].toString()

            matchesCharacter(koreanChar, block) &&
                    (block.isHanjaEmpty || block.hanja == hanjaChar)
        }
    }

    private fun matchesCharacter(korean: String, block: NameBlock): Boolean {
        return when {
            block.isKoreanEmpty -> true
            block.isCompleteKorean -> korean == block.korean
            block.isChosungOnly -> extractChosung(korean) == block.korean
            block.isJungsungOnly -> extractJungsung(korean) == block.korean
            else -> false
        }
    }

    private fun findSurnameCandidates(blocks: List<NameBlock>): List<SurnameCandidate> {
        if (blocks.isEmpty()) return emptyList()

        return if (blocks.size == 1) {
            findSingleSurnameCandidates(blocks[0])
        } else {
            findDoubleSurnameCandidates(blocks[0], blocks[1])
        }
    }

    private fun findSingleSurnameCandidates(block: NameBlock): List<SurnameCandidate> {

        if (block.isKoreanEmpty || block.isHanjaEmpty) {
            return emptyList()
        }

        val key = "${block.korean}/${block.hanja}"

        return if (hanjaDB.isSurname(key)) {
            listOf(SurnameCandidate(block.korean, block.hanja))
        } else {
            emptyList()
        }
    }

    private fun findDoubleSurnameCandidates(
        firstBlock: NameBlock,
        secondBlock: NameBlock
    ): List<SurnameCandidate> {

        if (firstBlock.isKoreanEmpty || firstBlock.isHanjaEmpty ||
            secondBlock.isKoreanEmpty || secondBlock.isHanjaEmpty) {
            return emptyList()
        }

        if (firstBlock.korean.length != 1 || firstBlock.hanja.length != 1 ||
            secondBlock.korean.length != 1 || secondBlock.hanja.length != 1) {
            return emptyList()
        }

        val combinedKey = "${firstBlock.korean}${secondBlock.korean}/${firstBlock.hanja}${secondBlock.hanja}"

        return if (hanjaDB.isSurname(combinedKey)) {
            listOf(SurnameCandidate(
                "${firstBlock.korean}${secondBlock.korean}",
                "${firstBlock.hanja}${secondBlock.hanja}"
            ))
        } else {
            emptyList()
        }
    }

    private fun matchesBlock(korean: String, hanja: String, block: NameBlock): Boolean {
        val koreanMatches = when {
            block.isKoreanEmpty -> true
            block.isCompleteKorean -> korean == block.korean
            block.isChosungOnly -> extractChosung(korean) == block.korean
            block.isJungsungOnly -> extractJungsung(korean) == block.korean
            else -> false
        }

        val hanjaMatches = when {
            block.isHanjaEmpty -> true
            else -> hanja == block.hanja
        }

        return koreanMatches && hanjaMatches
    }

    private fun extractChosung(char: String): String {
        if (char.isEmpty() || char[0] !in '가'..'힣') return ""

        val code = char[0].code - 0xAC00
        val chosungIndex = code / (21 * 28)

        val chosungList = listOf(
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ",
            "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
        )

        return if (chosungIndex in chosungList.indices) chosungList[chosungIndex] else ""
    }

    private fun extractJungsung(char: String): String {
        if (char.isEmpty() || char[0] !in '가'..'힣') return ""

        val code = char[0].code - 0xAC00
        val jungsungIndex = (code % (21 * 28)) / 28

        val jungsungList = listOf(
            "ㅏ", "ㅐ", "ㅑ", "ㅒ", "ㅓ", "ㅔ", "ㅕ", "ㅖ", "ㅗ", "ㅘ",
            "ㅙ", "ㅚ", "ㅛ", "ㅜ", "ㅝ", "ㅞ", "ㅟ", "ㅠ", "ㅡ", "ㅢ", "ㅣ"
        )

        return if (jungsungIndex in jungsungList.indices) jungsungList[jungsungIndex] else ""
    }
}

data class NameCombination(
    val korean: String,
    val hanja: String
)

data class CharCandidate(
    val korean: String,
    val hanja: String
)

data class SurnameCandidate(
    val korean: String,
    val hanja: String
)

data class SearchResult(
    val surname: String,
    val surnameHanja: String,
    val givenName: String,
    val givenNameHanja: String
) {
    val fullName: String = surname + givenName
    val fullNameHanja: String = surnameHanja + givenNameHanja
}