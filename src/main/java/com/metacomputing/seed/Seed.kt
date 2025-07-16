// Seed.kt
package com.metacomputing.seed

import com.metacomputing.seed.core.DataLoader
import com.metacomputing.seed.core.ProfileContext
import com.metacomputing.seed.core.SharedDataManager
import com.metacomputing.seed.domain.model.profile.Profile
import com.metacomputing.seed.core.DataConfig
import com.metacomputing.seed.services.ServiceFactory
import com.metacomputing.seed.services.naming.NamingService
import com.metacomputing.seed.util.logging.Logger
import com.metacomputing.seed.util.logging.PrintLogger
import java.util.concurrent.ConcurrentHashMap

class Seed private constructor(
    private val dataManager: SharedDataManager,
    private val serviceFactory: ServiceFactory,
) {
    private val profileContexts = ConcurrentHashMap<String, ProfileContext>()

    fun getContext(profile: Profile): ProfileContext {
        val key = profile.getCacheKey()
        return profileContexts.computeIfAbsent(key) {
            ProfileContext.create(profile, dataManager)
        }
    }

    fun naming(profile: Profile): NamingService {
        val context = getContext(profile)
        return serviceFactory.createNamingService(context)
    }

    companion object {
        private const val TAG = "Seed"

        @JvmStatic
        @JvmOverloads
        fun create(
            dataConfig: DataConfig,
            logger: Logger? = null
        ): Seed {
            val actualLogger = logger ?: PrintLogger(TAG)

            try {
                val dataLoader = DataLoader(dataConfig, actualLogger)
                val dataManager = SharedDataManager(dataLoader, actualLogger)
                val serviceFactory = ServiceFactory(dataManager, actualLogger)

                actualLogger.i("Seed SDK initialized successfully")

                return Seed(dataManager, serviceFactory)
            } catch (e: Exception) {
                throw SeedInitializationException("Failed to initialize Seed SDK: ${e.message}", e)
            }
        }
    }
}

class SeedInitializationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)