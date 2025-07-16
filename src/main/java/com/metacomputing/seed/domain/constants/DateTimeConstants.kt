// domain/constants/DateTimeConstants.kt
package com.metacomputing.seed.domain.constants

import java.time.LocalTime

object DateTimeConstants {

    // 시간대별 경계값
    object TimeSlots {
        val SLOT_BOUNDARIES = listOf(
            LocalTime.of(23, 30), // 자시
            LocalTime.of(1, 30),  // 축시
            LocalTime.of(3, 30),  // 인시
            LocalTime.of(5, 30),  // 묘시
            LocalTime.of(7, 30),  // 진시
            LocalTime.of(9, 30),  // 사시
            LocalTime.of(11, 30), // 오시
            LocalTime.of(13, 30), // 미시
            LocalTime.of(15, 30), // 신시
            LocalTime.of(17, 30), // 유시
            LocalTime.of(19, 30), // 술시
            LocalTime.of(21, 30)  // 해시
        )

        val SLOT_NAMES = listOf(
            "자시(子時)", "축시(丑時)", "인시(寅時)", "묘시(卯時)",
            "진시(辰時)", "사시(巳時)", "오시(午時)", "미시(未時)",
            "신시(申時)", "유시(酉時)", "술시(戌時)", "해시(亥時)"
        )
    }

    // 야자시 관련
    object Yajasi {
        val START_TIME = LocalTime.of(23, 30)
        const val DAY_INCREMENT = 1
    }

    object TimeZone {
        val timezoneOffsetMapping = mapOf(
            // UTC-12 ~ UTC-1
            720 to "Baker Island",          // UTC-12:00
            660 to "Pago Pago",            // UTC-11:00
            600 to "Honolulu",             // UTC-10:00
            570 to "Marquesas Islands",     // UTC-09:30
            540 to "Anchorage",            // UTC-09:00
            480 to "Los Angeles",          // UTC-08:00
            420 to "Denver",               // UTC-07:00
            360 to "Chicago",              // UTC-06:00
            300 to "New York",             // UTC-05:00
            270 to "Caracas",              // UTC-04:30
            240 to "Santiago",             // UTC-04:00
            210 to "St. John's",           // UTC-03:30
            180 to "Buenos Aires",         // UTC-03:00
            120 to "South Georgia",        // UTC-02:00
            60 to "Azores",                // UTC-01:00

            // UTC+0
            0 to "London",                  // UTC+00:00

            // UTC+1 ~ UTC+14
            -60 to "Paris",                 // UTC+01:00
            -120 to "Cairo",                // UTC+02:00
            -180 to "Moscow",               // UTC+03:00
            -210 to "Tehran",               // UTC+03:30
            -240 to "Dubai",                // UTC+04:00
            -270 to "Kabul",                // UTC+04:30
            -300 to "Karachi",              // UTC+05:00
            -330 to "New Delhi",            // UTC+05:30
            -345 to "Kathmandu",            // UTC+05:45
            -360 to "Dhaka",                // UTC+06:00
            -390 to "Yangon",               // UTC+06:30
            -420 to "Bangkok",              // UTC+07:00
            -480 to "Beijing",              // UTC+08:00
            -510 to "Pyongyang",            // UTC+08:30
            -540 to "Seoul",                // UTC+09:00
            -570 to "Darwin",               // UTC+09:30
            -600 to "Sydney",               // UTC+10:00
            -630 to "Adelaide",             // UTC+10:30
            -660 to "Noumea",               // UTC+11:00
            -690 to "Norfolk Island",       // UTC+11:30
            -720 to "Auckland",             // UTC+12:00
            -765 to "Chatham Islands",      // UTC+12:45
            -780 to "Nuku'alofa",          // UTC+13:00
            -840 to "Kiritimati"           // UTC+14:00
        )

        val cityToOffsetMapping = timezoneOffsetMapping.entries.associate { (k, v) -> v to k }
        fun getCityByOffset(offsetMinutes: Int): String =
            timezoneOffsetMapping.getValue(offsetMinutes)

        fun getOffsetByCity(cityName: String?): Int =
            cityToOffsetMapping.getValue(cityName as String)
    }
}
