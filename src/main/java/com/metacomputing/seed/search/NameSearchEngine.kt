package com.metacomputing.seed.search

import com.metacomputing.seed.model.NameBlock
import com.metacomputing.seed.model.NameQuery
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.statistics.NameStatisticsLoader

class NameSearchEngine(
    private val hanjaDB: HanjaDatabase,
    private val statsLoader: NameStatisticsLoader = NameStatisticsLoader()
) {

    // 미리 모든 유효한 이름 조합을 메모리에 로드
    private val allValidNameCombinations: Set<NameCombination> by lazy {
        val combinations = mutableSetOf<NameCombination>()
        val stats = statsLoader.loadStatistics()

        stats.forEach { (givenName, nameStats) ->
            // hanja_combinations가 있는 경우만 처리
            if (nameStats.hanjaCombinations.isNotEmpty()) {
                nameStats.hanjaCombinations.forEach { hanja ->
                    if (hanja.length == givenName.length) {
                        combinations.add(NameCombination(givenName, hanja))
                    }
                }
            }
            // hanja_combinations가 비어있으면 그 이름은 제외
        }

        combinations
    }

    fun search(query: NameQuery): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // 성씨 후보 찾기
        val surnameCandidates = findSurnameCandidates(query.surnameBlocks)
        if (surnameCandidates.isEmpty()) return emptyList()

        // 이름 블록이 없으면 빈 이름으로 처리
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

        // 유효한 이름 조합 필터링
        val matchingNames = allValidNameCombinations.filter { combination ->
            matchesNameQuery(combination, query.nameBlocks)
        }

        // 성씨와 이름 조합
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
        // 길이가 다르면 매치 안됨
        if (combination.korean.length != blocks.size) return false

        // 각 블록과 매칭 확인
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
        // 한글과 한자가 모두 채워져 있어야 함
        if (block.isKoreanEmpty || block.isHanjaEmpty) {
            return emptyList()
        }

        val key = "${block.korean}/${block.hanja}"

        // 성씨 데이터베이스에 정확히 일치하는 경우만 반환
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
        // 두 블록 모두 한글과 한자가 채워져 있어야 함
        if (firstBlock.isKoreanEmpty || firstBlock.isHanjaEmpty ||
            secondBlock.isKoreanEmpty || secondBlock.isHanjaEmpty) {
            return emptyList()
        }

        // 각 블록이 1글자씩이어야 함
        if (firstBlock.korean.length != 1 || firstBlock.hanja.length != 1 ||
            secondBlock.korean.length != 1 || secondBlock.hanja.length != 1) {
            return emptyList()
        }

        val combinedKey = "${firstBlock.korean}${secondBlock.korean}/${firstBlock.hanja}${secondBlock.hanja}"

        // 성씨 데이터베이스에 정확히 일치하는 경우만 반환
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

// 이름 조합을 나타내는 data class
data class NameCombination(
    val korean: String,
    val hanja: String
)

// 나머지 data class들
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