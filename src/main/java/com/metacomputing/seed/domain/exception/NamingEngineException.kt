// domain/exception/NamingEngineException.kt
package com.metacomputing.seed.domain.exception

class SeedException(
    message: String,
    cause: Throwable? = null
) : Exception("Seed error: $message", cause)