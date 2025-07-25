package com.metacomputing.seed.database

data class HanjaInfo(
    val character: String,
    val strokes: Int,
    val radicalOheng: String,  // 부수 오행
    val meaning: String
)

class HanjaDatabase {
    // 임시 한자 데이터베이스
    private val hanjaData = mapOf(
        "金" to HanjaInfo("金", 8, "금", "쇠, 금속"),
        "木" to HanjaInfo("木", 4, "목", "나무"),
        "水" to HanjaInfo("水", 4, "수", "물"),
        "火" to HanjaInfo("火", 4, "화", "불"),
        "土" to HanjaInfo("土", 3, "토", "흙"),
        "民" to HanjaInfo("民", 5, "토", "백성"),
        "秀" to HanjaInfo("秀", 7, "목", "빼어나다"),
        "李" to HanjaInfo("李", 7, "목", "자두나무, 성씨"),
        "王" to HanjaInfo("王", 4, "토", "임금"),
        "張" to HanjaInfo("張", 11, "화", "펴다, 성씨"),
        "朴" to HanjaInfo("朴", 6, "목", "순박하다, 성씨"),
        "崔" to HanjaInfo("崔", 11, "토", "높다, 성씨"),
        "鄭" to HanjaInfo("鄭", 19, "금", "나라이름, 성씨"),
        "姜" to HanjaInfo("姜", 9, "목", "생강, 성씨"),
        "趙" to HanjaInfo("趙", 14, "금", "나라이름, 성씨"),
        "尹" to HanjaInfo("尹", 4, "토", "다스리다, 성씨"),
        "林" to HanjaInfo("林", 8, "목", "숲"),
        "韓" to HanjaInfo("韓", 17, "토", "나라이름, 성씨")
    )

    fun getHanjaInfo(character: String): HanjaInfo? {
        return hanjaData[character]
    }

    fun getRadicalOheng(character: String): String {
        return hanjaData[character]?.radicalOheng ?: determineOhengByRadical(character)
    }

    private fun determineOhengByRadical(character: String): String {
        // TODO: 부수별 오행 판단 로직 구현
        // 木 부수: 木, 林, 森 등 -> 목
        // 火 부수: 火, 灬 등 -> 화
        // 土 부수: 土, 山 등 -> 토
        // 金 부수: 金, 钅 등 -> 금
        // 水 부수: 水, 氵 등 -> 수

        return "토"  // 기본값
    }
}
