package com.metacomputing.seed

import com.metacomputing.seed.core.DataConfig
import com.metacomputing.seed.domain.model.name.Name
import com.metacomputing.seed.domain.model.profile.Profile
import com.metacomputing.seed.domain.model.profile.Gender
import com.metacomputing.seed.domain.model.profile.BloodType
import com.metacomputing.seed.services.naming.NamingConfig
import com.metacomputing.seed.util.logging.PrintLogger
import java.time.LocalDateTime

fun main() {
    val logger = PrintLogger("Seed")
    val seed = Seed.create(
        dataConfig = DataConfig.default(),
        logger = logger
    )

    val profile = Profile.create(
        name = Name.fromTriples(
            Triple("이", "李", false),
            Triple("서", "瑞", false),
            Triple("준", "俊", false)
        ),
        birthDateTime = LocalDateTime.of(2000, 6, 15, 14, 45),
        birthPlace = "Seoul",
        currentPlace = "Los Angeles",
        currentDateTime = LocalDateTime.of(2024, 12, 25, 15, 30),
        gender = Gender.MALE,
        bloodType = BloodType.A_POSITIVE,
        mbti = "INTJ"
    )

    val naming = seed.naming(profile)
    println(naming.generate(profile, NamingConfig()))
}
