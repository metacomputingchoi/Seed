package com.metacomputing.seed.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class StrokeData(
    @SerialName("stroke_meanings")
    val strokeMeanings: Map<String, StrokeMeaning>
)

@Serializable
data class StrokeMeaning(
    val number: Int,
    val title: String,
    val summary: String,
    @SerialName("detailed_explanation")
    val detailedExplanation: String,
    @SerialName("positive_aspects")
    val positiveAspects: String,
    @SerialName("caution_points")
    val cautionPoints: String,
    @SerialName("personality_traits")
    val personalityTraits: List<String>,
    @SerialName("suitable_career")
    val suitableCareer: List<String>,
    @SerialName("life_period_influence")
    val lifePeriodInfluence: String?,
    @SerialName("special_characteristics")
    val specialCharacteristics: String?,
    @SerialName("challenge_period")
    val challengePeriod: String?,
    @SerialName("opportunity_area")
    val opportunityArea: String?,
    @SerialName("lucky_level")
    val luckyLevel: String
)
