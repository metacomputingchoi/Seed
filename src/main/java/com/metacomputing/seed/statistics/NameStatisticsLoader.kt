package com.metacomputing.seed.statistics

import com.metacomputing.seed.model.NameStatistics
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class NameStatisticsLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun loadStatistics(): Map<String, NameStatistics> {
        return try {
            val resourcePath = "resources/seed/data/name_to_stat_minified.json"
            val file = File(resourcePath)

            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString(
                    MapSerializer(String.serializer(), NameStatistics.serializer()),
                    jsonString
                )
            } else {
                // 리소스에서 로드 시도
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
        } catch (e: Exception) {
            println("통계 파일 로드 실패: ${e.message}")
            emptyMap()
        }
    }
}
