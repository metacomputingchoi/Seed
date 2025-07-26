// database/HanjaDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.HanjaInfo
import com.metacomputing.seed.util.BaleumAnalyzer
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class HanjaDatabase {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    var hanjaDict: Map<String, HanjaInfo> = emptyMap()
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

    private fun loadSurnamePairMapping() {
        val resourcePath = "resources/seed/data/surname_hanja_pair_mapping_dict.json"
        val file = File(resourcePath)

        surnamePairMapping = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/surname_hanja_pair_mapping_dict.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadHanjaDict() {
        val resourcePath = "resources/seed/data/name_char_hanja_dict_effective.json"
        val file = File(resourcePath)

        hanjaDict = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), HanjaInfo.serializer()),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_char_hanja_dict_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), HanjaInfo.serializer()),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadKoreanToHanjaMapping() {
        val resourcePath = "resources/seed/data/name_korean_to_hanja_dict_keys_mapping_effective.json"
        val file = File(resourcePath)

        koreanToHanjaMapping = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_korean_to_hanja_dict_keys_mapping_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadHanjaToKeysMapping() {
        val resourcePath = "resources/seed/data/name_hanja_to_hanja_dict_keys_mapping_effective.json"
        val file = File(resourcePath)

        hanjaToKeysMapping = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_hanja_to_hanja_dict_keys_mapping_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadChosungToKoreanMapping() {
        val resourcePath = "resources/seed/data/name_chosung_to_korean_mapping_effective.json"
        val file = File(resourcePath)

        chosungToKoreanMapping = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_chosung_to_korean_mapping_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadJungsungToKoreanMapping() {
        val resourcePath = "resources/seed/data/name_jungsung_to_korean_mapping_effective.json"
        val file = File(resourcePath)

        jungsungToKoreanMapping = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                jsonString
            )
        } else {
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_jungsung_to_korean_mapping_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), ListSerializer(String.serializer())),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    fun getAllSurnames(): List<String> {
        return surnamePairMapping.keys.toList()
    }

    fun getAllNameHanja(): List<String> {
        return hanjaDict.keys.toList()
    }

    fun isSurname(key: String): Boolean {
        return surnamePairMapping.containsKey(key)
    }

    fun getDoubleSurnameParts(surname: String, surnameHanja: String): List<String>? {
        val key = "$surname/$surnameHanja"
        return surnamePairMapping[key]
    }

    fun getHanjaListByKorean(korean: String): List<String> {
        return koreanToHanjaMapping[korean] ?: emptyList()
    }

    fun getReadingsByHanja(hanja: String): List<String> {
        return hanjaToKeysMapping[hanja] ?: emptyList()
    }

    fun getKoreanListByChosung(chosung: String): List<String> {
        return chosungToKoreanMapping[chosung] ?: emptyList()
    }

    fun getKoreanListByJungsung(jungsung: String): List<String> {
        return jungsungToKoreanMapping[jungsung] ?: emptyList()
    }

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
        if (surname.length > 1) {
            val pairs = mutableListOf<String>()
            surname.forEachIndexed { index, char ->
                val hanjaChar = surnameHanja.getOrNull(index)?.toString() ?: ""
                pairs.add("$char/$hanjaChar")
            }
            return pairs
        }

        return listOf("$surname/$surnameHanja")
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