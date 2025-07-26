// analyzer/NameAnalyzer.kt
package com.metacomputing.seed.analyzer

import com.metacomputing.seed.model.*
import com.metacomputing.seed.*
import com.metacomputing.seed.database.*
import com.metacomputing.mcalendar.TimePointResult

class NameAnalyzer {
    private val hanjaDB = HanjaDatabase()
    private val strokeDB = StrokeMeaningDatabase()

    // 상수 정의
    private val EMPTY_OHAENG_DIST = mapOf("목(木)" to 0, "화(火)" to 0, "토(土)" to 0, "금(金)" to 0, "수(水)" to 0)
    private val DEFAULT_OHAENG = "土"
    private val SLASH = "/"
    private val WEIGHT_SAGEOK_SURI = 0.20
    private val WEIGHT_SAJU_NAME_OHAENG = 0.20
    private val WEIGHT_HOEKSU_EUMYANG = 0.15
    private val WEIGHT_BALEUM_OHAENG = 0.15
    private val WEIGHT_BALEUM_EUMYANG = 0.15
    private val WEIGHT_SAGEOK_SURI_OHAENG = 0.15

    // 한자 정보 캐시 구조체
    private data class HanjaInfoCache(
        val surnameInfos: List<HanjaDetailedInfo?>,
        val givenNameInfos: List<HanjaDetailedInfo?>
    )

    fun analyze(nameInput: NameInput): NameEvaluation {
        // 1. 한자 정보를 미리 모두 로드하여 캐싱
        val hanjaCache = preloadHanjaInfo(nameInput)

        // 2. 사주 정보 추출
        val sajuInfo = extractSajuInfo(nameInput.timePointResult)

        // 3. 사격수리 분석 (캐시 사용)
        val sageokSuri = analyzeSageokSuri(nameInput, hanjaCache)

        // 4. 각종 분석 수행 (캐시 사용)
        val sageokSuriOhaeng = analyzeOhaengFromSageok(sageokSuri)
        val sageokSuriEumYang = analyzeEumYangFromSageok(sageokSuri)
        val sajuOhaeng = analyzeSajuOhaeng(sajuInfo)
        val sajuEumYang = analyzeSajuEumYang(sajuInfo)
        val hoeksuOhaeng = analyzeOhaengWithCache(hanjaCache, "stroke")
        val hoeksuEumYang = analyzeEumYangWithCache(hanjaCache, "stroke")
        val baleumOhaeng = analyzeOhaengWithCache(hanjaCache, "sound")
        val baleumEumYang = analyzeEumYangWithCache(hanjaCache, "sound")
        val jawonOhaeng = analyzeJawonOhaengWithCache(hanjaCache)
        val sajuNameOhaeng = combineSajuNameOhaeng(sajuOhaeng, jawonOhaeng)

        val surnameLength = if (nameInput.surname.length == 2) 2 else 1

        // 5. 점수 계산
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

    // 한자 정보 미리 로드
    private fun preloadHanjaInfo(nameInput: NameInput): HanjaInfoCache {
        val surnameInfos = mutableListOf<HanjaDetailedInfo?>()
        val givenNameInfos = mutableListOf<HanjaDetailedInfo?>()

        // 성씨 정보 로드
        val surnamePairs = hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja)
        surnamePairs.forEach { pair ->
            val index = pair.indexOf(SLASH)
            if (index > 0) {
                val korean = pair.substring(0, index)
                val hanja = pair.substring(index + 1)
                surnameInfos.add(hanjaDB.getHanjaInfo(korean, hanja, true))
            } else {
                surnameInfos.add(null)
            }
        }

        // 이름 정보 로드
        nameInput.givenName.forEachIndexed { i, char ->
            val hanja = if (i < nameInput.givenNameHanja.length)
                nameInput.givenNameHanja[i].toString()
            else ""
            givenNameInfos.add(hanjaDB.getHanjaInfo(char.toString(), hanja, false))
        }

        return HanjaInfoCache(surnameInfos, givenNameInfos)
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

    // 캐시를 사용한 오행 분석
    private fun analyzeOhaengWithCache(cache: HanjaInfoCache, type: String): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()
        val arr = ArrayList<String>(cache.surnameInfos.size + cache.givenNameInfos.size)

        // 성씨 처리
        cache.surnameInfos.forEach { info ->
            if (info != null) {
                val ohaeng = when (type) {
                    "stroke" -> info.strokeElement
                    "sound" -> info.soundOhaeng
                    else -> DEFAULT_OHAENG
                }
                val key = ohaeng.toOhaengFull()
                dist[key] = dist[key]!! + 1
                arr.add(key[0].toString())
            }
        }

        // 이름 처리
        cache.givenNameInfos.forEach { info ->
            if (info != null) {
                val ohaeng = when (type) {
                    "stroke" -> info.strokeElement
                    "sound" -> info.soundOhaeng
                    else -> DEFAULT_OHAENG
                }
                val key = ohaeng.toOhaengFull()
                dist[key] = dist[key]!! + 1
                arr.add(key[0].toString())
            }
        }

        return OhaengData(dist, arr)
    }

