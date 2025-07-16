// util/logging/PrintLogger.kt
package com.metacomputing.seed.util.logging

class PrintLogger(private val tag: String) : Logger {
    override fun d(message: String) {
        println("[$tag] DEBUG: $message")
    }

    override fun e(message: String, throwable: Throwable?) {
        println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }

    override fun w(message: String) {
        println("[$tag] WARMING: $message")
    }

    override fun v(message: String) {
        println("[$tag] VERBOSE: $message")
    }

    override fun i(message: String) {
        println("[$tag] INFO: $message")
    }
}
