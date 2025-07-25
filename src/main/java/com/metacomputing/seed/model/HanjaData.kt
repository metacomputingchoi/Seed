package com.metacomputing.seed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HanjaTripleData(
    @SerialName("한글정보")
    val koreanInfo: KoreanInfo,

    @SerialName("한자정보")
    val hanjaInfo: HanjaDetailInfo,

    @SerialName("통합정보")
    val integratedInfo: IntegratedInfo
)

@Serializable
data class KoreanInfo(
    @SerialName("글자")
    val character: String,

    @SerialName("뜻")
    val meaning: String? = null,

    @SerialName("음")
    val sound: String,

    @SerialName("음양")
    val eumyang: Int,

    @SerialName("오행")
    val oheng: String,

    @SerialName("획수")
    val strokes: Int,

    @SerialName("원획수")
    val originalStrokes: Int
)

@Serializable
data class HanjaDetailInfo(
    @SerialName("글자")
    val character: String,

    @SerialName("뜻")
    val meaning: String? = null,

    @SerialName("음")
    val sound: String,

    @SerialName("음양")
    val eumyang: Int,

    @SerialName("오행")
    val oheng: String,

    @SerialName("획수")
    val strokes: Int,

    @SerialName("원획수")
    val originalStrokes: Int
)

@Serializable
data class IntegratedInfo(
    @SerialName("한자")
    val hanja: String,

    @SerialName("인명용 뜻")
    val nameMeaning: String? = null,

    @SerialName("인명용 음")
    val nameSound: String,

    @SerialName("발음음양")
    val soundEumyang: Int,

    @SerialName("획수음양")
    val strokeEumyang: Int,

    @SerialName("발음오행")
    val soundOheng: String,

    @SerialName("자원오행")
    val resourceOheng: String,

    @SerialName("원획수")
    val originalStrokes: Int,

    @SerialName("옥편획수")
    val dictionaryStrokes: Int,

    @SerialName("E")
    val english: String? = null,

    @SerialName("CAUTION_RED")
    val cautionRed: String? = null,

    @SerialName("CAUTION_BLUE")
    val cautionBlue: String? = null
)
