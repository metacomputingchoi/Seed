// domain/model/name/Sagyeok.kt
package com.metacomputing.seed.domain.model.name

data class Sagyeok(
    val won: Int,
    val hyeong: Int,
    val i: Int,
    val jeong: Int
) {
    fun getValues() = listOf(won, hyeong, i, jeong)
}
