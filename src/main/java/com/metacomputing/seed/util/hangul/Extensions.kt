// util/hangul/Extensions.kt
package com.metacomputing.seed.util.hangul

import com.metacomputing.seed.domain.constants.HangulConstants
import java.text.Normalizer

fun String.normalizeNFC(): String = Normalizer.normalize(this, Normalizer.Form.NFC)

fun Char.toHangulDecomposition(): Triple<Int, Int, Int> {
    val code = this.code - HangulConstants.HANGUL_BASE
    val cho = code / HangulConstants.INITIAL_COUNT
    val jung = (code / HangulConstants.MEDIAL_COUNT) % HangulConstants.MEDIALS_PER_INITIAL
    val jong = code % HangulConstants.MEDIAL_COUNT
    return Triple(cho, jung, jong)
}

fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
    return lists.fold(listOf(listOf())) { acc, list ->
        acc.flatMap { combination ->
            list.map { element -> combination + element }
        }
    }
}

fun <T> Iterable<T>.countMatching(predicate: (T) -> Boolean): Int {
    var count = 0
    for (element in this) {
        if (predicate(element)) count++
    }
    return count
}