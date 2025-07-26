// model/NameInput.kt
package com.metacomputing.seed.model

import com.metacomputing.mcalendar.TimePointResult

data class NameInput(
    val surname: String,
    val surnameHanja: String,
    val givenName: String,
    val givenNameHanja: String,
    val timePointResult: TimePointResult
)