// services/naming/NamingService.kt
package com.metacomputing.seed.services.naming

import com.metacomputing.seed.domain.model.profile.Profile
import com.metacomputing.seed.services.BaseService
import com.metacomputing.seed.services.ServiceResult

interface NamingService : BaseService {
    fun generate(
        profile: Profile,
        config: NamingConfig
    ): ServiceResult<List<Any>>
}
