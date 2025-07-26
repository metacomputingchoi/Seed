// Main.kt
package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.model.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.search.NameQueryParser
import com.metacomputing.seed.search.NameSearchEngine
import com.metacomputing.seed.statistics.NameStatisticsLoader
import com.metacomputing.seed.util.OhaengRelationUtil

fun main() {

    val timePoint = CalSDK.getTimePointData(1986, 4, 19, 5, 45, -540, 0)

    val queries = listOf(
        "[최/崔][성/成][수/秀]",
        "[최/崔][_/_][_/_]"
    )

    queries.forEach { query ->
        println("\n=== 입력: $query ===")
        processAndDisplayResults(query, timePoint)
    }
}

fun processAndDisplayResults(query: String, timePoint: TimePointResult) {

    val evaluations = searchAndEvaluateNames(query, timePoint)
    println("평가 완료: ${evaluations.size}개")

    val passedEvaluations = filterPassedNames(evaluations)
    println("필터링 후: ${passedEvaluations.size}개 (필수 항목 모두 통과)")

    if (passedEvaluations.isEmpty()) {
        displayNoResultsMessage(evaluations)
    } else {
        displayTopRecommendations(passedEvaluations)
    }
}

fun searchAndEvaluateNames(query: String, timePoint: TimePointResult): List<Pair<NameInput, NameEvaluation>> {
    val hanjaDB = HanjaDatabase()
    val parser = NameQueryParser(hanjaDB)
    val analyzer = NameAnalyzer()

    val parsedQuery = parser.parse(query)
    val evaluations = mutableListOf<Pair<NameInput, NameEvaluation>>()

    if (isCompleteQuery(parsedQuery)) {
        val nameInput = buildNameInput(parsedQuery, timePoint)
        evaluations.add(nameInput to analyzer.analyze(nameInput))

        if (query.contains("[성/成][수/秀]")) {
            printDetailedDebugInfo(evaluations.first())
        }
    }

    else {
        val searchEngine = NameSearchEngine(hanjaDB, NameStatisticsLoader())
        val searchResults = searchEngine.search(parsedQuery)
        println("검색된 이름: ${searchResults.size}개")

        searchResults.forEach { result ->
            val nameInput = NameInput(
                surname = result.surname,
                surnameHanja = result.surnameHanja,
                givenName = result.givenName,
                givenNameHanja = result.givenNameHanja,
                timePointResult = timePoint
            )
            evaluations.add(nameInput to analyzer.analyze(nameInput))
        }
    }

    return evaluations
}

fun filterPassedNames(evaluations: List<Pair<NameInput, NameEvaluation>>): List<Pair<NameInput, NameEvaluation>> {
    return evaluations.filter { (_, evaluation) ->
        val scores = evaluation.detailedScores
        scores.sageokSuriScore.isPassed &&
                scores.sajuNameOhaengScore.isPassed &&
                scores.hoeksuEumYangScore.isPassed &&
                scores.baleumOhaengScore.isPassed &&
                scores.baleumEumYangScore.isPassed &&
                scores.sageokSuriOhaengScore.isPassed
    }
}

fun displayTopRecommendations(evaluations: List<Pair<NameInput, NameEvaluation>>) {
    val top10 = evaluations.sortedByDescending { it.second.totalScore }.take(10)

    println("\n【 상위 10개 추천 이름 】")
    println("─".repeat(80))

    top10.forEachIndexed { index, (nameInput, evaluation) ->
        println("${index + 1}위. ${nameInput.surname}${nameInput.givenName} " +
                "(${nameInput.surnameHanja}${nameInput.givenNameHanja}) - " +
                "종합점수: ${evaluation.totalScore}점/100점")

        if (index == 0) {
            printTopNameDetails(evaluation)
        }
    }

    println("─".repeat(80))
    println("\n【 분석 통계 】")
    println("- 전체 평가: ${evaluations.size}개")
    val passRate = evaluations.size.toDouble() / evaluations.size * 100
    println("- 통과율: ${String.format("%.1f", passRate)}%")
}

