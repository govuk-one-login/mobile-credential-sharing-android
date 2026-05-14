package uk.gov.onelogin.sharing.models.mdoc.sessionData

/**
 * Represents the status codes defined by ISO 18013-5 for the `SessionData` transport envelope.
 *
 * @param code The unsigned integer value transmitted in the CBOR `"status"` field.
 */
enum class SessionDataStatus(val code: UInt) {
    ERROR_SESSION_ENCRYPTION(10u),
    ERROR_CBOR_DECODING(11u),
    SESSION_TERMINATION(20u);

    companion object {
        val applicableCodes = SessionDataStatus.entries.map(SessionDataStatus::code)
        fun from(code: UInt? = null): SessionDataStatus? = code?.let { code ->
            require(code in applicableCodes) {
                "Received invalid session data status: $code"
            }

            SessionDataStatus.entries.first { code == it.code }
        }
    }
}
