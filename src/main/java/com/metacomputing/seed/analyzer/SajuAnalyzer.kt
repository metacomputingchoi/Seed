package com.metacomputing.seed.analyzer

import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.model.SajuInfo

class SajuAnalyzer {
    fun extractSajuInfo(timePointResult: TimePointResult): SajuInfo {
        val result = timePointResult

        return SajuInfo(
            yearStem = result.sexagenaryInfo.year.substring(0, 1),
            yearBranch = result.sexagenaryInfo.year.substring(1, 2),
            monthStem = result.sexagenaryInfo.month.substring(0, 1),
            monthBranch = result.sexagenaryInfo.month.substring(1, 2),
            dayStem = result.sexagenaryInfo.day.substring(0, 1),
            dayBranch = result.sexagenaryInfo.day.substring(1, 2),
            hourStem = result.sexagenaryInfo.hour.substring(0, 1),
            hourBranch = result.sexagenaryInfo.hour.substring(1, 2)
        )
    }
}
