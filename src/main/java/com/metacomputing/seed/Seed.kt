// Seed.kt
package com.metacomputing.seed

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.analyzer.NameAnalyzer
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.model.*
import com.metacomputing.seed.search.NameQueryParser
import com.metacomputing.seed.search.NameSearchEngine
import com.metacomputing.seed.statistics.NameStatisticsManager

object Seed {
    private val hanjaDB = HanjaDatabase()
    private val analyzer = NameAnalyzer()
    private val statsManager = NameStatisticsManager()

    fun evaluateName(
        surname: String, surnameHanja: String, givenName: String, givenNameHanja: String,
        year: Int, month: Int, day: Int, hour: Int, minute: Int, timezone: Int = -540
    ): NameEvaluationResult {
        val timePoint = CalSDK.getTimePointData(year, month, day, hour, minute, timezone, 0)
        val nameInput = NameInput(surname, surnameHanja, givenName, givenNameHanja, timePoint)
        val evaluation = analyzer.analyze(nameInput)

        return createEvaluationResult(nameInput, evaluation)
    }

    fun searchNames(
        query: String, year: Int, month: Int, day: Int, hour: Int, minute: Int,
        timezone: Int = -540, limit: Int = 10000000
    ): List<NameEvaluationResult> {
        val timePoint = CalSDK.getTimePointData(year, month, day, hour, minute, timezone, 0)
        val parser = NameQueryParser(hanjaDB)
        val parsedQuery = parser.parse(query)

        if (isCompleteQuery(parsedQuery)) {
            val nameInput = buildNameInput(parsedQuery, timePoint)
            val evaluation = analyzer.analyze(nameInput)
            return listOf(createEvaluationResult(nameInput, evaluation))
        }

        val searchEngine = NameSearchEngine(hanjaDB, statsManager)
        return searchEngine.search(parsedQuery)
            .map { result ->
                val nameInput = NameInput(
                    result.surname, result.surnameHanja,
                    result.givenName, result.givenNameHanja, timePoint
                )
                nameInput to analyzer.analyze(nameInput)
            }
            .filter { (_, evaluation) -> checkIfPassed(evaluation) }
            .sortedByDescending { it.second.totalScore }
            .take(limit)
            .map { (nameInput, evaluation) -> createEvaluationResult(nameInput, evaluation) }
    }

    fun getNameStatistics(givenName: String) = statsManager.getStatistics(givenName)
    fun getHanjaInfo(korean: String, hanja: String) = hanjaDB.getHanjaInfo(korean, hanja, false)

    private fun checkIfPassed(eval: NameEvaluation) = with(eval.detailedScores) {
        sageokSuriScore.isPassed && sajuNameOhaengScore.isPassed &&
        hoeksuEumYangScore.isPassed && baleumOhaengScore.isPassed &&
        baleumEumYangScore.isPassed && sageokSuriOhaengScore.isPassed
    }

    private fun createScoreSummary(eval: NameEvaluation) = with(eval.detailedScores) {
        mapOf(
            "사격수리" to ScoreSummary(sageokSuriScore.score, sageokSuriScore.isPassed, sageokSuriScore.reason),
            "사주이름오행" to ScoreSummary(sajuNameOhaengScore.score, sajuNameOhaengScore.isPassed, sajuNameOhaengScore.reason),
            "획수음양" to ScoreSummary(hoeksuEumYangScore.score, hoeksuEumYangScore.isPassed, hoeksuEumYangScore.reason),
            "발음오행" to ScoreSummary(baleumOhaengScore.score, baleumOhaengScore.isPassed, baleumOhaengScore.reason),
            "발음음양" to ScoreSummary(baleumEumYangScore.score, baleumEumYangScore.isPassed, baleumEumYangScore.reason),
            "사격수리오행" to ScoreSummary(sageokSuriOhaengScore.score, sageokSuriOhaengScore.isPassed, sageokSuriOhaengScore.reason)
        )
    }

    private fun createEvaluationResult(nameInput: NameInput, evaluation: NameEvaluation) = NameEvaluationResult(
        "${nameInput.surname}${nameInput.givenName}",
        "${nameInput.surnameHanja}${nameInput.givenNameHanja}",
        evaluation.totalScore,
        checkIfPassed(evaluation),
        createScoreSummary(evaluation),
        evaluation
    )

    private fun isCompleteQuery(query: NameQuery) =
        query.surnameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty } &&
        query.nameBlocks.all { !it.isKoreanEmpty && !it.isHanjaEmpty }

    private fun buildNameInput(query: NameQuery, timePoint: TimePointResult) = NameInput(
        query.surnameBlocks.joinToString("") { it.korean },
        query.surnameBlocks.joinToString("") { it.hanja },
        query.nameBlocks.joinToString("") { it.korean },
        query.nameBlocks.joinToString("") { it.hanja },
        timePoint
    )
}

data class NameEvaluationResult(
    val fullName: String, val fullNameHanja: String,
    val totalScore: Int, val isPassed: Boolean,
    val scores: Map<String, ScoreSummary>, val details: NameEvaluation
)

data class ScoreSummary(val score: Int, val passed: Boolean, val reason: String)

data class NameStatisticsResult(
    val name: String, val popularity: PopularityAnalysis,
    val genderDistribution: GenderDistribution, val birthTrend: BirthTrend,
    val hanjaCombinations: List<String>
)