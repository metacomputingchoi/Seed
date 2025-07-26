// database/HanjaDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.HanjaInfo
import com.metacomputing.seed.util.BaleumAnalyzer
import com.metacomputing.seed.util.ResourceLoader
import java.text.Normalizer

class HanjaDatabase {
    var hanjaDict: Map<String, HanjaInfo> = emptyMap()
        private set
    private val strokeCache = mutableMapOf<String, Int>()
    private val hanjaByStroke = mutableMapOf<Int, MutableList<Pair<String, String>>>()
    private var koreanToHanjaMapping: Map<String, List<String>> = emptyMap()
    private var hanjaToKeysMapping: Map<String, List<String>> = emptyMap()
    private var chosungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var jungsungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var surnamePairMapping: Map<String, List<String>> = emptyMap()

    init {
        loadAllData()
        buildStrokeIndex()
    }

    private fun loadAllData() {
        try {

            val rawDict = ResourceLoader.loadHanjaDict("name_char_hanja_dict_effective.json")
            hanjaDict = rawDict.mapKeys { it.key.normalize() }.mapValues { (_, value) ->
                value.copy(
                    charKo = value.charKo.normalize(),
                    hanja = value.hanja.normalize(),
                    meaning = value.meaning.normalize(),
                    strokeElement = value.strokeElement.normalize(),
                    radical = value.radical.normalize(),
                    sourceElement = value.sourceElement.normalize()
                )
            }

            koreanToHanjaMapping = ResourceLoader.loadStringListMap("name_korean_to_hanja_dict_keys_mapping_effective.json")
            hanjaToKeysMapping = ResourceLoader.loadStringListMap("name_hanja_to_hanja_dict_keys_mapping_effective.json")
            chosungToKoreanMapping = ResourceLoader.loadStringListMap("name_chosung_to_korean_mapping_effective.json")
            jungsungToKoreanMapping = ResourceLoader.loadStringListMap("name_jungsung_to_korean_mapping_effective.json")
            surnamePairMapping = ResourceLoader.loadStringListMap("surname_hanja_pair_mapping_dict.json")
        } catch (e: Exception) {
            println("한자 데이터 로드 실패: ${e.message}")
        }
    }

    private fun buildStrokeIndex() {
        hanjaDict.forEach { (key, info) ->
            val strokes = info.strokes.toIntOrNull() ?: return@forEach
            val parts = key.split("/")
            if (parts.size == 2) {
                hanjaByStroke.getOrPut(strokes) { mutableListOf() }
                    .add(parts[0] to parts[1])
            }
        }
    }

    fun getAllSurnames() = surnamePairMapping.keys.toList()
    fun getHanjaListByKorean(korean: String) = koreanToHanjaMapping[korean] ?: emptyList()
    fun getKoreanListByChosung(chosung: String) = chosungToKoreanMapping[chosung] ?: emptyList()
    fun getKoreanListByJungsung(jungsung: String) = jungsungToKoreanMapping[jungsung] ?: emptyList()
    fun isSurname(key: String) = surnamePairMapping.containsKey(key)

    fun getHanjaStrokes(korean: String, hanja: String, isSurname: Boolean = false): Int {
        val key = "$korean/$hanja"
        return strokeCache.getOrPut(key) {
            hanjaDict[key]?.strokes?.toIntOrNull() ?: estimatedStrokes[hanja] ?: 10
        }
    }

    fun getHanjaByStrokes(strokes: Int): List<Pair<String, String>> {
        return hanjaByStroke[strokes] ?: emptyList()
    }

    fun getHanjaCombinationsByStrokes(strokesList: List<Int>): List<List<Pair<String, String>>> {
        if (strokesList.isEmpty()) return emptyList()

        // 각 획수에 해당하는 한자들을 가져와서 카테시안 곱 생성
        val hanjaLists = strokesList.map { strokes ->
            hanjaByStroke[strokes] ?: emptyList()
        }

        return cartesianProduct(hanjaLists)
    }

    private fun cartesianProduct(lists: List<List<Pair<String, String>>>): List<List<Pair<String, String>>> {
        if (lists.isEmpty()) return listOf(emptyList())
        if (lists.size == 1) return lists[0].map { listOf(it) }

        val result = mutableListOf<List<Pair<String, String>>>()
        val restProduct = cartesianProduct(lists.drop(1))

        for (item in lists[0]) {
            for (restItems in restProduct) {
                result.add(listOf(item) + restItems)
            }
        }

        return result
    }

    fun getHanjaInfo(korean: String, hanja: String, isSurname: Boolean = false): HanjaDetailedInfo? {
        val key = "$korean/$hanja"
        val info = hanjaDict[key] ?: return null

        return HanjaDetailedInfo(
            korean, hanja, info.meaning,
            info.strokes.toIntOrNull() ?: 10,
            info.strokeElement, info.sourceElement,
            BaleumAnalyzer.calculateBaleumOhaeng(korean),
            BaleumAnalyzer.calculateSoundEumyang(korean),
            BaleumAnalyzer.calculateStrokeEumyang(info.strokes.toIntOrNull() ?: 10)
        )
    }

    fun getSurnamePairs(surname: String, surnameHanja: String) =
        if (surname.length > 1) {
            surname.indices.map { i -> "${surname[i]}/${surnameHanja.getOrNull(i) ?: ""}" }
        } else listOf("$surname/$surnameHanja")

    companion object {
        private val estimatedStrokes = mapOf(
            "金" to 8, "木" to 4, "水" to 4, "火" to 4, "土" to 3,
            "民" to 5, "秀" to 7, "李" to 7, "王" to 4, "張" to 11,
            "朴" to 6, "崔" to 11, "鄭" to 19, "姜" to 9, "趙" to 14,
            "尹" to 4, "林" to 8, "韓" to 17
        )
    }
}

private fun String.normalize() = Normalizer.normalize(this, Normalizer.Form.NFC)

data class HanjaDetailedInfo(
    val korean: String, val hanja: String, val meaning: String,
    val originalStrokes: Int, val strokeElement: String, val sourceElement: String,
    val soundOhaeng: String, val soundEumyang: Int, val strokeEumyang: Int
)