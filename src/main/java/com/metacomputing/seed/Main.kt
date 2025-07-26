// Main.kt 수정 - 디버깅 정보 추가
package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.model.NameInput
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.model.NameEvaluation
import com.metacomputing.seed.model.NameQuery
import com.metacomputing.seed.search.NameQueryParser
import com.metacomputing.seed.search.NameSearchEngine
import com.metacomputing.seed.statistics.NameStatisticsLoader

fun main() {
    val timePoint = CalSDK.getTimePointData(1986, 4, 19, 5, 45, -540, 0)

    // 블록 포맷 입력 예시들
    val queries = listOf(
        "[최/崔][성/成][수/秀]",      // 완전한 이름
        "[최/崔][_/_][_/_]"         // 부분 검색
    )

    queries.forEach { query ->
        println("\n=== 입력: $query ===")
        val evaluations = processNameQuery(query, timePoint)

        println("평가 완료: ${evaluations.size}개")

        // 디버깅: 첫 번째 이름의 상세 평가 출력
        if (evaluations.isNotEmpty() && query.contains("[성/成][수/秀]")) {
            val (nameInput, evaluation) = evaluations.first()
            println("\n【 디버깅: ${nameInput.surname}${nameInput.givenName} 상세 평가 】")
            println("사격수리: ${evaluation.detailedScores.sageokSuriScore.isPassed} - ${evaluation.detailedScores.sageokSuriScore.reason}")
            println("사주이름오행: ${evaluation.detailedScores.sajuNameOhaengScore.isPassed} - ${evaluation.detailedScores.sajuNameOhaengScore.reason}")
            println("획수음양: ${evaluation.detailedScores.hoeksuEumYangScore.isPassed} - ${evaluation.detailedScores.hoeksuEumYangScore.reason}")
            println("발음오행: ${evaluation.detailedScores.baleumOhaengScore.isPassed} - ${evaluation.detailedScores.baleumOhaengScore.reason}")
            println("발음음양: ${evaluation.detailedScores.baleumEumYangScore.isPassed} - ${evaluation.detailedScores.baleumEumYangScore.reason}")
            println("사격수리오행: ${evaluation.detailedScores.sageokSuriOhaengScore.isPassed} - ${evaluation.detailedScores.sageokSuriOhaengScore.reason}")

            // 추가 디버깅 정보
            println("\n원시 데이터:")
            println("획수음양 배열: ${evaluation.hoeksuEumYang.arrangement}")
            println("발음오행 배열: ${evaluation.baleumOhaeng.arrangement}")
            println("발음음양 배열: ${evaluation.baleumEumYang.arrangement}")
            println("사격수리오행 배열: ${evaluation.sageokSuriOhaeng.arrangement}")
        }

        // 필수 항목들이 모두 통과한 것들만 필터링
        val filteredEvaluations = evaluations.filter { (_, evaluation) ->
            evaluation.detailedScores.sageokSuriScore.isPassed &&
                    evaluation.detailedScores.sajuNameOhaengScore.isPassed &&
                    evaluation.detailedScores.hoeksuEumYangScore.isPassed &&
                    evaluation.detailedScores.baleumOhaengScore.isPassed &&
                    evaluation.detailedScores.baleumEumYangScore.isPassed &&
                    evaluation.detailedScores.sageokSuriOhaengScore.isPassed
        }

        println("\n필터링 후: ${filteredEvaluations.size}개 (필수 항목 모두 통과)")

        // 필터링된 결과를 점수순으로 정렬하여 상위 10개 출력
        val topEvaluations = filteredEvaluations
            .sortedByDescending { it.second.totalScore }
            .take(10)

        if (topEvaluations.isEmpty()) {
            println("필수 항목을 모두 통과한 이름이 없습니다.")

            // 통과 실패 통계 (첫 100개 샘플)
            if (evaluations.size > 1) {
                val sampleSize = minOf(1000, evaluations.size)
                val sample = evaluations.take(sampleSize)

                val failStats = mapOf(
                    "사격수리" to sample.count { !it.second.detailedScores.sageokSuriScore.isPassed },
                    "사주이름오행" to sample.count { !it.second.detailedScores.sajuNameOhaengScore.isPassed },
                    "획수음양" to sample.count { !it.second.detailedScores.hoeksuEumYangScore.isPassed },
                    "발음오행" to sample.count { !it.second.detailedScores.baleumOhaengScore.isPassed },
                    "발음음양" to sample.count { !it.second.detailedScores.baleumEumYangScore.isPassed },
                    "사격수리오행" to sample.count { !it.second.detailedScores.sageokSuriOhaengScore.isPassed }
                )

                println("\n【 실패 원인 분석 (샘플 ${sampleSize}개) 】")
                failStats.forEach { (item, count) ->
                    println("- $item 불통과: ${count}개 (${count * 100 / sampleSize}%)")
                }
            }

            println("\n【 참고: 필수 통과 항목 】")
            println("- 사격수리: 4개 격 모두가 양운수 이상")
            println("- 사주이름오행: 사주의 부족한 오행을 보완")
            println("- 획수음양: 성 첫글자와 이름 끝글자가 다른 음양")
            println("- 발음오행: 상극이 없는 배열 (성첫-이름끝 상생 우선)")
            println("- 발음음양: 성 첫글자와 이름 끝글자가 다른 음양")
            println("- 사격수리오행: 상극이 없는 배열")
        } else {
            println("【 상위 10개 추천 이름 】")
            println("─".repeat(80))
            topEvaluations.forEachIndexed { index, (nameInput, evaluation) ->
                println("${index + 1}위. ${nameInput.surname}${nameInput.givenName} " +
                        "(${nameInput.surnameHanja}${nameInput.givenNameHanja}) - " +
                        "종합점수: ${evaluation.totalScore}점/100점")

                // 1위만 상세 출력
                if (index == 0) {
                    println("    ├─ 사주오행: ${evaluation.sajuOhaeng.ohaengDistribution}")
                    println("    ├─ 자원오행: ${evaluation.jawonOhaeng.ohaengDistribution}")
                    println("    ├─ 사주+이름오행: ${evaluation.sajuNameOhaeng.ohaengDistribution}")
                    println("    └─ 사주이름오행 통과: ${evaluation.detailedScores.sajuNameOhaengScore.isPassed}")
                }
            }
            println("─".repeat(80))

            // 통계 정보 표시
            println("\n【 분석 통계 】")
            println("- 전체 평가 이름: ${evaluations.size}개")
            println("- 필수 항목 통과: ${filteredEvaluations.size}개 (${String.format("%.1f", filteredEvaluations.size.toDouble() / evaluations.size * 100)}%)")
        }
    }
}

