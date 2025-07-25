package com.metacomputing.seed.database

import com.metacomputing.seed.model.HanjaTripleData
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

    private var surnameHanjaData: Map<String, HanjaTripleData> = emptyMap()
    private var nameHanjaData: Map<String, HanjaTripleData> = emptyMap()
    private var surnamePairMapping: Map<String, List<String>> = emptyMap()

    init {
        loadHanjaData()
    }

    private fun loadHanjaData() {
        try {
            // 성씨 한자 데이터 로드
            loadSurnameData()

            // 이름 한자 데이터 로드
            loadNameData()

            // 복성 매핑 데이터 로드
            loadSurnamePairMapping()
        } catch (e: Exception) {
            println("한자 데이터 로드 실패: ${e.message}")
        }
    }

    private fun loadSurnameData() {
        val resourcePath = "resources/seed/data/surname_char_triple_dict.json"
        val file = File(resourcePath)

        surnameHanjaData = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), HanjaTripleData.serializer()),
                jsonString
            )
        } else {
            // 리소스에서 로드
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/surname_char_triple_dict.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), HanjaTripleData.serializer()),
                    jsonString
                )
            } ?: emptyMap()
        }
    }

    private fun loadNameData() {
        val resourcePath = "resources/seed/data/name_char_triple_dict_effective.json"
        val file = File(resourcePath)

        nameHanjaData = if (file.exists()) {
            val jsonString = file.readText()
            json.decodeFromString(
                MapSerializer(String.serializer(), HanjaTripleData.serializer()),
                jsonString
            )
        } else {
            // 리소스에서 로드
            val resourceStream = this::class.java.classLoader.getResourceAsStream(
                "seed/data/name_char_triple_dict_effective.json"
            )
            resourceStream?.use { stream ->
                val jsonString = stream.bufferedReader().use { it.readText() }
                json.decodeFromString(
                    MapSerializer(String.serializer(), HanjaTripleData.serializer()),
                    jsonString
                )
            } ?: emptyMap()
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
            // 리소스에서 로드
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

    fun getHanjaStrokes(korean: String, hanja: String, isSurname: Boolean = false): Int {
        val key = "$korean/$hanja"
        val data = if (isSurname) surnameHanjaData[key] else nameHanjaData[key]
        return data?.integratedInfo?.originalStrokes ?: getEstimatedStrokes(hanja)
    }

    fun getHanjaInfo(korean: String, hanja: String, isSurname: Boolean = false): HanjaTripleData? {
        val key = "$korean/$hanja"
        return if (isSurname) surnameHanjaData[key] else nameHanjaData[key]
    }

    fun getSurnamePairs(surname: String, surnameHanja: String): List<String> {
        val key = "$surname/$surnameHanja"
        return surnamePairMapping[key] ?: listOf(key)
    }

    // 데이터베이스에 없는 경우 추정 획수 계산
    private fun getEstimatedStrokes(hanja: String): Int {
        // 기본적인 한자 획수 (데이터베이스에 없는 경우)
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
            else -> 10  // 기본값
        }
    }
}
