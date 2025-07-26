// analyzer/NameAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.*
import com.metacomputing.seed.database.HanjaDatabase
import com.metacomputing.seed.database.StrokeMeaningDatabase
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.util.OhaengRelationUtil
import com.metacomputing.seed.util.PassFailUtil

class NameAnalyzer {
    private val hanjaDB = HanjaDatabase()
    private val strokeDB = StrokeMeaningDatabase()

    fun analyze(nameInput: NameInput): NameEvaluation {
        val sajuInfo = extractSajuInfo(nameInput.timePointResult)
        val sageokSuri = analyzeSageokSuri(nameInput)

        val analyzers = mapOf(
            "sageokSuriOhaeng" to { analyzeSageokSuriOhaeng(sageokSuri) },
            "sageokSuriEumYang" to { analyzeSageokSuriEumYang(sageokSuri) },
            "sajuOhaeng" to { analyzeSajuOhaeng(sajuInfo) },
            "sajuEumYang" to { analyzeSajuEumYang(sajuInfo) },
            "hoeksuOhaeng" to { analyzeOhaeng(nameInput, "stroke") },
            "hoeksuEumYang" to { analyzeEumYang(nameInput, "stroke") },
            "baleumOhaeng" to { analyzeOhaeng(nameInput, "sound") },
            "baleumEumYang" to { analyzeEumYang(nameInput, "sound") },
            "jawonOhaeng" to { analyzeJawonOhaeng(nameInput) }
        )

        val results = analyzers.mapValues { it.value() }
        val sajuNameOhaeng = analyzeSajuNameOhaeng(
            results["sajuOhaeng"] as OhaengData,
            results["jawonOhaeng"] as OhaengData
        )

        val detailedScores = calculateDetailedScores(
            sageokSuri, results, sajuNameOhaeng, nameInput
        )

        val totalScore = calculateWeightedTotalScore(detailedScores)

        return NameEvaluation(
            totalScore, detailedScores, sageokSuri,
            results["sageokSuriOhaeng"] as OhaengData,
            results["sageokSuriEumYang"] as EumYangData,
            results["sajuOhaeng"] as OhaengData,
            results["sajuEumYang"] as EumYangData,
            results["hoeksuOhaeng"] as OhaengData,
            results["hoeksuEumYang"] as EumYangData,
            results["baleumOhaeng"] as OhaengData,
            results["baleumEumYang"] as EumYangData,
            sajuNameOhaeng,
            results["jawonOhaeng"] as OhaengData
        )
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

    private fun analyzeSageokSuriOhaeng(sageokSuri: SageokSuri): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        val arr = listOf(sageokSuri.iGyeok, sageokSuri.hyeongGyeok, sageokSuri.wonGyeok).map { 
            it.toOhaengByLastDigit().also { ohaeng -> dist[ohaeng] = dist[ohaeng]!! + 1 }.substring(0, 1)
        }
        return OhaengData(dist, arr)
    }

