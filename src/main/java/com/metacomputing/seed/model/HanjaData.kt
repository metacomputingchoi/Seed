// model/HanjaData.kt
package com.metacomputing.seed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HanjaInfo(
    @SerialName("char_ko")
    val charKo: String,

    @SerialName("hanja")
    val hanja: String,

    @SerialName("meaning")
    val meaning: String,

    @SerialName("strokes")
    val strokes: String,

    @SerialName("stroke_element")
    val strokeElement: String,

    @SerialName("radical")
    val radical: String,

    @SerialName("source_element")
    val sourceElement: String
)