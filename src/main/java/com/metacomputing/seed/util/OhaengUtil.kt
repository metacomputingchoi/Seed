// util/OhaengUtil.kt
package com.metacomputing.seed.util

object OhaengUtil {
    val HEAVENLY_STEM_OHENG = mapOf(
        "甲" to "목", "乙" to "목",
        "丙" to "화", "丁" to "화",
        "戊" to "토", "己" to "토",
        "庚" to "금", "辛" to "금",
        "壬" to "수", "癸" to "수"
    )

    val EARTHLY_BRANCH_OHENG = mapOf(
        "子" to "수", "丑" to "토", "寅" to "목", "卯" to "목",
        "辰" to "토", "巳" to "화", "午" to "화", "未" to "토",
        "申" to "금", "酉" to "금", "戌" to "토", "亥" to "수"
    )

    val EARTHLY_BRANCH_HIDDEN_STEMS = mapOf(
        "子" to listOf("癸"),
        "丑" to listOf("己", "癸", "辛"),
        "寅" to listOf("甲", "丙", "戊"),
        "卯" to listOf("乙"),
        "辰" to listOf("戊", "乙", "癸"),
        "巳" to listOf("丙", "戊", "庚"),
        "午" to listOf("丁", "己"),
        "未" to listOf("己", "丁", "乙"),
        "申" to listOf("庚", "壬", "戊"),
        "酉" to listOf("辛"),
        "戌" to listOf("戊", "辛", "丁"),
        "亥" to listOf("壬", "甲")
    )

    fun getHiddenStemOhaeng(branch: String): List<String> {
        val hiddenStems = EARTHLY_BRANCH_HIDDEN_STEMS[branch] ?: emptyList()
        return hiddenStems.map { stem -> HEAVENLY_STEM_OHENG[stem] ?: "토" }
    }

    fun getOhaengByStroke(stroke: Int): String {
        val lastDigit = stroke % 10
        return when (lastDigit) {
            1, 2 -> "목"
            3, 4 -> "화"
            5, 6 -> "토"
            7, 8 -> "금"
            9, 0 -> "수"
            else -> "토"
        }
    }
}
