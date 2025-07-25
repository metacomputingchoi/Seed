package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK.getTimePointData
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.model.NameInput
import com.metacomputing.seed.statistics.NameStatisticsLoader
import com.metacomputing.seed.statistics.NameStatisticsAnalyzer
import com.metacomputing.seed.database.HanjaDatabase

fun main() {
    // 사주 정보 가져오기
    val result = getTimePointData(
        year = 1990,
        month = 5,
        day = 15,
        hour = 15,
        minute = 30,
        timezOffset = -540,  // 한국 시간 (GMT+9)
        lang = 0             // 0: 한국어, 1: 영어
    )

    println("=== 사주 정보 ===")
    println("생년월일시: ${result.dateTime.localTime}")
    println("사주: ${result.sexagenaryInfo.year} ${result.sexagenaryInfo.month} ${result.sexagenaryInfo.day} ${result.sexagenaryInfo.hour}")
    println()

    // 이름 입력 예시
    val nameInput = NameInput(
        surname = "김",
        surnameHanja = "金",
        givenName = "민수",
        givenNameHanja = "民秀",
        birthYear = 1990,
        birthMonth = 5,
        birthDay = 15,
        birthHour = 15,
        birthMinute = 30,
        timezoneOffset = -540
    )

    // 한자 정보 출력
    println("=== 한자 정보 ===")
    val hanjaDB = HanjaDatabase()

    // 성씨 한자 정보
    val surnameInfo = hanjaDB.getHanjaInfo(nameInput.surname, nameInput.surnameHanja, true)
    if (surnameInfo != null) {
        println("성씨: ${nameInput.surname}(${nameInput.surnameHanja})")
        println("  원획수: ${surnameInfo.integratedInfo.originalStrokes}")
        println("  발음오행: ${surnameInfo.integratedInfo.soundOheng}")
        println("  획수오행: ${surnameInfo.integratedInfo.resourceOheng}")
        println("  발음음양: ${if (surnameInfo.integratedInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
        println("  획수음양: ${if (surnameInfo.integratedInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
    }

    // 이름 한자 정보
    nameInput.givenName.forEachIndexed { index, char ->
        val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
        val nameInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
        if (nameInfo != null) {
            println("이름: $char($hanjaChar)")
            println("  원획수: ${nameInfo.integratedInfo.originalStrokes}")
            println("  발음오행: ${nameInfo.integratedInfo.soundOheng}")
            println("  획수오행: ${nameInfo.integratedInfo.resourceOheng}")
            println("  발음음양: ${if (nameInfo.integratedInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
            println("  획수음양: ${if (nameInfo.integratedInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
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
    val evaluation = analyzer.analyze(nameInput, result)

    // 결과 출력
    println("=== 이름 평가 결과 ===")
    println("성명: ${nameInput.surname}${nameInput.givenName} (${nameInput.surnameHanja}${nameInput.givenNameHanja})")
    println()
    println("종합 점수: ${evaluation.totalScore}/100")
    println()

    // 원형이정 사격수리
    println("[원형이정 사격수리]")
    println("원격(元格): ${evaluation.sageokSuri.wonGyeok}획 (${evaluation.sageokSuri.wonGyeokFortune})")
    println("형격(亨格): ${evaluation.sageokSuri.hyeongGyeok}획 (${evaluation.sageokSuri.hyeongGyeokFortune})")
    println("이격(利格): ${evaluation.sageokSuri.iGyeok}획 (${evaluation.sageokSuri.iGyeokFortune})")
    println("정격(貞格): ${evaluation.sageokSuri.jeongGyeok}획 (${evaluation.sageokSuri.jeongGyeokFortune})")
    println()

    // 오행 분석
    println("[오행 분석]")
    println("사격수리 오행:")
    evaluation.sageokSuriOheng.ohengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println("사주 오행:")
    evaluation.sajuOheng.ohengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println("획수 오행 (자원오행):")
    evaluation.hoeksuOheng.ohengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println("발음 오행:")
    evaluation.baleumOheng.ohengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println()

    // 음양 분석
    println("[음양 분석]")
    println("사격수리 음양 - 음: ${evaluation.sageokSuriEumYang.eumCount}, 양: ${evaluation.sageokSuriEumYang.yangCount}")
    println("사주 음양 - 음: ${evaluation.sajuEumYang.eumCount}, 양: ${evaluation.sajuEumYang.yangCount}")
    println("획수 음양 - 음: ${evaluation.hoeksuEumYang.eumCount}, 양: ${evaluation.hoeksuEumYang.yangCount}")
    println("발음 음양 - 음: ${evaluation.baleumEumYang.eumCount}, 양: ${evaluation.baleumEumYang.yangCount}")
}
