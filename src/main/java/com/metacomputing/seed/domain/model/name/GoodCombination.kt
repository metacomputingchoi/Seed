// domain/model/name/GoodCombination.kt
package com.metacomputing.seed.domain.model.name

/**
 * 좋은 이름 조합 정보
 * 수리적으로 검증된 이름 조합의 데이터
 */
data class GoodCombination(
    val nameHanjaHoeksu: List<Int>,      // 이름 한자 획수 목록
    val sagyeok: Sagyeok,                 // 사격 정보
    val nameBaleumEumyang: List<Int>,    // 이름 발음 음양 (0: 음, 1: 양)
    val nameHoeksuOhaeng: List<Int>,   // 이름 획수 오행 (木, 火, 土, 金, 水)  <- String으로 변경
    val sagyeokSuriOhaeng: List<Int>   // 사격 수리 오행 (木, 火, 土, 金, 水)  <- String으로 변경
)