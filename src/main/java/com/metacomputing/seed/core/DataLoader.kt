// core/DataLoader.kt
package com.metacomputing.seed.core

import com.metacomputing.seed.infrastructure.repository.DataRepository
import com.metacomputing.seed.util.logging.Logger
import com.metacomputing.seed.util.resource.ResourceLoader as BaseResourceLoader
import java.util.concurrent.ConcurrentHashMap

class DataLoader(
    private val config: DataConfig,
    private val logger: Logger
) {
    private val loadedData = ConcurrentHashMap<String, String>()
    private var dataRepository: DataRepository? = null

    fun loadDataRepository(): DataRepository {
        if (dataRepository == null) {
            synchronized(this) {
                if (dataRepository == null) {
                    dataRepository = createDataRepository()
                }
            }
        }
        return dataRepository!!
    }

    private fun createDataRepository(): DataRepository {
        logger.d("Creating DataRepository...")

        val repository = DataRepository()

        // 모든 데이터 로드
        repository.loadFromJson(
            nameCharTripleJson = loadResource("name_char_triple_dict_effective.json"),
            surnameCharTripleJson = loadResource("surname_char_triple_dict.json"),
            nameKoreanToTripleJson = loadResource("name_korean_to_triple_keys_mapping_effective.json"),
            nameHanjaToTripleJson = loadResource("name_hanja_to_triple_keys_mapping_effective.json"),
            surnameKoreanToTripleJson = loadResource("surname_korean_to_triple_keys_mapping.json"),
            surnameHanjaToTripleJson = loadResource("surname_hanja_to_triple_keys_mapping.json"),
            surnameHanjaPairJson = loadResource("surname_hanja_pair_mapping_dict.json"),
            dictHangulGivenNamesJson = loadResource("dict_hangul_given_names.json"),
            surnameChosungToKoreanJson = loadResource("surname_chosung_to_korean_mapping.json"),
            nameToStatJson = loadResource("name_to_stat_minified.json"),
            strokeMeaningsJson = loadResource("stroke_data.json"),
            similarNamesJson = loadStatResource("similar_names.json"),
            romanNamesJson = loadStatResource("roman_names.json"),
            hanjaInfoJson = loadStatResource("hanja_info.json"),
            yearlyRankJson = loadStatResource("yearly_rank.json"),
            yearlyBirthsJson = loadStatResource("yearly_births.json"),
            monthlyBirthsJson = loadStatResource("monthly_births.json"),
            regionalBirthsJson = loadStatResource("regional_births.json")
        )

        logger.d("DataRepository created successfully")
        return repository
    }

    private fun loadResource(filename: String): String {
        return loadedData.computeIfAbsent(filename) {
            logger.d("Loading resource: $filename")
            BaseResourceLoader.load(config.resourcePath, filename)
        }
    }

    private fun loadStatResource(filename: String): String {
        return loadedData.computeIfAbsent("${config.nameStatPath}/$filename") {
            logger.d("Loading stat resource: $filename")
            BaseResourceLoader.load(config.resourcePath, "${config.nameStatPath}/$filename")
        }
    }

    fun clearCache() {
        loadedData.clear()
        dataRepository = null
        logger.d("DataLoader cache cleared")
    }
}