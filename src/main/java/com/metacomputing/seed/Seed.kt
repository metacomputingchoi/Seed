// Seed.kt
package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.database.HanjaDetailedInfo
import com.metacomputing.seed.model.*
import com.metacomputing.seed.search.NameQueryParser
import com.metacomputing.seed.search.NameSearchEngine
import com.metacomputing.seed.statistics.NameStatisticsLoader
import com.metacomputing.seed.statistics.NameStatisticsAnalyzer

/**
 * 작명 분석 엔진 SDK
 */
object Seed {

    private val hanjaDB = HanjaDatabase()
    private val analyzer = NameAnalyzer()
    private val statsLoader = NameStatisticsLoader()
    private val statsAnalyzer = NameStatisticsAnalyzer()

    /**
     * 특정 이름을 평가합니다.
     */
    fun evaluateName(
        surname: String,
        surnameHanja: String,
        givenName: String,
        givenNameHanja: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timezone: Int = -540
    ): NameEvaluationResult {
        val timePoint = CalSDK.getTimePointData(year, month, day, hour, minute, timezone, 0)
        val nameInput = NameInput(surname, surnameHanja, givenName, givenNameHanja, timePoint)
        val evaluation = analyzer.analyze(nameInput)

        return NameEvaluationResult(
            fullName = "$surname$givenName",
            fullNameHanja = "$surnameHanja$givenNameHanja",
            totalScore = evaluation.totalScore,
            isPassed = checkIfPassed(evaluation),
            scores = createScoreSummary(evaluation),
            details = evaluation
        )
    }

    /**
     * 이름 패턴으로 검색하고 평가합니다.
     */
    fun searchNames(
        query: String,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timezone: Int = -540,
        limit: Int = 10000000
    ): List<NameEvaluationResult> {
        val timePoint = CalSDK.getTimePointData(year, month, day, hour, minute, timezone, 0)
        val parser = NameQueryParser(hanjaDB)
        val parsedQuery = parser.parse(query)

        // 완전한 이름이 입력된 경우
        if (isCompleteQuery(parsedQuery)) {
            val nameInput = buildNameInput(parsedQuery, timePoint)
            val evaluation = analyzer.analyze(nameInput)
            return listOf(createEvaluationResult(nameInput, evaluation))
        }

        // 패턴 검색인 경우
        val searchEngine = NameSearchEngine(hanjaDB, statsLoader)
        val searchResults = searchEngine.search(parsedQuery)

        return searchResults
            .map { result ->
                val nameInput = NameInput(
                    result.surname,
                    result.surnameHanja,
                    result.givenName,
                    result.givenNameHanja,
                    timePoint
                )
                nameInput to analyzer.analyze(nameInput)
            }
            .filter { (_, evaluation) -> checkIfPassed(evaluation) }
            .sortedByDescending { it.second.totalScore }
            .take(limit)
            .map { (nameInput, evaluation) ->
                createEvaluationResult(nameInput, evaluation)
            }
    }

    /**
     * 이름에 대한 통계 정보를 가져옵니다.
     */
    fun getNameStatistics(givenName: String): NameStatisticsResult? {
        val stats = statsLoader.loadStatistics()[givenName] ?: return null

        return NameStatisticsResult(
            name = givenName,
            popularity = statsAnalyzer.analyzePopularity(stats),
            genderDistribution = statsAnalyzer.analyzeGenderDistribution(stats),
            birthTrend = statsAnalyzer.analyzeBirthTrend(stats),
            hanjaCombinations = stats.hanjaCombinations
        )
    }

    /**
     * 한자 정보를 조회합니다.
     */
    fun getHanjaInfo(korean: String, hanja: String): HanjaDetailedInfo? {
        return hanjaDB.getHanjaInfo(korean, hanja, false)
    }

    // Private helper functions

    private fun checkIfPassed(evaluation: NameEvaluation): Boolean {
        val scores = evaluation.detailedScores
        return scores.sageokSuriScore.isPassed &&
                scores.sajuNameOhaengScore.isPassed &&
                scores.hoeksuEumYangScore.isPassed &&
                scores.baleumOhaengScore.isPassed &&
                scores.baleumEumYangScore.isPassed &&
                scores.sageokSuriOhaengScore.isPassed
    }

    private fun createScoreSummary(evaluation: NameEvaluation): Map<String, ScoreSummary> {
        val scores = evaluation.detailedScores
        return mapOf(
            "사격수리" to ScoreSummary(
                scores.sageokSuriScore.score,
                scores.sageokSuriScore.isPassed,
                scores.sageokSuriScore.reason
            ),
            "사주이름오행" to ScoreSummary(
                scores.sajuNameOhaengScore.score,
                scores.sajuNameOhaengScore.isPassed,
                scores.sajuNameOhaengScore.reason
            ),
            "획수음양" to ScoreSummary(
                scores.hoeksuEumYangScore.score,
                scores.hoeksuEumYangScore.isPassed,
                scores.hoeksuEumYangScore.reason
            ),
            "발음오행" to ScoreSummary(
                scores.baleumOhaengScore.score,
                scores.baleumOhaengScore.isPassed,
                scores.baleumOhaengScore.reason
            ),
            "발음음양" to ScoreSummary(
                scores.baleumEumYangScore.score,
                scores.baleumEumYangScore.isPassed,
                scores.baleumEumYangScore.reason
            ),
            "사격수리오행" to ScoreSummary(
                scores.sageokSuriOhaengScore.score,
                scores.sageokSuriOhaengScore.isPassed,
                scores.sageokSuriOhaengScore.reason
            )
        )
    }

    private fun createEvaluationResult(
        nameInput: NameInput,
        evaluation: NameEvaluation
    ): NameEvaluationResult {
        return NameEvaluationResult(
            fullName = "${nameInput.surname}${nameInput.givenName}",
            fullNameHanja = "${nameInput.surnameHanja}${nameInput.givenNameHanja}",
            totalScore = evaluation.totalScore,
            isPassed = checkIfPassed(evaluation),
            scores = createScoreSummary(evaluation),
            details = evaluation
        )
    }

    private fun isCompleteQuery(query: NameQuery): Boolean {
        return query.surnameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty } &&
                query.nameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty }
    }

    private fun buildNameInput(query: NameQuery, timePoint: TimePointResult): NameInput {
        return NameInput(
            surname = query.surnameBlocks.joinToString("") { it.korean },
            surnameHanja = query.surnameBlocks.joinToString("") { it.hanja },
            givenName = query.nameBlocks.joinToString("") { it.korean },
            givenNameHanja = query.nameBlocks.joinToString("") { it.hanja },
            timePointResult = timePoint
        )
    }
}

// Result data classes for SDK

data class NameEvaluationResult(
    val fullName: String,
    val fullNameHanja: String,
    val totalScore: Int,
    val isPassed: Boolean,
    val scores: Map<String, ScoreSummary>,
    val details: NameEvaluation
)

data class ScoreSummary(
    val score: Int,
    val passed: Boolean,
    val reason: String
)

data class NameStatisticsResult(
    val name: String,
    val popularity: PopularityAnalysis,
    val genderDistribution: GenderDistribution,
    val birthTrend: BirthTrend,
    val hanjaCombinations: List<String>
)