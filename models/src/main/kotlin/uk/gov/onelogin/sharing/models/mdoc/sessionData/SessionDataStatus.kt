package uk.gov.onelogin.sharing.models.mdoc.sessionData

/**
 * Represents the status codes defined by ISO 18013-5 for the `SessionData` transport envelope.
 *
 * @param code The unsigned integer value transmitted in the CBOR `"status"` field.
 */
enum class SessionDataStatus(val code: UInt) {
    SESSION_TERMINATION(20u)
}
