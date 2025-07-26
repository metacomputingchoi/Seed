// Main.kt
package com.metacomputing.seed

fun main() {
    val birthInfo = BirthInfo(
        year = 1986,
        month = 4,
        day = 19,
        hour = 5,
        minute = 45
    )

    val queries = listOf(
        "[최/崔][성/成][수/秀]",
        "[최/崔][_/_][_/_]"
    )

    queries.forEach { query ->
        println("\n=== 입력: $query ===")
        processAndDisplayResults(query, birthInfo)
    }
}

fun processAndDisplayResults(query: String, birthInfo: BirthInfo) {
    // 완전한 이름인지 검색 패턴인지 확인
    val isCompleteQuery = query.count { it == '[' } == query.count { it == ']' } &&
            !query.contains("_")

    if (isCompleteQuery) {
        // 완전한 이름 평가
        val parsed = parseCompleteQuery(query)
        if (parsed != null) {
            val result = Seed.evaluateName(
                surname = parsed.surname,
                surnameHanja = parsed.surnameHanja,
                givenName = parsed.givenName,
                givenNameHanja = parsed.givenNameHanja,
                year = birthInfo.year,
                month = birthInfo.month,
                day = birthInfo.day,
                hour = birthInfo.hour,
                minute = birthInfo.minute
            )

            println("평가 완료: 1개")

            if (result.isPassed) {
                println("필터링 후: 1개 (필수 항목 모두 통과)")
                displaySingleResult(result)
            } else {
                println("필터링 후: 0개 (필수 항목 모두 통과)")
                displayFailureDetails(result)
            }

            if (query.contains("[성/成][수/秀]")) {
                printDetailedDebugInfo(result)
            }
        }
    } else {
        val searchResults = Seed.searchNames(
            query = query,
            year = birthInfo.year,
            month = birthInfo.month,
            day = birthInfo.day,
            hour = birthInfo.hour,
            minute = birthInfo.minute,
            limit = 10000000
        )
        println("\n- 필터된 이름 수: ${searchResults.size}")
        displayTopRecommendations(searchResults.take(10))
    }
}

fun displaySingleResult(result: NameEvaluationResult) {
    println("\n【 평가 결과 】")
    println("─".repeat(80))
    println("${result.fullName} (${result.fullNameHanja}) - 종합점수: ${result.totalScore}점/100점")
    printNameDetails(result)
    println("─".repeat(80))
}

fun displayTopRecommendations(results: List<NameEvaluationResult>) {
    println("\n【 상위 ${results.size}개 추천 이름 】")
    println("─".repeat(80))

    results.forEachIndexed { index, result ->
        println("${index + 1}위. ${result.fullName} " +
                "(${result.fullNameHanja}) - " +
                "종합점수: ${result.totalScore}점/100점")

        if (index == 0) {
            printNameDetails(result)
        }
    }

    println("─".repeat(80))
}

fun displayFailureDetails(result: NameEvaluationResult) {
    println("\n【 평가 결과 - 불합격 】")
    println("─".repeat(80))
    println("${result.fullName} (${result.fullNameHanja}) - 종합점수: ${result.totalScore}점/100점")

    println("\n【 불합격 사유 】")
    result.scores.filter { !it.value.passed }.forEach { (name, score) ->
        println("- $name: ✗ - ${score.reason}")
    }

    println("\n【 필수 통과 기준 】")
    println("- 사격수리: 4개 격 모두 양운수 이상")
    println("- 사주이름오행: 사주의 부족한 오행 보완")
    println("- 획수음양: 성 첫글자와 이름 끝글자가 다른 음양")
    println("- 발음오행: 상극 없는 배열")
    println("- 발음음양: 성 첫글자와 이름 끝글자가 다른 음양")
    println("- 사격수리오행: 상극 없는 배열")
}

fun printNameDetails(result: NameEvaluationResult) {
    val details = result.details
    println("    ├─ 사주오행: ${details.sajuOhaeng.ohaengDistribution}")
    println("    ├─ 자원오행: ${details.jawonOhaeng.ohaengDistribution}")
    println("    ├─ 사주+이름오행: ${details.sajuNameOhaeng.ohaengDistribution}")
    println("    ├─ 획수오행: ${details.hoeksuOhaeng.arrangement.joinToString("-")}")
    println("    └─ 발음오행: ${details.baleumOhaeng.arrangement.joinToString("-")}")

    val relations = details.hoeksuOhaeng.arrangement.zipWithNext()
        .map { (first, second) -> getDetailedRelation(first, second) }
    if (relations.isNotEmpty()) {
        println("       └─ 관계: ${relations.joinToString(" → ")}")
    }
}

fun printDetailedDebugInfo(result: NameEvaluationResult) {
    println("\n【 디버깅: ${result.fullName} 】")

    result.scores.forEach { (name, score) ->
        println("$name: ${if (score.passed) "✓" else "✗"} - ${score.reason}")
    }
}

fun parseCompleteQuery(query: String): ParsedName? {
    val pattern = """\[([^/]+)/([^/]+)\]""".toRegex()
    val matches = pattern.findAll(query).toList()

    if (matches.size < 2) return null

    val surnameMatch = matches[0]
    val surname = surnameMatch.groupValues[1]
    val surnameHanja = surnameMatch.groupValues[2]

    val givenNameMatches = matches.drop(1)
    val givenName = givenNameMatches.joinToString("") { it.groupValues[1] }
    val givenNameHanja = givenNameMatches.joinToString("") { it.groupValues[2] }

    return ParsedName(surname, surnameHanja, givenName, givenNameHanja)
}

fun getDetailedRelation(first: String, second: String): String {
    val ohaengToNumber = mapOf(
        "목" to 0, "화" to 1, "토" to 2, "금" to 3, "수" to 4
    )

    fun isSangSaeng(a: String, b: String): Boolean {
        val aNum = ohaengToNumber[a] ?: return false
        val bNum = ohaengToNumber[b] ?: return false
        return (aNum + 1) % 5 == bNum
    }

    fun isNormalGeuk(a: String, b: String): Boolean {
        val aNum = ohaengToNumber[a] ?: return false
        val bNum = ohaengToNumber[b] ?: return false
        return (aNum + 2) % 5 == bNum
    }

    fun isReverseGeuk(a: String, b: String): Boolean {
        val aNum = ohaengToNumber[a] ?: return false
        val bNum = ohaengToNumber[b] ?: return false
        return (bNum + 2) % 5 == aNum
    }

    return when {
        first == second -> "동일"
        isSangSaeng(first, second) -> "상생"
        isNormalGeuk(first, second) -> "정상극(${first}극$second)"
        isReverseGeuk(first, second) -> "역상극(${second}극$first)"
        else -> "중립"
    }
}

// Data classes

data class BirthInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)

data class ParsedName(
    val surname: String,
    val surnameHanja: String,
    val givenName: String,
    val givenNameHanja: String
)