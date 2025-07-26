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

    private var hanjaDict: Map<String, HanjaInfo> = emptyMap()
    private var koreanToHanjaMapping: Map<String, List<String>> = emptyMap()
    private var hanjaToKeysMapping: Map<String, List<String>> = emptyMap()

    init {
        loadHanjaData()
    }

    private fun loadHanjaData() {
        try {
            // 한자 사전 로드
            loadHanjaDict()
            // 한글→한자 매핑 로드
            loadKoreanToHanjaMapping()
            // 한자→키 매핑 로드
            loadHanjaToKeysMapping()
        } catch (e: Exception) {
            println("한자 데이터 로드 실패: ${e.message}")
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
        // 한글→한자 매핑 로드 코드
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
        // 한자→키 매핑 로드 코드 (필요시)
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
            strokeElement = info.strokeElement,  // 획수오행
            sourceElement = info.sourceElement,   // 자원오행
            soundOhaeng = BaleumAnalyzer.calculateBaleumOhaeng(korean),
            soundEumyang = BaleumAnalyzer.calculateSoundEumyang(korean),
            strokeEumyang = BaleumAnalyzer.calculateStrokeEumyang(
                info.strokes.toIntOrNull() ?: 10
            )
        )
    }

    fun getSurnamePairs(surname: String, surnameHanja: String): List<String> {
        // 복성 처리
        if (surname.length > 1) {
            // 복성인 경우 각 글자로 분리
            val pairs = mutableListOf<String>()
            surname.forEachIndexed { index, char ->
                val hanjaChar = surnameHanja.getOrNull(index)?.toString() ?: ""
                pairs.add("$char/$hanjaChar")
            }
            return pairs
        }

        // 단성인 경우
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

// 새로운 상세 정보 데이터 클래스
data class HanjaDetailedInfo(
    val korean: String,
    val hanja: String,
    val meaning: String,
    val originalStrokes: Int,
    val strokeElement: String,    // 획수오행 (stroke_element 사용)
    val sourceElement: String,    // 자원오행 (source_element 사용)
    val soundOhaeng: String,       // 발음오행
    val soundEumyang: Int,        // 발음음양
    val strokeEumyang: Int        // 획수음양
)