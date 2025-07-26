// search/SearchResult.kt
package com.metacomputing.seed.search

data class SearchResult(
    val surname: String,
    val surnameHanja: String,
    val givenName: String,
    val givenNameHanja: String
) {
    val fullName: String = surname + givenName
    val fullNameHanja: String = surnameHanja + givenNameHanja
}