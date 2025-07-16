package com.metacomputing.seed.domain.model.profile

enum class BloodType(val display: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-");

    val baseType: String
        get() = when (this) {
            A_POSITIVE, A_NEGATIVE -> "A"
            B_POSITIVE, B_NEGATIVE -> "B"
            O_POSITIVE, O_NEGATIVE -> "O"
            AB_POSITIVE, AB_NEGATIVE -> "AB"
        }

    val rhType: String
        get() = when (this) {
            A_POSITIVE, B_POSITIVE, O_POSITIVE, AB_POSITIVE -> "+"
            A_NEGATIVE, B_NEGATIVE, O_NEGATIVE, AB_NEGATIVE -> "-"
        }

    override fun toString(): String = display

    companion object {
        fun fromString(value: String): BloodType? {
            return values().find {
                it.display.equals(value, ignoreCase = true) ||
                        it.name.equals(value, ignoreCase = true)
            }
        }

        fun from(base: String, rh: String): BloodType? {
            val normalizedBase = base.uppercase()
            val normalizedRh = rh.replace("positive", "+")
                .replace("negative", "-")
                .replace("pos", "+")
                .replace("neg", "-")

            return fromString("$normalizedBase$normalizedRh")
        }
    }
}