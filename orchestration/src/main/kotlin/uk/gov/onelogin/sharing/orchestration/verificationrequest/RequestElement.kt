package uk.gov.onelogin.sharing.orchestration.verificationrequest

sealed interface RequestElement {
    val value: String

    data object GivenName : RequestElement {
        override val value = "given_name"
    }
    data class AgeOver(val age: Int) : RequestElement {
        override val value = "age_over_$age"
    }
    data object FamilyName : RequestElement {
        override val value = "family_name"
    }
    data object Portrait : RequestElement {
        override val value = "portrait"
    }

    data class Custom(override val value: String) : RequestElement
}
