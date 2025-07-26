// Main.kt
package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.model.NameInput
import com.metacomputing.seed.statistics.NameStatisticsLoader
import com.metacomputing.seed.statistics.NameStatisticsAnalyzer
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.search.NameQueryParser
import com.metacomputing.seed.search.NameSearchEngine

fun main() {
    val timePointResult = CalSDK.getTimePointData(
        year = 1986,
        month = 4,
        day = 19,
        hour = 5,
        minute = 45,
        timezOffset = -540,
        lang = 0
    )

    val nameInput = NameInput(
        surname = "최",
        surnameHanja = "崔",
        givenName = "성수",
        givenNameHanja = "成秀",
        timePointResult = timePointResult
    )

    println("=== 사주 정보 ===")
    println("생년월일시: ${nameInput.timePointResult.dateTime}")
    println("사주: ${nameInput.timePointResult.sexagenaryInfo.year} ${nameInput.timePointResult.sexagenaryInfo.month} ${nameInput.timePointResult.sexagenaryInfo.day} ${nameInput.timePointResult.sexagenaryInfo.hour}")
    println()

    println("=== 한자 정보 ===")
    val hanjaDB = HanjaDatabase()

    val surnameInfo = hanjaDB.getHanjaInfo(nameInput.surname, nameInput.surnameHanja, true)
    if (surnameInfo != null) {
        println("성씨: ${nameInput.surname}(${nameInput.surnameHanja})")
        println("  원획수: ${surnameInfo.originalStrokes}")
        println("  발음오행: ${surnameInfo.soundOhaeng}")
        println("  획수오행: ${surnameInfo.strokeElement}")
        println("  자원오행: ${surnameInfo.sourceElement}")
        println("  발음음양: ${if (surnameInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
        println("  획수음양: ${if (surnameInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
    }

    nameInput.givenName.forEachIndexed { index, char ->
        val hanjaChar = nameInput.givenNameHanja.getOrNull(index)?.toString() ?: ""
        val nameInfo = hanjaDB.getHanjaInfo(char.toString(), hanjaChar, false)
        if (nameInfo != null) {
            println("이름: $char($hanjaChar)")
            println("  원획수: ${nameInfo.originalStrokes}")
            println("  발음오행: ${nameInfo.soundOhaeng}")
            println("  획수오행: ${nameInfo.strokeElement}")
            println("  자원오행: ${nameInfo.sourceElement}")
            println("  발음음양: ${if (nameInfo.soundEumyang == 0) "음(陰)" else "양(陽)"}")
            println("  획수음양: ${if (nameInfo.strokeEumyang == 0) "음(陰)" else "양(陽)"}")
        }
    }
    println()

    val statsLoader = NameStatisticsLoader()
    val nameStats = statsLoader.loadStatistics()
    val statsAnalyzer = NameStatisticsAnalyzer()

    println("=== 이름 통계 정보 ===")
    val givenNameStats = nameStats[nameInput.givenName]
    if (givenNameStats != null) {
        println("이름: ${nameInput.givenName}")

        if (givenNameStats.similarNames.isNotEmpty()) {
            println("유사 이름: ${givenNameStats.similarNames.take(5).joinToString(", ")}...")
        }

        val popularity = statsAnalyzer.analyzePopularity(givenNameStats)
        println("인기도 분석:")
        println("  최고 순위: ${popularity.highestRank}위 (${popularity.highestRankYear}년)")
        println("  최근 순위: ${popularity.recentRank}위 (${popularity.recentYear}년)")
        println("  순위 추세: ${popularity.trend}")

        val genderDist = statsAnalyzer.analyzeGenderDistribution(givenNameStats)
        println("성별 분포:")
        println("  남자: ${genderDist.malePercentage}%")
        println("  여자: ${genderDist.femalePercentage}%")
        println("  성별 특성: ${genderDist.genderCharacteristic}")
    } else {
        println("'${nameInput.givenName}'에 대한 통계 정보가 없습니다.")
    }
    println()

    val analyzer = NameAnalyzer()
    val evaluation = analyzer.analyze(nameInput)

    println("=== 이름 평가 결과 ===")
    println("성명: ${nameInput.surname}${nameInput.givenName} (${nameInput.surnameHanja}${nameInput.givenNameHanja})")
    println()
    println("종합 점수: ${evaluation.totalScore}/100")
    println()

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

    println("[오행 분석]")

    println("사격수리 오행 배치 (이격-형격-원격): ${evaluation.sageokSuriOhaeng.arrangement.joinToString("")}")
    println("사격수리 오행 분포:")
    evaluation.sageokSuriOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("획수 오행 배치 (성명 순서): ${evaluation.hoeksuOhaeng.arrangement.joinToString("")}")
    println("획수 오행:")
    evaluation.hoeksuOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("발음 오행 배치 (성명 순서): ${evaluation.baleumOhaeng.arrangement.joinToString("")}")
    println("발음 오행:")
    evaluation.baleumOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("자원오행 (이름에서만):")
    evaluation.jawonOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("사주 오행:")
    evaluation.sajuOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }

    println("사주이름오행 (사주 + 이름 획수오행):")
    evaluation.sajuNameOhaeng.ohaengDistribution.forEach { (element, count) ->
        println("  $element: $count 개")
    }
    println()

    println("[음양 분석]")

    println("사격수리 음양 배치 (이격-형격-원격): ${evaluation.sageokSuriEumYang.arrangement.joinToString("")}")
    println("사격수리 음양 - 음: ${evaluation.sageokSuriEumYang.eumCount}, 양: ${evaluation.sageokSuriEumYang.yangCount}")

    println("사주 음양 - 음: ${evaluation.sajuEumYang.eumCount}, 양: ${evaluation.sajuEumYang.yangCount}")

    println("획수 음양 배치 (성명 순서): ${evaluation.hoeksuEumYang.arrangement.joinToString("")}")
    println("획수 음양 - 음: ${evaluation.hoeksuEumYang.eumCount}, 양: ${evaluation.hoeksuEumYang.yangCount}")

    println("발음 음양 배치 (성명 순서): ${evaluation.baleumEumYang.arrangement.joinToString("")}")
    println("발음 음양 - 음: ${evaluation.baleumEumYang.eumCount}, 양: ${evaluation.baleumEumYang.yangCount}")

    testNameSearch()
}

fun testNameSearch() {
    val hanjaDB = HanjaDatabase()
    val statsLoader = NameStatisticsLoader()
    val searchEngine = NameSearchEngine(hanjaDB, statsLoader)
    val parser = NameQueryParser(hanjaDB)

    // 전체 유효한 조합 수 출력 (디버깅용)
    println("=== 통계 정보 ===")
    val stats = statsLoader.loadStatistics()
    var totalCombinations = 0
    var namesWithHanja = 0
    var namesWithoutHanja = 0

    stats.forEach { (name, stat) ->
        if (stat.hanjaCombinations.isNotEmpty()) {
            totalCombinations += stat.hanjaCombinations.size
            namesWithHanja++
        } else {
            namesWithoutHanja++
        }
    }

    println("전체 이름 수: ${stats.size}")
    println("한자 조합이 있는 이름: $namesWithHanja")
    println("한자 조합이 없는 이름: $namesWithoutHanja")
    println("전체 유효한 이름 조합 수: $totalCombinations")

    // 테스트 케이스들 - 복성 테스트 추가
    val testCases = listOf(
        "[최/崔][_/_][_/_]",
        "[제갈/諸葛][ㅓ/_][ㅅ/_]",     // 복성을 한 블록으로
        "[제갈/諸葛][_/秀]",            // 복성을 한 블록으로
        "[제/諸][갈/葛][ㅓ/_][ㅅ/_]",  // 복성을 두 블록으로 (기존 방식)
        "[최/崔][_/_]",
        "[최/崔][성/成]",
        "[최/崔][_/成][수/_]",
        "[최/崔][성/成][수/秀]",
        "[최/崔][ㅅ/_][_/_]",
        "[최/崔][_/_][ㅜ/_]",
        "[김/金][성/成][숙/秀]",
        "[제/諸][갈/_][_/_]",
        "[제갈/諸葛][성/成][수/秀]"      // 제갈성수 테스트
    )

    println("\n=== 이름 검색 테스트 ===")

    testCases.forEach { testCase ->
        println("\n검색 조건: $testCase")

        val query = parser.parse(testCase)
        println("파싱 결과 - 성씨: ${query.surnameBlocks.size}블록, 이름: ${query.nameBlocks.size}블록")

        val results = searchEngine.search(query)

        println("검색 결과: ${results.size}개")

        // 처음 20개만 출력
        results.take(20).forEach { result ->
            println("  ${result.fullName} (${result.fullNameHanja})")
        }

        if (results.size > 20) {
            println("  ... 외 ${results.size - 20}개")
        }
    }
}