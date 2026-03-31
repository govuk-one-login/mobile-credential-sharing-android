package uk.gov.onelogin.sharing.orchestration.verificationrequest

sealed interface RequestElement {
    val value: String
    fun validate(data: Any): Boolean

    data object FamilyName : RequestElement {
        override val value = "family_name"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object GivenName : RequestElement {
        override val value = "given_name"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object BirthDate : RequestElement {
        override val value = "birth_date"
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    data object IssueDate : RequestElement {
        override val value = "issue_date"
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    data object ExpiryDate : RequestElement {
        override val value = "expiry_date"
        override fun validate(data: Any) = data is String && FULL_DATE_PATTERN.matches(data)
    }

    data object IssuingCountry : RequestElement {
        override val value = "issuing_country"
        override fun validate(data: Any) = data is String && data.length == COUNTRY_CODE_LENGTH
    }

    data object IssuingAuthority : RequestElement {
        override val value = "issuing_authority"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object DocumentNumber : RequestElement {
        override val value = "document_number"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object Portrait : RequestElement {
        override val value = "portrait"
        override fun validate(data: Any) = true
    }

    data object BirthPlace : RequestElement {
        override val value = "birth_place"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object DrivingPrivileges : RequestElement {
        override val value = "driving_privileges"
        override fun validate(data: Any) = data is List<*>
    }

    data object UnDistinguishingSign : RequestElement {
        override val value = "un_distinguishing_sign"
        override fun validate(data: Any) = data is String && data.isNotEmpty()
    }

    data object ResidentAddress : RequestElement {
        override val value = "resident_address"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object ResidentPostalCode : RequestElement {
        override val value = "resident_postal_code"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data object ResidentCity : RequestElement {
        override val value = "resident_city"
        override fun validate(data: Any) = data is String && data.length <= MAX_LENGTH
    }

    data class AgeOver(val age: Int) : RequestElement {
        init {
            require(age in MIN_AGE..MAX_AGE) {
                "age must be between $MIN_AGE and $MAX_AGE, was $age"
            }
        }

        override val value = "age_over_%02d".format(age)
        override fun validate(data: Any) = data is Boolean
    }

    data class Custom(override val value: String) : RequestElement {
        override fun validate(data: Any) = true
    }

    companion object {
        private val FULL_DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}".toRegex()
        private const val COUNTRY_CODE_LENGTH = 2
        private const val MIN_AGE = 0
        private const val MAX_AGE = 99
        const val MAX_LENGTH = 150
    }
}
