// database/StrokeMeaningDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.StrokeData
import kotlinx.serialization.json.Json
import java.io.File

class StrokeMeaningDatabase {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private var strokeData: StrokeData? = null

    init { loadStrokeData() }

    private fun loadStrokeData() {
        val resourcePath = "resources/seed/data/stroke_data.json"
        val file = File(resourcePath)

        strokeData = if (file.exists()) {
            json.decodeFromString(StrokeData.serializer(), file.readText())
        } else {
            javaClass.classLoader.getResourceAsStream("seed/data/stroke_data.json")?.use { stream ->
                json.decodeFromString(StrokeData.serializer(), stream.bufferedReader().use { it.readText() })
            }
        }
    }

    fun getStrokeMeaning(strokes: Int) = strokeData?.strokeMeanings?.get(strokes.toString())
}