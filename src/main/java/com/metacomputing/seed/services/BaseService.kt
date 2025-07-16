// services/BaseService.kt
package com.metacomputing.seed.services

import com.metacomputing.seed.core.ProfileContext

interface BaseService {
    val context: ProfileContext
    val serviceType: ServiceType
}

enum class ServiceType {
    NAMING,              // 작명
    DAILY_FORTUNE,       // 일일 운세
    YEARLY_FORTUNE,      // 연간 운세
    COMPATIBILITY,       // 궁합
    PERSONALITY,         // 성격 분석
    AI_ANALYSIS,         // AI 통합 분석
    BIORHYTHM,          // 바이오리듬
    FORTUNE_COOKIE      // 일일 포츈쿠키
}

sealed class ServiceResult<out T> {
    data class Success<T>(val data: T) : ServiceResult<T>()
    data class Error(val error: ServiceError) : ServiceResult<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw ServiceException(error)
    }

    inline fun <R> map(transform: (T) -> R): ServiceResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): ServiceResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (ServiceError) -> Unit): ServiceResult<T> {
        if (this is Error) action(error)
        return this
    }
}

data class ServiceError(
    val code: String,
    val message: String,
    val details: Map<String, Any> = emptyMap()
) {
    companion object {
        fun invalidInput(message: String) = ServiceError("INVALID_INPUT", message)
        fun notFound(message: String) = ServiceError("NOT_FOUND", message)
        fun internal(message: String) = ServiceError("INTERNAL_ERROR", message)
    }
}

class ServiceException(
    val error: ServiceError
) : Exception(error.message)
