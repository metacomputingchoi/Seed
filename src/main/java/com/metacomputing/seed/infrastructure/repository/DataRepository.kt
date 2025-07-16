// infrastructure/repository/DataRepository.kt
package com.metacomputing.seed.infrastructure.repository

import com.metacomputing.seed.old.domain.exception.NamingException
import com.metacomputing.seed.old.util.parsing.JsonParser

class DataRepository {
    lateinit var nameCharTripleDict: Map<String, Map<String, Any>>
    lateinit var surnameCharTripleDict: Map<String, Map<String, Any>>
    lateinit var nameKoreanToTripleKeys: Map<String, List<String>>
    lateinit var nameHanjaToTripleKeys: Map<String, List<String>>
    lateinit var surnameKoreanToTripleKeys: Map<String, List<String>>
    lateinit var surnameHanjaToTripleKeys: Map<String, List<String>>
    lateinit var surnameHanjaPairMapping: Map<String, Any>
    lateinit var dictHangulGivenNames: Set<String>
    lateinit var surnameChosungToKorean: Map<String, List<String>>

    lateinit var nameToStat: Map<String, Any>
    lateinit var similarNames: Map<String, Any>
    lateinit var romanNames: Map<String, Any>
    lateinit var hanjaInfo: Map<String, Any>
    lateinit var yearlyRank: Map<String, Any>
    lateinit var yearlyBirths: Map<String, Any>
    lateinit var monthlyBirths: Map<String, Any>
    lateinit var regionalBirths: Map<String, Any>


    lateinit var strokeMeanings: Map<Int, Map<String, Any>>

    fun loadFromJson(
        nameCharTripleJson: String,
        surnameCharTripleJson: String,
        nameKoreanToTripleJson: String,
        nameHanjaToTripleJson: String,
        surnameKoreanToTripleJson: String,
        surnameHanjaToTripleJson: String,
        surnameHanjaPairJson: String,
        dictHangulGivenNamesJson: String,
        surnameChosungToKoreanJson: String,
        nameToStatJson: String,
        strokeMeaningsJson: String,
        similarNamesJson: String,
        romanNamesJson: String,
        hanjaInfoJson: String,
        yearlyRankJson: String,
        yearlyBirthsJson: String,
        monthlyBirthsJson: String,
        regionalBirthsJson: String
    ) {
        try {
            nameCharTripleDict = JsonParser.parseJsonToMap(nameCharTripleJson)
            surnameCharTripleDict = JsonParser.parseJsonToMap(surnameCharTripleJson)
            nameKoreanToTripleKeys = JsonParser.parseJsonToMapList(nameKoreanToTripleJson)
            nameHanjaToTripleKeys = JsonParser.parseJsonToMapList(nameHanjaToTripleJson)
            surnameKoreanToTripleKeys = JsonParser.parseJsonToMapList(surnameKoreanToTripleJson)
            surnameHanjaToTripleKeys = JsonParser.parseJsonToMapList(surnameHanjaToTripleJson)
            surnameHanjaPairMapping = JsonParser.parseSurnameHanjaPairMapping(surnameHanjaPairJson)
            surnameChosungToKorean = JsonParser.parseJsonToMapList(surnameChosungToKoreanJson)
            dictHangulGivenNames = JsonParser.parseDictHangulGivenNames(dictHangulGivenNamesJson)

            nameToStat = JsonParser.parseJsonToMap(nameToStatJson)

            similarNames = JsonParser.parseJsonToMap(similarNamesJson)
            romanNames = JsonParser.parseJsonToMap(romanNamesJson)
            hanjaInfo = JsonParser.parseJsonToMap(hanjaInfoJson)
            yearlyRank = JsonParser.parseJsonToMap(yearlyRankJson)
            yearlyBirths = JsonParser.parseJsonToMap(yearlyBirthsJson)
            monthlyBirths = JsonParser.parseJsonToMap(monthlyBirthsJson)
            regionalBirths = JsonParser.parseJsonToMap(regionalBirthsJson)

            strokeMeanings = JsonParser.parseStrokeMeanings(strokeMeaningsJson)

        } catch (e: Exception) {
            throw NamingException.ConfigurationException(
                "데이터 로드 실패",
                cause = e
            )
        }
    }
}