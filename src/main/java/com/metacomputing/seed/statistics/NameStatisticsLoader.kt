// statistics/NameStatisticsLoader.kt
package com.metacomputing.seed.statistics

import com.metacomputing.seed.model.NameStatistics
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

// statistics/NameStatisticsLoader.kt
class NameStatisticsLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var nameStatistics: Map<String, NameStatistics> = emptyMap()

    init {
        loadStatistics()
    }

    fun loadStatistics(): Map<String, NameStatistics> {
        return try {
            val resourcePath = "resources/seed/data/name_to_stat_minified.json"
            val file = File(resourcePath)

            nameStatistics = if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString(
                    MapSerializer(String.serializer(), NameStatistics.serializer()),
                    jsonString
                )
            } else {
                val resourceStream = this::class.java.classLoader.getResourceAsStream(
                    "seed/data/name_to_stat_minified.json"
                )
                resourceStream?.use { stream ->
                    val jsonString = stream.bufferedReader().use { it.readText() }
                    json.decodeFromString(
                        MapSerializer(String.serializer(), NameStatistics.serializer()),
                        jsonString
                    )
                } ?: emptyMap()
            }

            nameStatistics
        } catch (e: Exception) {
            println("통계 파일 로드 실패: ${e.message}")
            emptyMap()
        }
    }

    // 한자 조합이 유효한지 확인
    fun isValidHanjaCombination(givenName: String, givenNameHanja: String): Boolean {
        val stats = nameStatistics[givenName] ?: return true  // 통계가 없으면 허용

        // hanja_combinations가 비어있으면 모든 조합 허용
        if (stats.hanjaCombinations.isEmpty()) return true

        // hanja_combinations에 포함되어 있는지 확인
        return givenNameHanja in stats.hanjaCombinations
    }

    // 특정 이름의 유효한 한자 조합 목록 가져오기
    fun getValidHanjaCombinations(givenName: String): List<String> {
        return nameStatistics[givenName]?.hanjaCombinations ?: emptyList()
    }
}