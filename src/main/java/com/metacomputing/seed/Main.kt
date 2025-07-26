// Main.kt
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
        // 필요시 여기서 evaluations 리스트 활용
        evaluations.take(5).forEach { (nameInput, evaluation) ->
            println("${nameInput.surname}${nameInput.givenName} (${nameInput.surnameHanja}${nameInput.givenNameHanja}) - 점수: ${evaluation.totalScore}/100")
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