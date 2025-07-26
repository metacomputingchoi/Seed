// database/StrokeMeaningDatabase.kt
package com.metacomputing.seed.database

import com.metacomputing.seed.model.StrokeData
import com.metacomputing.seed.util.ResourceLoader

class StrokeMeaningDatabase {
    private val strokeData: StrokeData? by lazy {
        try {
            ResourceLoader.loadStrokeData("stroke_data.json")
        } catch (e: Exception) {
            null
        }
    }

    fun getStrokeMeaning(strokes: Int) = strokeData?.strokeMeanings?.get(strokes.toString())
}