    private fun analyzeSageokSuriEumYang(sageokSuri: SageokSuri): EumYangData {
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

    private fun analyzeOhaeng(nameInput: NameInput, type: String): OhaengData {
        val dist = mutableMapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
        val arr = mutableListOf<String>()

        hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja).forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val info = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val ohaeng = if (type == "stroke") info?.strokeElement else info?.soundOhaeng
                val key = (ohaeng ?: "土").toOhaengFull()
                dist[key] = dist[key]!! + 1
                arr.add(key.substring(0, 1))
            }
        }

        nameInput.givenName.forEachIndexed { i, char ->
            val info = hanjaDB.getHanjaInfo(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
            val ohaeng = if (type == "stroke") info?.strokeElement else info?.soundOhaeng
            val key = (ohaeng ?: "土").toOhaengFull()
            dist[key] = dist[key]!! + 1
            arr.add(key.substring(0, 1))
        }

        return OhaengData(dist, arr)
    }

    private fun analyzeEumYang(nameInput: NameInput, type: String): EumYangData {
        val arr = mutableListOf<String>()

        hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja).forEach { pair ->
            val parts = pair.split("/")
            if (parts.size == 2) {
                val info = hanjaDB.getHanjaInfo(parts[0], parts[1], true)
                val eumyang = if (type == "stroke") info?.strokeEumyang else info?.soundEumyang
                arr.add((eumyang ?: 0).toEumYang())
            }
        }

        nameInput.givenName.forEachIndexed { i, char ->
            val info = hanjaDB.getHanjaInfo(char.toString(), nameInput.givenNameHanja.getOrNull(i)?.toString() ?: "", false)
            val eumyang = if (type == "stroke") info?.strokeEumyang else info?.soundEumyang
            arr.add((eumyang ?: 0).toEumYang())
        }

        return EumYangData(arr.count { it == "음" }, arr.count { it == "양" }, arr)
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

    private fun analyzeSajuNameOhaeng(sajuOhaeng: OhaengData, jawonOhaeng: OhaengData): OhaengData {
        val combined = sajuOhaeng.ohaengDistribution.toMutableMap()
        jawonOhaeng.ohaengDistribution.forEach { (k, v) -> combined[k] = combined[k]!! + v }
        return OhaengData(combined)
    }

    private fun calculateDetailedScores(
        sageokSuri: SageokSuri, results: Map<String, Any>,
        sajuNameOhaeng: OhaengData, nameInput: NameInput
    ): DetailedScores {
        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        return DetailedScores(
            calculateSageokSuriScore(sageokSuri),
            calculateSageokSuriOhaengScore(results["sageokSuriOhaeng"] as OhaengData),
            calculateSageokSuriEumYangScore(results["sageokSuriEumYang"] as EumYangData),
            calculateSajuEumYangScore(results["sajuEumYang"] as EumYangData),
            calculateHoeksuOhaengScore(results["hoeksuOhaeng"] as OhaengData, surnameLength),
            calculateEumYangScore(results["hoeksuEumYang"] as EumYangData, surnameLength, "획수음양"),
            calculateBaleumOhaengScore(results["baleumOhaeng"] as OhaengData, surnameLength),
            calculateEumYangScore(results["baleumEumYang"] as EumYangData, surnameLength, "발음음양"),
            calculateSajuNameOhaengScore(results["sajuOhaeng"] as OhaengData, sajuNameOhaeng, results["jawonOhaeng"] as OhaengData),
            calculateJawonOhaengScore(results["sajuOhaeng"] as OhaengData, results["jawonOhaeng"] as OhaengData)
        )
    }

    private fun calculateSageokSuriScore(sageokSuri: SageokSuri): ScoreDetail {
        val fortunes = listOf(
            sageokSuri.wonGyeokFortune, sageokSuri.hyeongGyeokFortune,
            sageokSuri.iGyeokFortune, sageokSuri.jeongGyeokFortune
        )

        val scores = fortunes.map { fortune ->
            when {
                fortune.contains("최상운수") -> 25
                fortune.contains("상운수") -> 20
                fortune.contains("양운수") -> 15
                fortune.contains("흉운수") && !fortune.contains("최흉운수") -> 5
                fortune.contains("최흉운수") -> 0
                else -> 10
            }
        }

        val totalScore = scores.sum()
        val passCount = scores.count { it >= 15 }

        return ScoreDetail(
            totalScore, 100,
            "원격: ${sageokSuri.wonGyeokFortune}, 형격: ${sageokSuri.hyeongGyeokFortune}, " +
            "이격: ${sageokSuri.iGyeokFortune}, 정격: ${sageokSuri.jeongGyeokFortune}",
            passCount == 4
        )
    }

    private fun calculateSageokSuriOhaengScore(ohaeng: OhaengData): ScoreDetail {
        val balance = OhaengRelationUtil.calculateBalanceScore(ohaeng.ohaengDistribution) * 0.5
        val array = OhaengRelationUtil.calculateArrayScore(ohaeng.arrangement) * 0.5
        val isPassed = PassFailUtil.checkSageokSuriOhaeng(ohaeng.arrangement)

        return ScoreDetail(
            (balance + array).toInt(), 100,
            "오행균형(${balance.toInt()}/50), 배열조화(${array.toInt()}/50)",
            isPassed
        )
    }

    private fun calculateSageokSuriEumYangScore(eumyang: EumYangData): ScoreDetail {
        val total = eumyang.eumCount + eumyang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "데이터 없음", false)

        val ratio = minOf(eumyang.eumCount, eumyang.yangCount).toDouble() / total
        val score = when {
            ratio >= 0.4 -> 100
            ratio >= 0.3 -> 85
            ratio >= 0.2 -> 70
            else -> 60
        }

        return ScoreDetail(score, 100, "음${eumyang.eumCount}:양${eumyang.yangCount}", ratio >= 0.2)
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
            "사주음양 - 음${eumyang.eumCount}:양${eumyang.yangCount} " +
            "(${(ratio * 100).toInt()}:${((1-ratio) * 100).toInt()})",
            ratio >= 0.25
        )
    }

    private fun calculateHoeksuOhaengScore(ohaeng: OhaengData, surnameLength: Int): ScoreDetail {
        val balance = OhaengRelationUtil.calculateBalanceScore(ohaeng.ohaengDistribution) * 0.5
        val array = OhaengRelationUtil.calculateArrayScore(ohaeng.arrangement) * 0.5
        val isPassed = PassFailUtil.checkOhaengSangSaeng(ohaeng.arrangement, surnameLength)

        return ScoreDetail(
            (balance + array).toInt(), 100,
            "획수오행 균형도: ${balance.toInt()}/50, 배열: ${ohaeng.arrangement.joinToString("-")}",
            isPassed
        )
    }

    private fun calculateBaleumOhaengScore(ohaeng: OhaengData, surnameLength: Int): ScoreDetail {
        val balance = OhaengRelationUtil.calculateBalanceScore(ohaeng.ohaengDistribution) * 0.5
        val array = OhaengRelationUtil.calculateArrayScore(ohaeng.arrangement) * 0.5
        val isPassed = PassFailUtil.checkOhaengSangSaeng(ohaeng.arrangement, surnameLength)

        return ScoreDetail(
            (balance + array).toInt(), 100,
            "발음오행 균형도: ${balance.toInt()}/50, 배열: ${ohaeng.arrangement.joinToString("-")}",
            isPassed
        )
    }

    private fun calculateEumYangScore(eumyang: EumYangData, surnameLength: Int, prefix: String): ScoreDetail {
        val total = eumyang.eumCount + eumyang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "$prefix 데이터 없음", false)

        val ratio = minOf(eumyang.eumCount, eumyang.yangCount).toDouble() / total
        val ratioScore = when {
            ratio >= 0.4 -> 50
            ratio >= 0.3 -> 35
            ratio >= 0.2 -> 20
            else -> 10
        }

        val isPassed = PassFailUtil.checkEumYangHarmony(eumyang.arrangement, surnameLength)
        val totalScore = (ratioScore + 40).coerceIn(0, 100)

        return ScoreDetail(
            totalScore, 100,
            "$prefix - 음${eumyang.eumCount}:양${eumyang.yangCount}, 배열: ${eumyang.arrangement.joinToString("")}",
            isPassed
        )
    }

    private fun calculateSajuNameOhaengScore(sajuOhaeng: OhaengData, sajuNameOhaeng: OhaengData, jawonOhaeng: OhaengData): ScoreDetail {
        val sajuZero = sajuOhaeng.ohaengDistribution.filter { it.value == 0 }.keys
        val finalZero = sajuNameOhaeng.ohaengDistribution.filter { it.value == 0 }.keys
        val zeroReduction = sajuZero.size - finalZero.size
        val jawonForZero = sajuZero.count { (jawonOhaeng.ohaengDistribution[it] ?: 0) > 0 }

        val isPassed = sajuZero.isEmpty() || jawonForZero == 0 || zeroReduction == jawonForZero
        val balance = OhaengRelationUtil.calculateBalanceScore(sajuNameOhaeng.ohaengDistribution)
        val complementScore = if (sajuZero.isNotEmpty() && jawonForZero > 0) {
            (zeroReduction.toDouble() / jawonForZero * 50).toInt()
        } else 30

        val score = (complementScore + balance * 0.5).toInt().coerceIn(0, 100)
        val reason = if (sajuZero.isNotEmpty()) {
            "0인 오행(${sajuZero.joinToString(",")}) 중 ${zeroReduction}개 보완, 균형도: ${balance}점"
        } else "0인 오행 없음, 균형도: ${balance}점"

        return ScoreDetail(score, 100, reason, isPassed)
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