    // 캐시를 사용한 음양 분석
    private fun analyzeEumYangWithCache(cache: HanjaInfoCache, type: String): EumYangData {
        val arr = ArrayList<String>(cache.surnameInfos.size + cache.givenNameInfos.size)

        // 성씨 처리
        cache.surnameInfos.forEach { info ->
            if (info != null) {
                val eumyang = when (type) {
                    "stroke" -> info.strokeEumyang
                    "sound" -> info.soundEumyang
                    else -> 0
                }
                arr.add(eumyang.toEumYang())
            }
        }

        // 이름 처리
        cache.givenNameInfos.forEach { info ->
            if (info != null) {
                val eumyang = when (type) {
                    "stroke" -> info.strokeEumyang
                    "sound" -> info.soundEumyang
                    else -> 0
                }
                arr.add(eumyang.toEumYang())
            }
        }

        val eumCount = arr.count { it == "음" }
        return EumYangData(eumCount, arr.size - eumCount, arr)
    }

    // 기존 메서드들 (호환성 유지)
    private fun analyzeOhaengUnified(nameInput: NameInput, type: String): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()
        val arr = mutableListOf<String>()

        val processHanja = { korean: String, hanja: String, isSurname: Boolean ->
            val info = hanjaDB.getHanjaInfo(korean, hanja, isSurname)
            val ohaeng = when (type) {
                "stroke" -> info?.strokeElement
                "sound" -> info?.soundOhaeng
                else -> DEFAULT_OHAENG
            }
            val key = (ohaeng ?: DEFAULT_OHAENG).toOhaengFull()
            dist[key] = dist[key]!! + 1
            arr.add(key[0].toString())
        }

        hanjaDB.getSurnamePairs(nameInput.surname, nameInput.surnameHanja).forEach { pair ->
            val index = pair.indexOf(SLASH)
            if (index > 0) {
                processHanja(pair.substring(0, index), pair.substring(index + 1), true)
            }
        }

        nameInput.givenName.forEachIndexed { i, char ->
            val hanja = if (i < nameInput.givenNameHanja.length)
                nameInput.givenNameHanja[i].toString()
            else ""
            processHanja(char.toString(), hanja, false)
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
            val index = pair.indexOf(SLASH)
            if (index > 0) {
                processHanja(pair.substring(0, index), pair.substring(index + 1), true)
            }
        }

        nameInput.givenName.forEachIndexed { i, char ->
            val hanja = if (i < nameInput.givenNameHanja.length)
                nameInput.givenNameHanja[i].toString()
            else ""
            processHanja(char.toString(), hanja, false)
        }

