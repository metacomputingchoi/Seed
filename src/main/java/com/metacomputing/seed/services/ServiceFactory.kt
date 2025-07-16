// services/ServiceFactory.kt
package com.metacomputing.seed.services

import com.metacomputing.seed.core.*
import com.metacomputing.seed.services.naming.NamingService
import com.metacomputing.seed.services.naming.NamingServiceImpl
import com.metacomputing.seed.util.logging.Logger

class ServiceFactory(
    private val dataManager: SharedDataManager,
    private val logger: Logger
) {
    fun createNamingService(context: ProfileContext): NamingService {
        return NamingServiceImpl(
            context = context,
            dataManager = dataManager,
            logger = logger
        )
    }
}