fun displayNoResultsMessage(evaluations: List<Pair<NameInput, NameEvaluation>>) {
    println("\n필수 항목을 모두 통과한 이름이 없습니다.")

    if (evaluations.size > 1) {
        val sample = evaluations.take(minOf(1000, evaluations.size))
        val failureStats = analyzeFailureReasons(sample)

        println("\n【 실패 원인 분석 (샘플 ${sample.size}개) 】")
        failureStats.forEach { (item, count) ->
            println("- $item 불통과: ${count}개 (${count * 100 / sample.size}%)")
        }
    }

    println("\n【 필수 통과 기준 】")
    println("- 사격수리: 4개 격 모두 양운수 이상")
    println("- 사주이름오행: 사주의 부족한 오행 보완")
    println("- 획수음양: 성 첫글자와 이름 끝글자가 다른 음양")
    println("- 발음오행: 상극 없는 배열")
    println("- 발음음양: 성 첫글자와 이름 끝글자가 다른 음양")
    println("- 사격수리오행: 상극 없는 배열")
}

fun printTopNameDetails(evaluation: NameEvaluation) {
    println("    ├─ 사주오행: ${evaluation.sajuOhaeng.ohaengDistribution}")
    println("    ├─ 자원오행: ${evaluation.jawonOhaeng.ohaengDistribution}")
    println("    ├─ 사주+이름오행: ${evaluation.sajuNameOhaeng.ohaengDistribution}")
    println("    ├─ 획수오행: ${evaluation.hoeksuOhaeng.arrangement.joinToString("-")}")
    println("    └─ 발음오행: ${evaluation.baleumOhaeng.arrangement.joinToString("-")}")

    val relations = evaluation.hoeksuOhaeng.arrangement.zipWithNext()
        .map { (first, second) -> OhaengRelationUtil.getDetailedRelation(first, second) }
    if (relations.isNotEmpty()) {
        println("       └─ 관계: ${relations.joinToString(" → ")}")
    }
}

fun printDetailedDebugInfo(evaluation: Pair<NameInput, NameEvaluation>) {
    val (nameInput, eval) = evaluation
    println("\n【 디버깅: ${nameInput.surname}${nameInput.givenName} 】")

    val scores = eval.detailedScores
    listOf(
        "사격수리" to scores.sageokSuriScore,
        "사주이름오행" to scores.sajuNameOhaengScore,
        "획수음양" to scores.hoeksuEumYangScore,
        "발음오행" to scores.baleumOhaengScore,
        "발음음양" to scores.baleumEumYangScore,
        "사격수리오행" to scores.sageokSuriOhaengScore
    ).forEach { (name, score) ->
        println("$name: ${if (score.isPassed) "✓" else "✗"} - ${score.reason}")
    }
}

fun analyzeFailureReasons(sample: List<Pair<NameInput, NameEvaluation>>): Map<String, Int> {
    return mapOf(
        "사격수리" to sample.count { !it.second.detailedScores.sageokSuriScore.isPassed },
        "사주이름오행" to sample.count { !it.second.detailedScores.sajuNameOhaengScore.isPassed },
        "획수음양" to sample.count { !it.second.detailedScores.hoeksuEumYangScore.isPassed },
        "발음오행" to sample.count { !it.second.detailedScores.baleumOhaengScore.isPassed },
        "발음음양" to sample.count { !it.second.detailedScores.baleumEumYangScore.isPassed },
        "사격수리오행" to sample.count { !it.second.detailedScores.sageokSuriOhaengScore.isPassed }
    )
}

fun isCompleteQuery(query: NameQuery): Boolean {
    return query.surnameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty } &&
            query.nameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty }
}

fun buildNameInput(query: NameQuery, timePoint: TimePointResult): NameInput {
    return NameInput(
        surname = query.surnameBlocks.joinToString("") { it.korean },
        surnameHanja = query.surnameBlocks.joinToString("") { it.hanja },
        givenName = query.nameBlocks.joinToString("") { it.korean },
        givenNameHanja = query.nameBlocks.joinToString("") { it.hanja },
        timePointResult = timePoint
    )
}