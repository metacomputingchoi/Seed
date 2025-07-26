// util/PassFailUtil.kt - 더 엄격한 오행 상극 체크
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

        // 1. 전체 배열에서 상극이 하나라도 있으면 불합격
        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return false
            }
        }

        // 2. 같은 오행이 3개 이상 연속되면 불합격
        var consecutiveCount = 1
        for (i in 1 until arrangement.size) {
            if (arrangement[i] == arrangement[i - 1]) {
                consecutiveCount++
                if (consecutiveCount >= 3) {
                    return false
                }
            } else {
                consecutiveCount = 1
            }
        }

        val firstChar = arrangement[0]
        val lastChar = arrangement.last()

        if (OhaengRelationUtil.isSangGeuk(firstChar, lastChar)) {
            return false
        }

        var sangSaengCount = 0
        var totalRelations = 0

        for (i in 0 until arrangement.size - 1) {
            if (arrangement[i] != arrangement[i + 1]) {  // 같은 오행은 제외
                totalRelations++
                if (OhaengRelationUtil.isSangSaeng(arrangement[i], arrangement[i + 1])) {
                    sangSaengCount++
                }
            }
        }

        // 관계가 있는 경우, 상생이 60% 이상이어야 함
        if (totalRelations > 0) {
            val sangSaengRatio = sangSaengCount.toDouble() / totalRelations
            if (sangSaengRatio < 0.6) {
                return false
            }
        }

        // 5. 특별 보너스: 성 첫-이름 끝이 상생이고 전체에 상극이 없으면 최고
        val isPerfect = OhaengRelationUtil.isSangSaeng(firstChar, lastChar) &&
                !hasAnySangGeuk(arrangement)

        return true  // 모든 조건 통과
    }

    // 더 엄격한 상극 체크 (사격수리오행용)
    fun checkSageokSuriOhaeng(arrangement: List<String>): Boolean {
        if (arrangement.size != 3) return false  // 이격-형격-원격 3개여야 함

        // 1. 상극이 하나라도 있으면 불합격
        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return false
            }
        }

        // 2. 순환 관계도 체크 (원격 -> 이격)
        if (OhaengRelationUtil.isSangGeuk(arrangement.last(), arrangement.first())) {
            return false
        }

        // 3. 모든 오행이 같으면 불합격 (정체됨)
        if (arrangement.toSet().size == 1) {
            return false
        }

        // 4. 이상적인 케이스: 순환 상생 (이격->형격->원격->이격)
        val isCircularSangSaeng = OhaengRelationUtil.isSangSaeng(arrangement[0], arrangement[1]) &&
                OhaengRelationUtil.isSangSaeng(arrangement[1], arrangement[2]) &&
                OhaengRelationUtil.isSangSaeng(arrangement[2], arrangement[0])

        return true
    }

    private fun hasAnySangGeuk(arrangement: List<String>): Boolean {
        for (i in 0 until arrangement.size - 1) {
            if (OhaengRelationUtil.isSangGeuk(arrangement[i], arrangement[i + 1])) {
                return true
            }
        }
        return false
    }
}