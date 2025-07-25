package com.metacomputing.seed.model

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
    val timezoneOffset: Int
)
