package com.metacomputing.seed

import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.model.NameInput
import com.metacomputing.seed.statistics.NameStatisticsLoader
import com.metacomputing.seed.statistics.NameStatisticsAnalyzer
import com.metacomputing.seed.database.HanjaDatabase

fun main() {
    // 이름 입력 생성 (TimePointResult 자동 계산)
    val nameInput = NameInput.create(
        surname = "최",
        surnameHanja = "崔",
        givenName = "성수",
        givenNameHanja = "成秀",
        birthYear = 1986,
        birthMonth = 4,
        birthDay = 19,
        birthHour = 5,
        birthMinute = 45,
        timezoneOffset = -540  // 한국 시간 (GMT+9)
    )

    println("=== 사주 정보 ===")
    println("생년월일시: ${nameInput.birthDateTime}")
    println("사주: ${nameInput.timePointResult.sexagenaryInfo.year} ${nameInput.timePointResult.sexagenaryInfo.month} ${nameInput.timePointResult.sexagenaryInfo.day} ${nameInput.timePointResult.sexagenaryInfo.hour}")
    println()

    // 한자 정보 출력
    println("=== 한자 정보 ===")
    val hanjaDB = HanjaDatabase()

    // 성씨 한자 정보
    val surnameInfo = hanjaDB.getHanjaInfo(nameInput.surname, nameInput.surnameHanja, true)
    if (surnameInfo != null) {
        println("성씨: ${nameInput.surname}(${nameInput.surnameHanja})")
        println("  원획수: ${surnameInfo.originalStrokes}")
        println("  발음오행: ${surnameInfo.soundOhaeng}")
        println("  획수오행: ${surnameInfo.strokeElement}")  // strokeElement 사용
        println("  자원오행: ${surnameInfo.sourceElement}")  // sourceElement 표시
        println("  발음음양: ${if (surnameInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
        println("  획수음양: ${if (surnameInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
    }

// 이름 한자 정보
    nameInput.givenName.forEachIndexed { index, char ->
        val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
        val nameInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
        if (nameInfo != null) {
            println("이름: $char($hanjaChar)")
            println("  원획수: ${nameInfo.originalStrokes}")
            println("  발음오행: ${nameInfo.soundOhaeng}")
            println("  획수오행: ${nameInfo.strokeElement}")  // strokeElement 사용
            println("  자원오행: ${nameInfo.sourceElement}")  // sourceElement 표시
            println("  발음음양: ${if (nameInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
            println("  획수음양: ${if (nameInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
        }
    }
    println()

    // 이름 통계 정보 로드
    val statsLoader = NameStatisticsLoader()
    val nameStats = statsLoader.loadStatistics()
    val statsAnalyzer = NameStatisticsAnalyzer()

    // 통계 정보 출력
    println("=== 이름 통계 정보 ===")
    val givenNameStats = nameStats[nameInput.givenName]
    if (givenNameStats != null) {
        println("이름: ${nameInput.givenName}")

        // 유사 이름
        if (givenNameStats.similarNames.isNotEmpty()) {
            println("유사 이름: ${givenNameStats.similarNames.take(5).joinToString(", ")}...")
        }

        // 인기도 분석
        val popularity = statsAnalyzer.analyzePopularity(givenNameStats)
        println("인기도 분석:")
        println("  최고 순위: ${popularity.highestRank}위 (${popularity.highestRankYear}년)")
        println("  최근 순위: ${popularity.recentRank}위 (${popularity.recentYear}년)")
        println("  순위 추세: ${popularity.trend}")

        // 성별 분포
        val genderDist = statsAnalyzer.analyzeGenderDistribution(givenNameStats)
        println("성별 분포:")
        println("  남자: ${genderDist.malePercentage}%")
        println("  여자: ${genderDist.femalePercentage}%")
        println("  성별 특성: ${genderDist.genderCharacteristic}")
    } else {
        println("'${nameInput.givenName}'에 대한 통계 정보가 없습니다.")
    }
    println()

    // 이름 분석
    val analyzer = NameAnalyzer()
    val evaluation = analyzer.analyze(nameInput)

    // 결과 출력
    println("=== 이름 평가 결과 ===")
    println("성명: ${nameInput.surname}${nameInput.givenName} (${nameInput.surnameHanja}${nameInput.givenNameHanja})")
    println()
    println("종합 점수: ${evaluation.totalScore}/100")
    println()

    // 원형이정 사격수리
    println("[원형이정 사격수리]")
    println("원격(元格): ${evaluation.sageokSuri.wonGyeok}획 - ${evaluation.sageokSuri.wonGyeokFortune}")
    println("  ${evaluation.sageokSuri.wonGyeokMeaning}")
    println("형격(亨格): ${evaluation.sageokSuri.hyeongGyeok}획 - ${evaluation.sageokSuri.hyeongGyeokFortune}")
    println("  ${evaluation.sageokSuri.hyeongGyeokMeaning}")
    println("이격(利格): ${evaluation.sageokSuri.iGyeok}획 - ${evaluation.sageokSuri.iGyeokFortune}")
    println("  ${evaluation.sageokSuri.iGyeokMeaning}")
    println("정격(貞格): ${evaluation.sageokSuri.jeongGyeok}획 - ${evaluation.sageokSuri.jeongGyeokFortune}")
    println("  ${evaluation.sageokSuri.jeongGyeokMeaning}")
    println()

    // 오행 분석
    println("[오행 분석]")

    // 사격수리 오행 배치
    println("사격수리 오행 배치 (이격-형격-원격): ${evaluation.sageokSuriOhaeng.arrangement.joinToString("")}")
    println("사격수리 오행 분포:")
    evaluation.sageokSuriOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    // 획수 오행 배치
    println("획수 오행 배치 (성명 순서): ${evaluation.hoeksuOhaeng.arrangement.joinToString("")}")
    println("획수 오행:")
    evaluation.hoeksuOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    // 발음 오행 배치
    println("발음 오행 배치 (성명 순서): ${evaluation.baleumOhaeng.arrangement.joinToString("")}")
    println("발음 오행:")
    evaluation.baleumOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("자원오행 (이름에서만):")
    evaluation.jawonOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    // 사주 오행
    println("사주 오행:")
    evaluation.sajuOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    // 사주이름오행 (사주 + 이름 획수오행)
    println("사주이름오행 (사주 + 이름 획수오행):")
    evaluation.sajuNameOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println()

    // 음양 분석
    println("[음양 분석]")

    // 사격수리 음양 배치
    println("사격수리 음양 배치 (이격-형격-원격): ${evaluation.sageokSuriEumYang.arrangement.joinToString("")}")
    println("사격수리 음양 - 음: ${evaluation.sageokSuriEumYang.eumCount}, 양: ${evaluation.sageokSuriEumYang.yangCount}")

    println("사주 음양 - 음: ${evaluation.sajuEumYang.eumCount}, 양: ${evaluation.sajuEumYang.yangCount}")

    // 획수 음양 배치
    println("획수 음양 배치 (성명 순서): ${evaluation.hoeksuEumYang.arrangement.joinToString("")}")
    println("획수 음양 - 음: ${evaluation.hoeksuEumYang.eumCount}, 양: ${evaluation.hoeksuEumYang.yangCount}")

    // 발음 음양 배치
    println("발음 음양 배치 (성명 순서): ${evaluation.baleumEumYang.arrangement.joinToString("")}")
    println("발음 음양 - 음: ${evaluation.baleumEumYang.eumCount}, 양: ${evaluation.baleumEumYang.yangCount}")
}
