package com.metacomputing.seed.services.naming

data class NamingConfig(
    val useYajasi: Boolean = false,
    val withoutFilter: Boolean = false,
    val preferredHanja: List<String> = emptyList(),
    val avoidHanja: List<String> = emptyList(),
    val preferredMeaning: List<String> = emptyList(),
    val useExhaustiveSearch: Boolean = false,
    val sagyeokScoringWeight: Float = 0.8f,
    val sagyeokTopPercentile: Float = 0.10f,
    val requireMinScore: Boolean = true,
    val minScoreThreshold: Int = 60,
)