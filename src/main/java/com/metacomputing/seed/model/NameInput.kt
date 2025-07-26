// model/NameInput.kt
package com.metacomputing.seed.model

import com.metacomputing.mcalendar.CalSDK
import com.metacomputing.mcalendar.TimePointResult

data class NameInput(
    val surname: String,
    val surnameHanja: String,
    val givenName: String,
    val givenNameHanja: String,
    val birthYear: Int,
    val birthMonth: Int,
    val birthDay: Int,
    val birthHour: Int,
    val birthMinute: Int,
    val timezoneOffset: Int,
    val timePointResult: TimePointResult,
    val birthDateTime: String
) {
    companion object {
        fun create(
            surname: String,
            surnameHanja: String,
            givenName: String,
            givenNameHanja: String,
            birthYear: Int,
            birthMonth: Int,
            birthDay: Int,
            birthHour: Int,
            birthMinute: Int,
            timezoneOffset: Int
        ): NameInput {
            val timePointResult = CalSDK.getTimePointData(
                year = birthYear,
                month = birthMonth,
                day = birthDay,
                hour = birthHour,
                minute = birthMinute,
                timezOffset = timezoneOffset,
                lang = 0
            )

            val birthDateTime = String.format(
                "%04d년 %02d월 %02d일 %02d시 %02d분",
                birthYear, birthMonth, birthDay, birthHour, birthMinute
            )

            return NameInput(
                surname = surname,
                surnameHanja = surnameHanja,
                givenName = givenName,
                givenNameHanja = givenNameHanja,
                birthYear = birthYear,
                birthMonth = birthMonth,
                birthDay = birthDay,
                birthHour = birthHour,
                birthMinute = birthMinute,
                timezoneOffset = timezoneOffset,
                timePointResult = timePointResult,
                birthDateTime = birthDateTime
            )
        }
    }
}
