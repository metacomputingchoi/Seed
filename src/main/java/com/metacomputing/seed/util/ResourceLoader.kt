// util/ResourceLoader.kt
package com.metacomputing.seed.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import com.metacomputing.seed.model.*
import java.io.File

object ResourceLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun loadHanjaDict(fileName: String): Map<String, HanjaInfo> {
        val jsonString = loadJsonString(fileName)
        return json.decodeFromString(
            MapSerializer(String.serializer(), HanjaInfo.serializer()),
            jsonString
        )
    }

    fun loadStringListMap(fileName: String): Map<String, List<String>> {
        val jsonString = loadJsonString(fileName)
        return json.decodeFromString(
            MapSerializer(String.serializer(), ListSerializer(String.serializer())),
            jsonString
        )
    }

    fun loadStatistics(fileName: String): Map<String, NameStatistics> {
        val jsonString = loadJsonString(fileName)
        return json.decodeFromString(
            MapSerializer(String.serializer(), NameStatistics.serializer()),
            jsonString
        )
    }

    fun loadStrokeData(fileName: String): StrokeData {
        val jsonString = loadJsonString(fileName)
        return json.decodeFromString(StrokeData.serializer(), jsonString)
    }

    private fun loadJsonString(fileName: String): String {
        val resourcePath = "resources/seed/data/$fileName"
        val file = File(resourcePath)

        return if (file.exists()) {
            file.readText()
        } else {
            this::class.java.classLoader.getResourceAsStream("seed/data/$fileName")
                ?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("리소스를 찾을 수 없음: $fileName")
        }
    }
}