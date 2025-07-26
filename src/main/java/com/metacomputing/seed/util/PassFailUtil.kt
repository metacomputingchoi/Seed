// util/PassFailUtil.kt (새 파일)
package com.metacomputing.seed.util

object PassFailUtil {

    // 음양 배열에서 성 첫글자와 이름 끝글자 체크
    fun checkEumYangFirstLast(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.isEmpty()) return false

        val firstChar = arrangement[0]  // 성의 첫글자
        val lastChar = arrangement.last()  // 이름의 끝글자

        // 성의 첫글자와 이름의 끝글자가 같은 음양이면 불합격
        return firstChar != lastChar
    }

    fun checkEumYangHarmony(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true

        // 성의 첫글자와 이름의 끝글자가 다른지만 확인
        if (!checkEumYangFirstLast(arrangement, surnameLength)) return false

        // 그 외에는 극단적인 불균형만 제외 (전부 음 또는 전부 양인 경우만 제외)
        val eumCount = arrangement.count { it == "음" }
        val yangCount = arrangement.count { it == "양" }

        return eumCount > 0 && yangCount > 0  // 음양이 최소 1개씩은 있어야 함
    }

    fun checkOhaengSangSaeng(arrangement: List<String>, surnameLength: Int): Boolean {
        if (arrangement.size < 2) return true

        // 성의 첫글자와 이름의 끝글자
        val firstChar = arrangement[0]
        val lastChar = arrangement.last()

        // 1. 최우선: 성 첫글자와 이름 끝글자가 상생이면 무조건 통과
        if (OhaengRelationUtil.isSangSaeng(firstChar, lastChar)) {
            return true
        }

        // 2. 성 첫글자와 이름 끝글자가 상극이면 무조건 불합격
        if (OhaengRelationUtil.isSangGeuk(firstChar, lastChar)) {
            return false
        }

        // 3. 성 첫글자와 이름 끝글자가 같은 오행이거나 비상생/비상극 관계일 때
        // 전체 배열에서 상극이 없는지 체크 (단, 같은 오행 연속은 허용)
        for (i in 0 until arrangement.size - 1) {
            // 같은 오행이 연속되는 것은 상극으로 보지 않음
            if (arrangement[i] == arrangement[i + 1]) {
                continue
            }

            // 실제 상극 관계만 체크
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return false
            }
        }

        return true
    }
}