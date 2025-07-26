// database/HanjaDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.HanjaInfo
import com.metacomputing.seed.util.BaleumAnalyzer
import kotlinx.serialization.json.Json
import java.io.File
import java.text.Normalizer

class HanjaDatabase {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    var hanjaDict: Map<String, HanjaInfo> = emptyMap()
        private set
    private var koreanToHanjaMapping: Map<String, List<String>> = emptyMap()
    private var hanjaToKeysMapping: Map<String, List<String>> = emptyMap()
    private var chosungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var jungsungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var surnamePairMapping: Map<String, List<String>> = emptyMap()

    init {
        loadHanjaData()
    }

    private fun loadHanjaData() {
        try {
            loadHanjaDict()
            loadKoreanToHanjaMapping()
            loadHanjaToKeysMapping()
            loadChosungToKoreanMapping()
            loadJungsungToKoreanMapping()
            loadSurnamePairMapping()
        } catch (e: Exception) {
            println("한자 데이터 로드 실패: ${e.message}")
        }
    }

    private fun loadHanjaDict() {
        val rawDict: Map<String, HanjaInfo> = loadJsonData("name_char_hanja_dict_effective.json")

        hanjaDict = rawDict.mapKeys { (key, _) ->
            Normalizer.normalize(key, Normalizer.Form.NFC)
        }.mapValues { (_, value) ->
            value.copy(
                charKo = Normalizer.normalize(value.charKo, Normalizer.Form.NFC),
                hanja = Normalizer.normalize(value.hanja, Normalizer.Form.NFC),
                meaning = Normalizer.normalize(value.meaning, Normalizer.Form.NFC),
                strokeElement = Normalizer.normalize(value.strokeElement, Normalizer.Form.NFC),
                radical = Normalizer.normalize(value.radical, Normalizer.Form.NFC),
                sourceElement = Normalizer.normalize(value.sourceElement, Normalizer.Form.NFC)
            )
        }
    }

    private fun loadKoreanToHanjaMapping() {
        koreanToHanjaMapping = loadJsonData("name_korean_to_hanja_dict_keys_mapping_effective.json")
    }

    private fun loadHanjaToKeysMapping() {
        hanjaToKeysMapping = loadJsonData("name_hanja_to_hanja_dict_keys_mapping_effective.json")
    }

    private fun loadChosungToKoreanMapping() {
        chosungToKoreanMapping = loadJsonData("name_chosung_to_korean_mapping_effective.json")
    }

    private fun loadJungsungToKoreanMapping() {
        jungsungToKoreanMapping = loadJsonData("name_jungsung_to_korean_mapping_effective.json")
    }

    private fun loadSurnamePairMapping() {
        surnamePairMapping = loadJsonData("surname_hanja_pair_mapping_dict.json")
    }

    private inline fun <reified T> loadJsonData(fileName: String): T {
        val resourcePath = "resources/seed/data/$fileName"
        val file = File(resourcePath)

        val jsonString = if (file.exists()) {
            file.readText()
        } else {
            this::class.java.classLoader.getResourceAsStream("seed/data/$fileName")
                ?.bufferedReader()?.use { it.readText() }
                ?: when (T::class) {
                    Map::class -> return emptyMap<String, Any>() as T
                    else -> throw Exception("리소스를 찾을 수 없음: $fileName")
                }
        }

        return json.decodeFromString(jsonString)
    }

    fun getAllSurnames(): List<String> = surnamePairMapping.keys.toList()
    fun getHanjaListByKorean(korean: String): List<String> = koreanToHanjaMapping[korean] ?: emptyList()
    fun getReadingsByHanja(hanja: String): List<String> = hanjaToKeysMapping[hanja] ?: emptyList()
    fun getKoreanListByChosung(chosung: String): List<String> = chosungToKoreanMapping[chosung] ?: emptyList()
    fun getKoreanListByJungsung(jungsung: String): List<String> = jungsungToKoreanMapping[jungsung] ?: emptyList()
    fun isSurname(key: String): Boolean = surnamePairMapping.containsKey(key)

    fun isMultiReading(hanja: String): Boolean {
        val readings = hanjaToKeysMapping[hanja] ?: emptyList()
        return readings.size > 1
    }

    fun getHanjaCount(korean: String): Int {
        val hanjaList = koreanToHanjaMapping[korean] ?: emptyList()
        return hanjaList.size
    }

    fun getHanjaStrokes(korean: String, hanja: String, isSurname: Boolean = false): Int {
        val key = "$korean/$hanja"
        val info = hanjaDict[key]
        return info?.strokes?.toIntOrNull() ?: getEstimatedStrokes(hanja)
    }

    fun getHanjaInfo(korean: String, hanja: String, isSurname: Boolean = false): HanjaDetailedInfo? {
        val key = "$korean/$hanja"
        val info = hanjaDict[key] ?: return null

        return HanjaDetailedInfo(
            korean = korean,
            hanja = hanja,
            meaning = info.meaning,
            originalStrokes = info.strokes.toIntOrNull() ?: 10,
            strokeElement = info.strokeElement,
            sourceElement = info.sourceElement,
            soundOhaeng = BaleumAnalyzer.calculateBaleumOhaeng(korean),
            soundEumyang = BaleumAnalyzer.calculateSoundEumyang(korean),
            strokeEumyang = BaleumAnalyzer.calculateStrokeEumyang(
                info.strokes.toIntOrNull() ?: 10
            )
        )
    }

    fun getSurnamePairs(surname: String, surnameHanja: String): List<String> {
        return if (surname.length > 1) {
            surname.indices.map { i ->
                "${surname[i]}/${surnameHanja.getOrNull(i) ?: ""}"
            }
        } else {
            listOf("$surname/$surnameHanja")
        }
    }

    fun getDoubleSurnameParts(surname: String, surnameHanja: String): List<String>? {
        val key = "$surname/$surnameHanja"
        return surnamePairMapping[key]
    }

    private fun getEstimatedStrokes(hanja: String): Int {
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
            else -> 10
        }
    }
}

data class HanjaDetailedInfo(
    val korean: String,
    val hanja: String,
    val meaning: String,
    val originalStrokes: Int,
    val strokeElement: String,
    val sourceElement: String,
    val soundOhaeng: String,
    val soundEumyang: Int,
    val strokeEumyang: Int
)