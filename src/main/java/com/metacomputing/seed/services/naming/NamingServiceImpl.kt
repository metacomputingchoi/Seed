// services/naming/NamingServiceImpl.kt
package com.metacomputing.seed.services.naming

import com.metacomputing.seed.core.ProfileContext
import com.metacomputing.seed.core.SharedDataManager
import com.metacomputing.seed.services.ServiceType
import com.metacomputing.seed.services.ServiceResult
import com.metacomputing.seed.domain.model.profile.Profile
import com.metacomputing.seed.util.logging.Logger

internal class NamingServiceImpl(
    override val context: ProfileContext,
    val dataManager: SharedDataManager,
    private val logger: Logger
) : NamingService {

    override val serviceType = ServiceType.NAMING

    override fun generate(
        profile: Profile,
        config: NamingConfig
    ): ServiceResult<List<Any>> {
        return ServiceResult.Success(emptyList())
    }
}