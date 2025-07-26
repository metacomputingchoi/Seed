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
                // 첫 블록이 복성인지 확인
                val processedBlocks = processFirstBlock(blocks[0])
                NameQuery(processedBlocks, emptyList())
            }
            else -> {
                // 첫 블록 처리 (복성일 수 있음)
                val processedFirstBlocks = processFirstBlock(blocks[0])

                if (processedFirstBlocks.size == 2) {
                    // 첫 블록이 복성으로 분리된 경우
                    NameQuery(
                        surnameBlocks = processedFirstBlocks,
                        nameBlocks = blocks.drop(1)
                    )
                } else {
                    // 첫 블록이 단성인 경우, 두 번째 블록과 함께 복성인지 확인
                    if (blocks.size >= 2 && isValidDoubleSurname(blocks[0], blocks[1])) {
                        NameQuery(
                            surnameBlocks = blocks.take(2),
                            nameBlocks = blocks.drop(2)
                        )
                    } else {
                        // 단성인 경우
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
        // 한글과 한자가 모두 채워져 있고 2글자인 경우만 복성 가능성 확인
        if (!block.isKoreanEmpty && !block.isHanjaEmpty &&
            block.korean.length == 2 && block.hanja.length == 2) {

            val combinedKey = "${block.korean}/${block.hanja}"

            // 복성 데이터베이스에 정확히 일치하는 키가 있는지 확인
            if (hanjaDB.isSurname(combinedKey)) {
                // 복성인 경우 2개로 분리
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

        // 복성이 아니면 그대로 반환
        return listOf(block)
    }

    private fun isValidDoubleSurname(first: NameBlock, second: NameBlock): Boolean {
        // 두 블록 모두 한글과 한자가 완전히 채워져 있어야 함
        if (first.isKoreanEmpty || first.isHanjaEmpty ||
            second.isKoreanEmpty || second.isHanjaEmpty) {
            return false
        }

        // 각 블록이 1글자씩이어야 함
        if (first.korean.length != 1 || first.hanja.length != 1 ||
            second.korean.length != 1 || second.hanja.length != 1) {
            return false
        }

        // 조합된 복성 키 생성
        val combinedKey = "${first.korean}${second.korean}/${first.hanja}${second.hanja}"

        // 성씨 데이터베이스에 정확히 일치하는 키가 있는지 확인
        return hanjaDB.isSurname(combinedKey)
    }
}