package com.metacomputing.seed.model

data class NameBlock(
    val korean: String,
    val hanja: String
) {
    val isKoreanEmpty: Boolean = korean == "_" || korean.isEmpty()
    val isHanjaEmpty: Boolean = hanja == "_" || hanja.isEmpty()
    val isEmpty: Boolean = isKoreanEmpty && isHanjaEmpty

    val isCompleteKorean: Boolean = korean.length == 1 && korean[0] in '가'..'힣'

    val isChosungOnly: Boolean = korean.length == 1 && korean[0] in "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ"

    val isJungsungOnly: Boolean = korean.length == 1 && korean[0] in "ㅏㅐㅑㅒㅓㅔㅕㅖㅗㅘㅙㅚㅛㅜㅝㅞㅟㅠㅡㅢㅣ"
}

data class NameQuery(
    val surnameBlocks: List<NameBlock>,
    val nameBlocks: List<NameBlock>
) {
    val isSingleSurname: Boolean = surnameBlocks.size == 1
    val isDoubleSurname: Boolean = surnameBlocks.size == 2
}