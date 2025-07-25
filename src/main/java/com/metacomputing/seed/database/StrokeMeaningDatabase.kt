package com.metacomputing.seed.database

import com.metacomputing.seed.model.StrokeData
import com.metacomputing.seed.model.StrokeMeaning
import kotlinx.serialization.json.Json
import java.io.File

class StrokeMeaningDatabase {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var strokeData: StrokeData? = null

    init {
        loadStrokeData()
    }

    private fun loadStrokeData() {
        try {
            val resourcePath = "resources/seed/data/stroke_data.json"
            val file = File(resourcePath)

            strokeData = if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString(StrokeData.serializer(), jsonString)
            } else {
                // 리소스에서 로드
                val resourceStream = this::class.java.classLoader.getResourceAsStream(
                    "seed/data/stroke_data.json"
                )
                resourceStream?.use { stream ->
                    val jsonString = stream.bufferedReader().use { it.readText() }
                    json.decodeFromString(StrokeData.serializer(), jsonString)
                }
            }
        } catch (e: Exception) {
            println("획수 의미 데이터 로드 실패: ${e.message}")
        }
    }

    fun getStrokeMeaning(strokes: Int): StrokeMeaning? {
        return strokeData?.strokeMeanings?.get(strokes.toString())
    }
}
