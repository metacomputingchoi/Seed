// util/resource/ResourceLoader.kt
package com.metacomputing.seed.util.resource

import java.io.InputStream

/**
 * 리소스 로더 유틸리티
 */
object ResourceLoader {

    /**
     * 리소스 로드
     */
    fun load(basePath: String, filename: String): String {
        val path = "$basePath/$filename"
        return getResourceAsStream(path)?.use { stream ->
            stream.bufferedReader().readText()
        } ?: throw ResourceNotFoundException("Resource not found: $filename")
    }

    /**
     * 리소스 스트림 가져오기
     */
    fun getResourceAsStream(path: String): InputStream? {
        return ResourceLoader::class.java.getResourceAsStream(path)
    }

    /**
     * 리소스 존재 여부 확인
     */
    fun resourceExists(basePath: String, filename: String): Boolean {
        val path = "$basePath/$filename"
        return getResourceAsStream(path) != null
    }
}

/**
 * 리소스를 찾을 수 없을 때 예외
 */
class ResourceNotFoundException(message: String) : Exception(message)
