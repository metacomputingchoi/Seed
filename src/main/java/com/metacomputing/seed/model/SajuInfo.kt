// model/SajuInfo.kt
package com.metacomputing.seed.model

data class SajuInfo(
    val yearStem: String,
    val yearBranch: String,
    val monthStem: String,
    val monthBranch: String,
    val dayStem: String,
    val dayBranch: String,
    val hourStem: String,
    val hourBranch: String
) {
    fun toList(): List<String> = listOf(
        yearStem, yearBranch,
        monthStem, monthBranch,
        dayStem, dayBranch,
        hourStem, hourBranch
    )
}
