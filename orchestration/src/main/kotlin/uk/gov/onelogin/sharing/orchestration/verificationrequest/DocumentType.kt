package uk.gov.onelogin.sharing.orchestration.verificationrequest

sealed interface DocumentType {
    val value: String

    data object Mdl : DocumentType {
        override val value = "org.iso.18013.5.1.mDL"
    }

    data class Custom(override val value: String) : DocumentType
}
