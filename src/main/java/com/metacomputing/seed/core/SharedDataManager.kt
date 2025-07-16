// core/SharedDataManager.kt
package com.metacomputing.seed.core

import com.metacomputing.seed.util.logging.Logger
import com.metacomputing.seed.infrastructure.repository.DataRepository

class SharedDataManager(
    private val dataLoader: DataLoader,
    private val logger: Logger
) {
    // 데이터 저장소
    val dataRepository: DataRepository by lazy {
        dataLoader.loadDataRepository()
    }
}