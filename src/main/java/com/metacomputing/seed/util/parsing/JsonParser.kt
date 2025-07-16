// util/parsing/JsonParser.kt
package com.metacomputing.seed.util.parsing

import com.metacomputing.seed.domain.exception.NamingException
import com.metacomputing.seed.util.hangul.normalizeNFC
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONException

object JsonParser {
    fun parseJsonToMap(json: String): Map<String, Map<String, Any>> {
        return parseJsonObject(json, "JSON 맵 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    key.normalizeNFC() to parseInnerObject(jsonObj.getJSONObject(key))
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    fun parseJsonToMapList(json: String): Map<String, List<String>> {
        return parseJsonObject(json, "JSON 리스트 맵 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().mapNotNull { key ->
                try {
                    val jsonArray = jsonObj.getJSONArray(key)
                    key.normalizeNFC() to parseStringArray(jsonArray)
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    fun parseSurnameHanjaPairMapping(json: String): Map<String, Any> {
        return parseJsonObject(json, "성씨 한자 매핑 파싱 실패") { jsonObj ->
            jsonObj.keys().asSequence().associate { key ->
                val value = jsonObj.get(key)
                key.normalizeNFC() to when (value) {
                    is JSONArray -> parseStringArray(value)
                    is String -> value.normalizeNFC()
                    else -> value
                }
            }.toMap()
        }
    }

    fun parseDictHangulGivenNames(json: String): Set<String> {
        return try {
            parseJsonArray(json, "한글 이름 사전 파싱") { jsonArray ->
                parseStringArray(jsonArray).toSet()
            }
        } catch (e: Exception) {
            try {
                parseJsonObject(json, "한글 이름 사전 파싱") { jsonObj ->
                    jsonObj.keys().asSequence().map { it.normalizeNFC() }.toSet()
                }
            } catch (e2: Exception) {
                emptySet()
            }
        }
    }

    fun parseStrokeMeanings(json: String): Map<Int, Map<String, Any>> {
        return parseJsonObject(json, "획수 의미 데이터 파싱 실패") { jsonObj ->
            val strokeMeaningsObj = jsonObj.getJSONObject("stroke_meanings")

            strokeMeaningsObj.keys().asSequence().mapNotNull { key ->
                try {
                    val number = key.toInt()
                    val meaningObj = strokeMeaningsObj.getJSONObject(key)

                    number to mapOf<String, Any>(  // 타입 명시
                        "number" to meaningObj.getInt("number"),
                        "title" to meaningObj.getString("title").normalizeNFC(),
                        "summary" to meaningObj.getString("summary").normalizeNFC(),
                        "detailed_explanation" to meaningObj.getString("detailed_explanation").normalizeNFC(),
                        "positive_aspects" to meaningObj.getString("positive_aspects").normalizeNFC(),
                        "caution_points" to meaningObj.getString("caution_points").normalizeNFC(),
                        "personality_traits" to parseStringArray(meaningObj.getJSONArray("personality_traits")),
                        "suitable_career" to parseStringArray(meaningObj.getJSONArray("suitable_career")),
                        "life_period_influence" to meaningObj.getString("life_period_influence").normalizeNFC(),
                        // null 대신 빈 문자열 사용
                        "special_characteristics" to (meaningObj.optString("special_characteristics", "").normalizeNFC()),
                        "challenge_period" to (meaningObj.optString("challenge_period", "").normalizeNFC()),
                        "opportunity_area" to (meaningObj.optString("opportunity_area", "").normalizeNFC()),
                        "lucky_level" to meaningObj.getString("lucky_level").normalizeNFC()
                    )
                } catch (e: Exception) {
                    null
                }
            }.toMap()
        }
    }

    // 제네릭 파싱 메소드들
    private inline fun <T> parseJsonArray(
        json: String,
        errorMessage: String,
        parser: (JSONArray) -> T
    ): T = parseJsonSafely(json, errorMessage, "JSON Array") {
        parser(JSONArray(json))
    }

    private inline fun <T> parseJsonObject(
        json: String,
        errorMessage: String,
        parser: (JSONObject) -> T
    ): T = parseJsonSafely(json, errorMessage, "JSON Object") {
        parser(JSONObject(json))
    }

    private inline fun <T> parseJsonSafely(
        json: String,
        errorMessage: String,
        dataType: String,
        parser: () -> T
    ): T {
        return try {
            parser()
        } catch (e: JSONException) {
            throw NamingException.DataNotFoundException(
                errorMessage,
                dataType = dataType,
                cause = e
            )
        }
    }

    private fun parseStringArray(jsonArray: JSONArray): List<String> {
        return (0 until jsonArray.length()).map {
            jsonArray.getString(it).normalizeNFC()
        }
    }

    private fun parseInnerObject(jsonObj: JSONObject): Map<String, Any> {
        return jsonObj.keys().asSequence().associate { key ->
            key.normalizeNFC() to normalizeValue(jsonObj.get(key))
        }.toMap()
    }

    private fun normalizeValue(value: Any): Any {
        return when (value) {
            is String -> value.normalizeNFC()
            is JSONObject -> parseInnerObject(value)
            is JSONArray -> (0 until value.length()).map { normalizeValue(value.get(it)) }
            else -> value
        }
    }
}