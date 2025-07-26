// database/HanjaDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.HanjaInfo
import com.metacomputing.seed.util.BaleumAnalyzer
import com.metacomputing.seed.util.ResourceLoader
import java.text.Normalizer

class HanjaDatabase {
    // 미리 계산된 데이터를 저장하는 구조체
    private data class PrecomputedHanjaInfo(
        val info: HanjaInfo,
        val strokes: Int,
        val soundOhaeng: String,
        val soundEumyang: Int,
        val strokeEumyang: Int
    )

    // 최적화된 데이터 저장소
    private var precomputedDict: Map<String, PrecomputedHanjaInfo> = emptyMap()
    private var koreanToHanjaMapping: Map<String, List<String>> = emptyMap()
    private var hanjaToKeysMapping: Map<String, List<String>> = emptyMap()
    private var chosungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var jungsungToKoreanMapping: Map<String, List<String>> = emptyMap()
    private var surnamePairMapping: Map<String, List<String>> = emptyMap()

    // 자주 사용되는 한자의 획수를 빠르게 조회하기 위한 캐시
    private var strokeCache: Map<String, Int> = emptyMap()

    // 상수
    private val DEFAULT_STROKE_COUNT = 10
    private val EMPTY_LIST = emptyList<String>()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        try {
            // 원본 데이터 로드
            val rawDict = ResourceLoader.loadHanjaDict("name_char_hanja_dict_effective.json")

            // 데이터 전처리 및 미리 계산
            val tempPrecomputedDict = mutableMapOf<String, PrecomputedHanjaInfo>()
            val tempStrokeCache = mutableMapOf<String, Int>()

            rawDict.forEach { (key, value) ->
                // 키 정규화
                val normalizedKey = key.normalize()

                // 획수 미리 계산
                val strokes = value.strokes.toIntOrNull() ?: DEFAULT_STROKE_COUNT

                // HanjaInfo 정규화
                val normalizedInfo = value.copy(
                    charKo = value.charKo.normalize(),
                    hanja = value.hanja.normalize(),
                    meaning = value.meaning.normalize(),
                    strokeElement = value.strokeElement.normalize(),
                    radical = value.radical.normalize(),
                    sourceElement = value.sourceElement.normalize()
                )

                // 발음 관련 정보 미리 계산
                val soundOhaeng = BaleumAnalyzer.calculateBaleumOhaeng(normalizedInfo.charKo)
                val soundEumyang = BaleumAnalyzer.calculateSoundEumyang(normalizedInfo.charKo)
                val strokeEumyang = BaleumAnalyzer.calculateStrokeEumyang(strokes)

                // 미리 계산된 정보 저장
                tempPrecomputedDict[normalizedKey] = PrecomputedHanjaInfo(
                    info = normalizedInfo,
                    strokes = strokes,
                    soundOhaeng = soundOhaeng,
                    soundEumyang = soundEumyang,
                    strokeEumyang = strokeEumyang
                )

                // 한자별 획수 캐시 (빠른 조회용)
                tempStrokeCache[normalizedInfo.hanja] = strokes
            }

            precomputedDict = tempPrecomputedDict
            strokeCache = tempStrokeCache

            // 나머지 매핑 로드 (이미 정규화된 상태로 저장되어 있다고 가정)
            koreanToHanjaMapping = ResourceLoader.loadStringListMap("name_korean_to_hanja_dict_keys_mapping_effective.json")
            hanjaToKeysMapping = ResourceLoader.loadStringListMap("name_hanja_to_hanja_dict_keys_mapping_effective.json")
            chosungToKoreanMapping = ResourceLoader.loadStringListMap("name_chosung_to_korean_mapping_effective.json")
            jungsungToKoreanMapping = ResourceLoader.loadStringListMap("name_jungsung_to_korean_mapping_effective.json")
            surnamePairMapping = ResourceLoader.loadStringListMap("surname_hanja_pair_mapping_dict.json")

        } catch (e: Exception) {
            println("한자 데이터 로드 실패: ${e.message}")
        }
    }

    fun getAllSurnames(): List<String> = surnamePairMapping.keys.toList()

    fun getHanjaListByKorean(korean: String): List<String> =
        koreanToHanjaMapping[korean] ?: EMPTY_LIST

    fun getKoreanListByChosung(chosung: String): List<String> =
        chosungToKoreanMapping[chosung] ?: EMPTY_LIST

    fun getKoreanListByJungsung(jungsung: String): List<String> =
        jungsungToKoreanMapping[jungsung] ?: EMPTY_LIST

    fun isSurname(key: String): Boolean = surnamePairMapping.containsKey(key)

    fun getHanjaStrokes(korean: String, hanja: String, isSurname: Boolean = false): Int {
        // StringBuilder 대신 문자열 템플릿 사용 (컴파일러 최적화)
        val key = "$korean/$hanja"

        // 미리 계산된 값 조회
        precomputedDict[key]?.let { return it.strokes }

        // 캐시에서 조회
        strokeCache[hanja]?.let { return it }

        // 기본 추정값
        return estimatedStrokes[hanja] ?: DEFAULT_STROKE_COUNT
    }

    fun getHanjaInfo(korean: String, hanja: String, isSurname: Boolean = false): HanjaDetailedInfo? {
        val key = "$korean/$hanja"
        val precomputed = precomputedDict[key] ?: return null

        // 미리 계산된 값들을 사용하여 HanjaDetailedInfo 생성
        return HanjaDetailedInfo(
            korean = precomputed.info.charKo,
            hanja = precomputed.info.hanja,
            meaning = precomputed.info.meaning,
            originalStrokes = precomputed.strokes,
            strokeElement = precomputed.info.strokeElement,
            sourceElement = precomputed.info.sourceElement,
            soundOhaeng = precomputed.soundOhaeng,
            soundEumyang = precomputed.soundEumyang,
            strokeEumyang = precomputed.strokeEumyang
        )
    }

    fun getSurnamePairs(surname: String, surnameHanja: String): List<String> {
        return when (surname.length) {
            1 -> listOf("$surname/$surnameHanja")
            2 -> {
                // 미리 할당된 리스트 사용
                val result = ArrayList<String>(2)
                result.add("${surname[0]}/${surnameHanja.getOrNull(0) ?: ""}")
                result.add("${surname[1]}/${surnameHanja.getOrNull(1) ?: ""}")
                result
            }
            else -> {
                // 3글자 이상 성씨의 경우
                surname.indices.map { i ->
                    "${surname[i]}/${surnameHanja.getOrNull(i) ?: ""}"
                }
            }
        }
    }

    // hanjaDict에 대한 읽기 전용 접근자 (호환성 유지)
    val hanjaDict: Map<String, HanjaInfo>
        get() = precomputedDict.mapValues { it.value.info }

    companion object {
        private val estimatedStrokes = mapOf(
            "金" to 8, "木" to 4, "水" to 4, "火" to 4, "土" to 3,
            "民" to 5, "秀" to 7, "李" to 7, "王" to 4, "張" to 11,
            "朴" to 6, "崔" to 11, "鄭" to 19, "姜" to 9, "趙" to 14,
            "尹" to 4, "林" to 8, "韓" to 17
        )
    }
}

// Extension function을 inline으로 변경하여 성능 향상
private inline fun String.normalize(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

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