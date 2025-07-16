// domain/model/name/Name.kt
package com.metacomputing.seed.domain.model.name

class Name private constructor(
    private val allBlocks: List<NameBlock>
) {
    val surname: SurnameInfo = SurnameInfo(listOf(allBlocks.first()))
    val givenName: GivenNameInfo = GivenNameInfo(allBlocks.drop(1))

    init {
        require(allBlocks.isNotEmpty()) { "최소 1개 이상의 블록이 필요합니다" }
    }

    companion object Factory {
        fun fromTriples(triples: List<Triple<String, String, Boolean>>): Name {
            require(triples.isNotEmpty()) { "최소 1개 이상의 블록이 필요합니다" }

            val blocks = triples.map { (pron, chinese, isPartial) ->
                NameBlock(
                    pronunciationChar = pron.ifEmpty { "_" },
                    chineseChar = chinese.ifEmpty { "_" },
                    pronunciationPartiality = isPartial
                )
            }

            return Name(blocks)
        }

        fun fromTriples(vararg triples: Triple<String, String, Boolean>): Name {
            return fromTriples(triples.toList())
        }

        fun fromString(nameString: String, partialityInfo: List<Boolean>): Name {
            val blockPattern = """\[([^/]*)/([^\]]*)\]""".toRegex()
            val matches = blockPattern.findAll(nameString).toList()

            require(matches.isNotEmpty()) { "최소 1개 이상의 블록이 필요합니다" }
            require(matches.size == partialityInfo.size) {
                "블록 수(${matches.size})와 부분성 정보 수(${partialityInfo.size})가 일치해야 합니다"
            }

            val triples = matches.mapIndexed { index, match ->
                val (pronunciation, chineseChar) = match.destructured
                Triple(pronunciation, chineseChar, partialityInfo[index])
            }

            return fromTriples(triples)
        }
    }

    override fun toString(): String = allBlocks.joinToString("")
}