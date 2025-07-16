package com.metacomputing.seed.core

import com.metacomputing.seed.domain.model.profile.Profile
import java.util.concurrent.ConcurrentHashMap

class ProfileContext private constructor(
    val profile: Profile,
    private val dataManager: SharedDataManager,
) {
    private val computedData = ConcurrentHashMap<String, Any>()

    companion object {
        fun create(profile: Profile, dataManager: SharedDataManager): ProfileContext {
            return ProfileContext(profile, dataManager)
        }
    }
}