package com.metacomputing.seed.domain.model.profile

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult
import com.metacomputing.seed.domain.model.name.Name
import com.metacomputing.seed.domain.constants.DateTimeConstants
import java.time.LocalDateTime

data class Profile(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: Name,
    val birthDateTime: LocalDateTime,
    val birthPlace: String? = null,
    val currentPlace: String? = null,
    val currentDateTime: LocalDateTime = LocalDateTime.now(),
    val gender: Gender? = null,
    val bloodType: BloodType? = null,
    val mbti: String? = null,
    val applyYajasi: Boolean = true,
    val metadata: Map<String, Any> = emptyMap(),
    val birthManseryeokData: TimePointResult? = null,      // 출생지 기준 만세력
    val currentManseryeokData: TimePointResult? = null     // 현재 거주지 기준 만세력
) {
    fun getCacheKey(): String = "$id:${birthDateTime.toLocalDate()}"

    companion object {
        fun create(
            name: Name,
            birthDateTime: LocalDateTime,
            birthPlace: String? = null,
            currentPlace: String? = null,
            currentDateTime: LocalDateTime = LocalDateTime.now(),
            gender: Gender? = null,
            bloodType: BloodType? = null,
            mbti: String? = null,
            applyYajasi: Boolean = true
        ): Profile {
            // 출생지 기반 timezone
            val birthCity = getCityFromPlace(birthPlace)
            val birthTimezOffset = DateTimeConstants.TimeZone.getOffsetByCity(birthCity)

            // 현재 거주지 기반 timezone
            val currentCity = getCityFromPlace(currentPlace ?: birthPlace)
            val currentTimezOffset = DateTimeConstants.TimeZone.getOffsetByCity(currentCity)

            // 출생 시점 만세력 계산
            var adjustedBirthDateTime = birthDateTime
            var birthYeolidxAdd = 0

            if (birthDateTime.toLocalTime() >= DateTimeConstants.Yajasi.START_TIME) {
                birthYeolidxAdd = DateTimeConstants.Yajasi.DAY_INCREMENT
                if (!applyYajasi) {
                    adjustedBirthDateTime = adjustedBirthDateTime.plusDays(1)
                }
            }

            // 분을 시간으로 변환 (CalSDK가 시간 단위를 받는다고 가정)
            val birthTimezOffsetHours = birthTimezOffset / 60

            val birthManseryeokData = CalSDK.getTimePointData(
                adjustedBirthDateTime.year,
                adjustedBirthDateTime.monthValue,
                adjustedBirthDateTime.dayOfMonth,
                adjustedBirthDateTime.hour,
                adjustedBirthDateTime.minute,
                birthTimezOffsetHours,
                birthYeolidxAdd
            )

            // 현재 시점 만세력 계산
            var adjustedCurrentDateTime = currentDateTime
            var currentYeolidxAdd = 0

            if (currentDateTime.toLocalTime() >= DateTimeConstants.Yajasi.START_TIME) {
                currentYeolidxAdd = DateTimeConstants.Yajasi.DAY_INCREMENT
                if (!applyYajasi) {
                    adjustedCurrentDateTime = adjustedCurrentDateTime.plusDays(1)
                }
            }

            val currentTimezOffsetHours = currentTimezOffset / 60

            val currentManseryeokData = CalSDK.getTimePointData(
                adjustedCurrentDateTime.year,
                adjustedCurrentDateTime.monthValue,
                adjustedCurrentDateTime.dayOfMonth,
                adjustedCurrentDateTime.hour,
                adjustedCurrentDateTime.minute,
                currentTimezOffsetHours,
                currentYeolidxAdd
            )

            return Profile(
                name = name,
                birthDateTime = birthDateTime,
                birthPlace = birthPlace,
                currentPlace = currentPlace,
                currentDateTime = currentDateTime,
                gender = gender,
                bloodType = bloodType,
                mbti = mbti,
                applyYajasi = applyYajasi,
                birthManseryeokData = birthManseryeokData,
                currentManseryeokData = currentManseryeokData
            )
        }

        private fun getCityFromPlace(place: String?): String {
            return when {
                place == null -> "Seoul"

                // 한국
                place.contains("서울") || place.contains("한국") ||
                        place.contains("부산") || place.contains("대구") ||
                        place.contains("인천") || place.contains("대전") -> "Seoul"

                // 일본
                place.contains("東京") || place.contains("도쿄") ||
                        place.contains("東京都") || place.contains("日本") ||
                        place.contains("大阪") || place.contains("오사카") -> "Seoul" // Tokyo는 Seoul과 같은 시간대

                // 중국
                place.contains("北京") || place.contains("베이징") ||
                        place.contains("上海") || place.contains("상하이") ||
                        place.contains("广州") || place.contains("광저우") -> "Beijing"

                // 대만
                place.contains("台北") || place.contains("타이베이") ||
                        place.contains("台湾") || place.contains("대만") -> "Beijing"

                // 동남아시아
                place.contains("싱가포르") || place.contains("Singapore") -> "Beijing"
                place.contains("방콕") || place.contains("Bangkok") -> "Bangkok"

                // 미국
                place.contains("뉴욕") || place.contains("New York") -> "New York"
                place.contains("로스앤젤레스") || place.contains("Los Angeles") -> "Los Angeles"
                place.contains("시카고") || place.contains("Chicago") -> "Chicago"

                // 유럽
                place.contains("런던") || place.contains("London") -> "London"
                place.contains("파리") || place.contains("Paris") -> "Paris"

                else -> "Seoul"  // 기본값
            }
        }
    }
}