// util/BaleumAnalyzer.kt
package com.metacomputing.seed.util

import com.metacomputing.seed.*

object BaleumAnalyzer {
    fun calculateBaleumOhaeng(korean: String): String {
        if (korean.isEmpty()) return "土"
        return Constants.CHOSUNG_OHAENG[korean.first().extractChosung().firstOrNull()] ?: "土"
    }

    fun calculateSoundEumyang(korean: String): Int {
        if (korean.isEmpty()) return 0
        return if (Constants.YANG_JUNGSUNG.contains(korean.first().extractJungsung().firstOrNull())) 1 else 0
    }

    fun calculateStrokeEumyang(strokes: Int) = if (strokes % 2 == 1) 1 else 0
}