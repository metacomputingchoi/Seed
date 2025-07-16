// util/logging/Logger.kt
package com.metacomputing.seed.util.logging

interface Logger {
    fun d(message: String)
    fun e(message: String, throwable: Throwable? = null)
    fun w(message: String)
    fun v(message: String)
    fun i(message: String)
}
