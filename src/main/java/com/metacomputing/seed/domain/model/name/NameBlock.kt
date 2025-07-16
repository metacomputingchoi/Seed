package com.metacomputing.seed.domain.model.name

data class NameBlock(
    val pronunciationChar: String,
    val chineseChar: String,
    val pronunciationPartiality: Boolean
) {
    override fun toString() = "[$pronunciationChar/$chineseChar]"
}