        val eumCount = arr.count { it == "음" }
        return EumYangData(eumCount, arr.size - eumCount, arr)
    }

    private fun extractSajuInfo(time: TimePointResult): SajuInfo {
        val year = time.sexagenaryInfo.year
        val month = time.sexagenaryInfo.month
        val day = time.sexagenaryInfo.day
        val hour = time.sexagenaryInfo.hour

        return SajuInfo(
            year[0].toString(), year[1].toString(),
            month[0].toString(), month[1].toString(),
            day[0].toString(), day[1].toString(),
            hour[0].toString(), hour[1].toString()
        )
    }

    private fun analyzeSageokSuri(nameInput: NameInput, cache: HanjaInfoCache): SageokSuri {
        // 획수 미리 계산
        val surnameStrokes = ArrayList<Int>(cache.surnameInfos.size)
        cache.surnameInfos.forEach { info ->
            surnameStrokes.add(info?.originalStrokes ?: 0)
        }

        val givenNameStrokes = ArrayList<Int>(cache.givenNameInfos.size)
        cache.givenNameInfos.forEach { info ->
            givenNameStrokes.add(info?.originalStrokes ?: 0)
        }

        val allStrokes = surnameStrokes + givenNameStrokes
        val nameStrokes = if (givenNameStrokes.size == 1) {
            givenNameStrokes + 0
        } else {
            givenNameStrokes
        }

        val halfSize = nameStrokes.size / 2
        val myeongsangja = nameStrokes.subList(0, halfSize).sum()
        val myeonghaja = nameStrokes.subList(halfSize, nameStrokes.size).sum()

        val surnameSum = surnameStrokes.sum()
        val jeong = allStrokes.sum()
        val won = nameStrokes.sum()
        val i = surnameSum + myeonghaja
        val hyeong = surnameSum + myeongsangja

        val adjusted = intArrayOf(
            adjustTo81(won),
            adjustTo81(hyeong),
            adjustTo81(i),
            adjustTo81(jeong)
        )

        return SageokSuri(
            adjusted[0], getMeaning(adjusted[0], "luckyLevel"), getMeaning(adjusted[0], "summary"),
            adjusted[1], getMeaning(adjusted[1], "luckyLevel"), getMeaning(adjusted[1], "summary"),
            adjusted[2], getMeaning(adjusted[2], "luckyLevel"), getMeaning(adjusted[2], "summary"),
            adjusted[3], getMeaning(adjusted[3], "luckyLevel"), getMeaning(adjusted[3], "summary")
        )
    }

    private fun getMeaning(strokes: Int, field: String): String {
        val meaning = strokeDB.getStrokeMeaning(strokes) ?: return ""
        return if (field == "luckyLevel") meaning.luckyLevel else meaning.summary
    }

    private inline fun adjustTo81(value: Int): Int = ((value - 1) % 81) + 1

    private fun analyzeOhaengFromSageok(sageokSuri: SageokSuri): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()
        val values = intArrayOf(sageokSuri.iGyeok, sageokSuri.hyeongGyeok, sageokSuri.wonGyeok)
        val arr = ArrayList<String>(3)

        values.forEach { value ->
            val ohaeng = value.toOhaengByLastDigit()
            dist[ohaeng] = dist[ohaeng]!! + 1
            arr.add(ohaeng[0].toString())
        }

        return OhaengData(dist, arr)
    }

    private fun analyzeEumYangFromSageok(sageokSuri: SageokSuri): EumYangData {
        val values = intArrayOf(sageokSuri.iGyeok, sageokSuri.hyeongGyeok, sageokSuri.wonGyeok)
        val arr = ArrayList<String>(3)
        var eumCount = 0

        values.forEach { value ->
            val eumyang = value.toEumYang()
            arr.add(eumyang)
            if (eumyang == "음") eumCount++
        }

        return EumYangData(eumCount, 3 - eumCount, arr)
    }

    private fun analyzeSajuOhaeng(sajuInfo: SajuInfo): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()
        val stems = arrayOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        val branches = arrayOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)

        stems.forEach { stem ->
            Constants.STEM_OHAENG[stem]?.let { ohaeng ->
                dist[ohaeng] = dist[ohaeng]!! + 1
            }
        }

        branches.forEach { branch ->
            Constants.BRANCH_OHAENG[branch]?.let { ohaeng ->
                dist[ohaeng] = dist[ohaeng]!! + 1
            }
        }

        return OhaengData(dist)
    }

    private fun analyzeSajuEumYang(sajuInfo: SajuInfo): EumYangData {
        var eum = 0
        var yang = 0

        val stems = arrayOf(sajuInfo.yearStem, sajuInfo.monthStem, sajuInfo.dayStem, sajuInfo.hourStem)
        val branches = arrayOf(sajuInfo.yearBranch, sajuInfo.monthBranch, sajuInfo.dayBranch, sajuInfo.hourBranch)

        stems.forEach { stem ->
            if (Constants.YANG_STEMS.contains(stem)) yang++ else eum++
        }

        branches.forEach { branch ->
            if (Constants.YANG_BRANCHES.contains(branch)) yang++ else eum++
        }

        return EumYangData(eum, yang)
    }

    private fun analyzeJawonOhaeng(nameInput: NameInput): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()

        nameInput.givenName.forEachIndexed { i, char ->
            val hanja = if (i < nameInput.givenNameHanja.length)
                nameInput.givenNameHanja[i].toString()
            else ""
            val info = hanjaDB.getHanjaInfo(char.toString(), hanja, false)
            val key = (info?.sourceElement ?: DEFAULT_OHAENG).toOhaengFull()
            dist[key] = dist[key]!! + 1
        }

        return OhaengData(dist)
    }

    private fun analyzeJawonOhaengWithCache(cache: HanjaInfoCache): OhaengData {
        val dist = EMPTY_OHAENG_DIST.toMutableMap()

        cache.givenNameInfos.forEach { info ->
            if (info != null) {
                val key = info.sourceElement.toOhaengFull()
                dist[key] = dist[key]!! + 1
            }
        }

        return OhaengData(dist)
    }

    private fun combineSajuNameOhaeng(sajuOhaeng: OhaengData, jawonOhaeng: OhaengData): OhaengData {
        val combined = sajuOhaeng.ohaengDistribution.toMutableMap()
        jawonOhaeng.ohaengDistribution.forEach { (k, v) ->
            combined[k] = combined[k]!! + v
        }
        return OhaengData(combined)
    }

    private fun calculateSajuEumYangScore(eumyang: EumYangData): ScoreDetail {
        val total = eumyang.eumCount + eumyang.yangCount
        if (total == 0) return ScoreDetail(0, 100, "사주 데이터 없음", false)

        val minCount = minOf(eumyang.eumCount, eumyang.yangCount)
        val ratio = minCount.toDouble() / total

        val score = when {
            ratio >= 0.4 -> 100
            ratio >= 0.375 -> 90
            ratio >= 0.25 -> 70
            ratio >= 0.125 -> 50
            else -> 30
        }

        val ratioPercent = (ratio * 100).toInt()
        val oppositePercent = ((1 - ratio) * 100).toInt()

        return ScoreDetail(
            score, 100,
            "사주음양 - 음${eumyang.eumCount}:양${eumyang.yangCount} ($ratioPercent:$oppositePercent)",
            ratio >= 0.25
        )
    }

    private fun calculateJawonOhaengScore(sajuOhaeng: OhaengData, jawonOhaeng: OhaengData): ScoreDetail {
        val sajuValues = sajuOhaeng.ohaengDistribution.values
        val sajuAvg = if (sajuValues.isNotEmpty()) sajuValues.average() else 0.0

        var score = 70
        val reasons = mutableListOf<String>()

        val weakOhaengs = sajuOhaeng.ohaengDistribution.filterValues { it < sajuAvg }.keys
        var complemented = 0
        weakOhaengs.forEach { ohaeng ->
            if ((jawonOhaeng.ohaengDistribution[ohaeng] ?: 0) > 0) {
                complemented++
            }
        }

        score += complemented * 10
        if (complemented > 0) {
            reasons.add("${weakOhaengs.joinToString(", ")} 보완")
        }

        val jawonSum = jawonOhaeng.ohaengDistribution.values.sum()
        val jawonBalance = if (jawonSum > 0) {
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
        val weighted = scores.sageokSuriScore.score * WEIGHT_SAGEOK_SURI +
                scores.sajuNameOhaengScore.score * WEIGHT_SAJU_NAME_OHAENG +
                scores.hoeksuEumYangScore.score * WEIGHT_HOEKSU_EUMYANG +
                scores.baleumOhaengScore.score * WEIGHT_BALEUM_OHAENG +
                scores.baleumEumYangScore.score * WEIGHT_BALEUM_EUMYANG +
                scores.sageokSuriOhaengScore.score * WEIGHT_SAGEOK_SURI_OHAENG

        return weighted.toInt().coerceIn(0, 100)
    }
}