fun processNameQuery(query: String, timePoint: TimePointResult): List<Pair<NameInput, NameEvaluation>> {
    val hanjaDB = HanjaDatabase()
    val parser = NameQueryParser(hanjaDB)
    val analyzer = NameAnalyzer()
    val evaluations = mutableListOf<Pair<NameInput, NameEvaluation>>()

    val parsedQuery = parser.parse(query)

    // 모든 블록이 완전히 채워져 있는지 확인
    val isComplete = parsedQuery.surnameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty } &&
            parsedQuery.nameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty }

    if (isComplete) {
        // 직접 평가
        val nameInput = buildNameInput(parsedQuery, timePoint)
        val evaluation = analyzer.analyze(nameInput)
        evaluations.add(nameInput to evaluation)
    } else {
        // 가능한 모든 조합 검색 후 평가
        val searchEngine = NameSearchEngine(hanjaDB, NameStatisticsLoader())
        val results = searchEngine.search(parsedQuery)

        println("검색된 이름: ${results.size}개")

        // 모든 결과 평가
        results.forEach { result ->
            val nameInput = NameInput(
                surname = result.surname,
                surnameHanja = result.surnameHanja,
                givenName = result.givenName,
                givenNameHanja = result.givenNameHanja,
                timePointResult = timePoint
            )

            val evaluation = analyzer.analyze(nameInput)

            evaluations.add(nameInput to evaluation)
        }
    }

    return evaluations
}

fun buildNameInput(query: NameQuery, timePoint: TimePointResult): NameInput {
    val surname = query.surnameBlocks.joinToString("") { it.korean }
    val surnameHanja = query.surnameBlocks.joinToString("") { it.hanja }
    val givenName = query.nameBlocks.joinToString("") { it.korean }
    val givenNameHanja = query.nameBlocks.joinToString("") { it.hanja }

    return NameInput(surname, surnameHanja, givenName, givenNameHanja, timePoint)
}