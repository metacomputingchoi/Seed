package com.metacomputing.seed.core

data class DataConfig(
    val resourcePath: String = "/seed/data",
    val nameStatPath: String = "name_to_stat",
    val lazyLoading: Boolean = true,
    val preloadEssentials: Boolean = true
) {
    companion object {
        fun default() = DataConfig()

        fun production() = DataConfig(
            lazyLoading = false,
            preloadEssentials = true
        )
    }
}
