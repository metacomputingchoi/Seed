// search/NameQueryParser.kt
package com.metacomputing.seed.search

import com.metacomputing.seed.model.NameBlock
import com.metacomputing.seed.model.NameQuery
import com.metacomputing.seed.database.HanjaDatabase

class NameQueryParser(private val hanjaDB: HanjaDatabase) {

    fun parse(input: String): NameQuery {
        val blocks = parseBlocks(input)

        return when {
            blocks.isEmpty() -> NameQuery(emptyList(), emptyList())
            blocks.size == 1 -> {
                // 첫 번째 블록 처리 (두 글자 성씨 확인)
                val processedBlocks = processFirstBlock(blocks[0])
                NameQuery(processedBlocks, emptyList())
            }
            else -> {
                // 여러 블록 처리
                val processedFirstBlocks = processFirstBlock(blocks[0])

                if (processedFirstBlocks.size == 2) {
                    // 두 글자 성씨로 분리된 경우
                    NameQuery(
                        surnameBlocks = processedFirstBlocks,
                        nameBlocks = blocks.drop(1)
                    )
                } else {
                    // 첫 번째와 두 번째 블록이 두 글자 성씨인지 확인
                    if (blocks.size >= 2 && isValidDoubleSurname(blocks[0], blocks[1])) {
                        NameQuery(
                            surnameBlocks = blocks.take(2),
                            nameBlocks = blocks.drop(2)
                        )
                    } else {
                        // 한 글자 성씨
                        NameQuery(
                            surnameBlocks = listOf(blocks[0]),
                            nameBlocks = blocks.drop(1)
                        )
                    }
                }
            }
        }
    }

    private fun parseBlocks(input: String): List<NameBlock> {
        val blockPattern = """\[([^/\]]*)/([^/\]]*)\]""".toRegex()
        return blockPattern.findAll(input).map { matchResult ->
            val korean = matchResult.groupValues[1].ifEmpty { "_" }
            val hanja = matchResult.groupValues[2].ifEmpty { "_" }
            NameBlock(korean, hanja)
        }.toList()
    }

    private fun processFirstBlock(block: NameBlock): List<NameBlock> {
        // 두 글자 성씨 확인
        if (!block.isKoreanEmpty && !block.isHanjaEmpty &&
            block.korean.length == 2 && block.hanja.length == 2) {

            val combinedKey = "${block.korean}/${block.hanja}"

            if (hanjaDB.isSurname(combinedKey)) {
                // 두 글자 성씨를 각각 분리
                val firstKorean = block.korean[0].toString()
                val secondKorean = block.korean[1].toString()
                val firstHanja = block.hanja[0].toString()
                val secondHanja = block.hanja[1].toString()

                return listOf(
                    NameBlock(firstKorean, firstHanja),
                    NameBlock(secondKorean, secondHanja)
                )
            }
        }

        return listOf(block)
    }

    private fun isValidDoubleSurname(first: NameBlock, second: NameBlock): Boolean {
        // 두 블록이 모두 비어있지 않은지 확인
        if (first.isKoreanEmpty || first.isHanjaEmpty ||
            second.isKoreanEmpty || second.isHanjaEmpty) {
            return false
        }

        // 각 블록이 한 글자인지 확인
        if (first.korean.length != 1 || first.hanja.length != 1 ||
            second.korean.length != 1 || second.hanja.length != 1) {
            return false
        }

        // 두 글자를 합쳐서 성씨인지 확인
        val combinedKey = "${first.korean}${second.korean}/${first.hanja}${second.hanja}"

        return hanjaDB.isSurname(combinedKey)
    }
}