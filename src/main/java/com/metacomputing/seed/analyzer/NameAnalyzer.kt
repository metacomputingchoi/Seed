// analyzer/NameAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.*
import com.metacomputing.seed.database.*
import com.metacomputing.mcalendar.TimePointResult

class NameAnalyzer {
    private val hanjaDB = HanjaDatabase()
    private val strokeDB = StrokeMeaningDatabase()

    fun analyze(nameInput: NameInput): NameEvaluation {
        val sajuInfo = extractSajuInfo(nameInput.timePointResult)
        val sageokSuri = analyzeSageokSuri(nameInput)

        val sageokSuriOhaeng = analyzeOhaengFromSageok(sageokSuri)
        val sageokSuriEumYang = analyzeEumYangFromSageok(sageokSuri)
        val sajuOhaeng = analyzeSajuOhaeng(sajuInfo)
        val sajuEumYang = analyzeSajuEumYang(sajuInfo)
        val hoeksuOhaeng = analyzeWithType(nameInput, AnalysisType.HOEKSU_OHAENG) as OhaengData
        val hoeksuEumYang = analyzeWithType(nameInput, AnalysisType.HOEKSU_EUMYANG) as EumYangData
        val baleumOhaeng = analyzeWithType(nameInput, AnalysisType.BALEUM_OHAENG) as OhaengData
        val baleumEumYang = analyzeWithType(nameInput, AnalysisType.BALEUM_EUMYANG) as EumYangData
        val jawonOhaeng = analyzeJawonOhaeng(nameInput)
        val sajuNameOhaeng = combineSajuNameOhaeng(sajuOhaeng, jawonOhaeng)

        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        val detailedScores = DetailedScores(
            SageokSuriScoreCalculator(sageokSuri).calculate(),
            OhaengScoreCalculator(sageokSuriOhaeng, surnameLength, "오행균형", "sageok").calculate(),
            EumYangScoreCalculator(sageokSuriEumYang, surnameLength, "사격수리음양").calculate(),
            calculateSajuEumYangScore(sajuEumYang),
            OhaengScoreCalculator(hoeksuOhaeng, surnameLength, "획수오행", "normal").calculate(),
            EumYangScoreCalculator(hoeksuEumYang, surnameLength, "획수음양").calculate(),
            OhaengScoreCalculator(baleumOhaeng, surnameLength, "발음오행", "normal").calculate(),
            EumYangScoreCalculator(baleumEumYang, surnameLength, "발음음양").calculate(),
            SajuNameOhaengScoreCalculator(sajuOhaeng, sajuNameOhaeng, jawonOhaeng).calculate(),
            calculateJawonOhaengScore(sajuOhaeng, jawonOhaeng)
        )

        return NameEvaluation(
            calculateWeightedTotalScore(detailedScores), detailedScores,
            sageokSuri, sageokSuriOhaeng, sageokSuriEumYang,
            sajuOhaeng, sajuEumYang, hoeksuOhaeng, hoeksuEumYang,
            baleumOhaeng, baleumEumYang, sajuNameOhaeng, jawonOhaeng
        )
    }

    private enum class AnalysisType {
        HOEKSU_OHAENG, HOEKSU_EUMYANG, BALEUM_OHAENG, BALEUM_EUMYANG
    }

    private fun analyzeWithType(nameInput: NameInput, type: AnalysisType): Any {
        return when (type) {
            AnalysisType.HOEKSU_OHAENG -> analyzeOhaengUnified(nameInput, "stroke")
            AnalysisType.HOEKSU_EUMYANG -> analyzeEumYangUnified(nameInput, "stroke")
            AnalysisType.BALEUM_OHAENG -> analyzeOhaengUnified(nameInput, "sound")
            AnalysisType.BALEUM_EUMYANG -> analyzeEumYangUnified(nameInput, "sound")
        }
    }

    private fun analyzeOhaengUnified(nameInput: NameInput, type: String): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        val arr = mutableListOf<String>()

        val processHanja = { korean: String, hanja: String, isSurname: Boolean ->
            val info = hanjaDB.getHanjaInfo(korean, hanja, isSurname)
            val ohaeng = when (type) {
                "stroke" -> info?.strokeElement
                "sound" -> info?.soundOhaeng
                else -> "土"
            }
            val key = (ohaeng ?: "土").toOhaengFull()
            dist[key] = dist[key]!! + 1
            arr.add(key.substring(0, 1))
        }

        hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja).forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) processHanja(parts[0], parts[1], true)
        }

        nameInput.givenName.forEachIndexed { i, char ->
            processHanja(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
        }

        return OhaengData(dist, arr)
    }

    private fun analyzeEumYangUnified(nameInput: NameInput, type: String): EumYangData {
        val arr = mutableListOf<String>()

        val processHanja = { korean: String, hanja: String, isSurname: Boolean ->
            val info = hanjaDB.getHanjaInfo(korean, hanja, isSurname)
            val eumyang = when (type) {
                "stroke" -> info?.strokeEumyang
                "sound" -> info?.soundEumyang
                else -> 0
            }
            arr.add((eumyang ?: 0).toEumYang())
        }

        hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja).forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) processHanja(parts[0], parts[1], true)
        }

        nameInput.givenName.forEachIndexed { i, char ->
            processHanja(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
        }

        return EumYangData(arr.count { it == "음" }, arr.count { it == "양" }, arr)
    }

    private fun extractSajuInfo(time: TimePointResult) = with(time.sexagenaryInfo) {
        SajuInfo(
            year.substring(0, 1), year.substring(1, 2),
            month.substring(0, 1), month.substring(1, 2),
            day.substring(0, 1), day.substring(1, 2),
            hour.substring(0, 1), hour.substring(1, 2)
        )
    }

    private fun analyzeSageokSuri(nameInput: NameInput): SageokSuri {
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        val surnameStrokes = surnamePairs.map { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) hanjaDB.getHanjaStrokes(parts[0], parts[1], true) else 0
        }

        val givenNameStrokes = nameInput.givenName.mapIndexed { i, char ->
            hanjaDB.getHanjaStrokes(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
        }

        val allStrokes = surnameStrokes + givenNameStrokes
        val nameStrokes = givenNameStrokes.toMutableList().apply { if (size == 1) add(0) }

        val myeongsangja = nameStrokes.subList(0, nameStrokes.size / 2).sum()
        val myeonghaja = nameStrokes.subList(nameStrokes.size / 2, nameStrokes.size).sum()

        val jeong = allStrokes.sum()
        val won = nameStrokes.sum()
        val i = surnameStrokes.sum() + myeonghaja
        val hyeong = surnameStrokes.sum() + myeongsangja

        return listOf(won, hyeong, i, jeong).map { adjustTo81(it) }.let { adjusted ->
            SageokSuri(
                adjusted[0], getMeaning(adjusted[0], "luckyLevel"), getMeaning(adjusted[0], "summary"),
                adjusted[1], getMeaning(adjusted[1], "luckyLevel"), getMeaning(adjusted[1], "summary"),
                adjusted[2], getMeaning(adjusted[2], "luckyLevel"), getMeaning(adjusted[2], "summary"),
                adjusted[3], getMeaning(adjusted[3], "luckyLevel"), getMeaning(adjusted[3], "summary")
            )
        }
    }

    private fun getMeaning(strokes: Int, field: String) =
        strokeDB.getStrokeMeaning(strokes)?.let {
            if (field == "luckyLevel") it.luckyLevel else it.summary
        } ?: ""

    private fun adjustTo81(value: Int) = if (value <= 81) value else ((value - 1) % 81) + 1

    private fun analyzeOhaengFromSageok(sageokSuri: SageokSuri): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        val arr = listOf(sageokSuri.iGyeok, sageokSuri.hyeongGyeok, sageokSuri.wonGyeok).map {
            it.toOhaengByLastDigit().also { ohaeng -> dist[ohaeng] = dist[ohaeng]!! + 1 }.substring(0, 1)
        }
        return OhaengData(dist, arr)
    }

    private fun analyzeEumYangFromSageok(sageokSuri: SageokSuri): EumYangData {
        val arr = listOf(sageokSuri.iGyeok, sageokSuri.hyeongGyeok, sageokSuri.wonGyeok).map { it.toEumYang() }
        return EumYangData(arr.count { it == "음" }, arr.count { it == "양" }, arr)
    }

    private fun analyzeSajuOhaeng(sajuInfo: SajuInfo): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem).forEach {
            Constants.STEM_OHAENG[it.normalize()]?.let { ohaeng -> dist[ohaeng] = dist[ohaeng]!! + 1 }
        }
        listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch).forEach {
            Constants.BRANCH_OHAENG[it.normalize()]?.let { ohaeng -> dist[ohaeng] = dist[ohaeng]!! + 1 }
        }
        return OhaengData(dist)
    }

    private fun analyzeSajuEumYang(sajuInfo: SajuInfo): EumYangData {
        var eum = 0
        var yang = 0
        listOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem).forEach {
            if (Constants.YANG_STEMS.contains(it.normalize())) yang++ else eum++
        }
        listOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch).forEach {
            if (Constants.YANG_BRANCHES.contains(it.normalize())) yang++ else eum++
        }
        return EumYangData(eum, yang)
    }

    private fun analyzeJawonOhaeng(nameInput: NameInput): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        nameInput.givenName.forEachIndexed { i, char ->
            val info = hanjaDB.getHanjaInfo(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
            val key = (info?.sourceElement ?: "土").toOhaengFull()
            dist[key] = dist[key]!! + 1
        }
        return OhaengData(dist)
    }

    private fun combineSajuNameOhaeng(sajuOhaeng: OhaengData, jawonOhaeng: OhaengData): OhaengData {
        val combined = sajuOhaeng.ohaengDistribution.toMutableMap()
        jawonOhaeng.ohaengDistribution.forEach { (k, v) -> combined[k] = combined[k]!! + v }
        return OhaengData(combined)
    }

    private fun calculateSajuEumYangScore(eumyang: EumYangData): ScoreDetail {
        val total = eumyang.eumCount + eumyang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "사주 데이터 없음", false)

        val ratio = minOf(eumyang.eumCount, eumyang.yangCount).toDouble() / total
        val score = when {
            ratio >= 0.4 -> 100
            ratio >= 0.375 -> 90
            ratio >= 0.25 -> 70
            ratio >= 0.125 -> 50
            else -> 30
        }

        return ScoreDetail(
            score, 100,
            "사주음양 - 음${eumyang.eumCount}:양${eumyang.yangCount} (${(ratio * 100).toInt()}:${((1-ratio) * 100).toInt()})",
            ratio >= 0.25
        )
    }

    private fun calculateJawonOhaengScore(sajuOhaeng: OhaengData, jawonOhaeng: OhaengData): ScoreDetail {
        val sajuAvg = sajuOhaeng.ohaengDistribution.values.average()
        var score = 70
        val reasons = mutableListOf<String>()

        val weakOhaengs = sajuOhaeng.ohaengDistribution.filter { it.value < sajuAvg }.keys
        val complemented = weakOhaengs.count { (jawonOhaeng.ohaengDistribution[it] ?: 0) > 0 }

        score += complemented * 10
        if (complemented > 0) reasons.add("${weakOhaengs.joinToString(", ")} 보완")

        val jawonBalance = if (jawonOhaeng.ohaengDistribution.values.sum() > 0) {
            val max = jawonOhaeng.ohaengDistribution.values.maxOrNull() ?: 0
            val min = jawonOhaeng.ohaengDistribution.values.minOrNull() ?: 0
            if (max - min <= 1) 10 else 0
        } else 0

        score = (score + jawonBalance).coerceIn(0, 100)
        val isPassed = weakOhaengs.isEmpty() || complemented >= (weakOhaengs.size + 1) / 2

        return ScoreDetail(
            score, 100,
            if (reasons.isNotEmpty()) reasons.joinToString(", ") else "자원오행 기본점수",
            isPassed
        )
    }

    private fun calculateWeightedTotalScore(scores: DetailedScores): Int {
        val weights = mapOf(
            "sageokSuri" to 0.20, "sajuNameOhaeng" to 0.20,
            "hoeksuEumYang" to 0.15, "baleumOhaeng" to 0.15,
            "baleumEumYang" to 0.15, "sageokSuriOhaeng" to 0.15
        )

        val weighted = scores.sageokSuriScore.score * weights["sageokSuri"]!! +
                      scores.sajuNameOhaengScore.score * weights["sajuNameOhaeng"]!! +
                      scores.hoeksuEumYangScore.score * weights["hoeksuEumYang"]!! +
                      scores.baleumOhaengScore.score * weights["baleumOhaeng"]!! +
                      scores.baleumEumYangScore.score * weights["baleumEumYang"]!! +
                      scores.sageokSuriOhaengScore.score * weights["sageokSuriOhaeng"]!!

        return weighted.toInt().coerceIn(0, 100)
    }
}