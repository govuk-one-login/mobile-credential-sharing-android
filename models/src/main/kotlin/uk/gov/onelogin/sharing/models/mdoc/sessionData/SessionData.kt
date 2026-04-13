package uk.gov.onelogin.sharing.models.mdoc.sessionData

/**
 * Represents the ISO 18013-5 `SessionData` transport envelope.
 *
 * This is the top-level message structure used to communicate with the Verifier over the BLE
 * transport layer. It can carry an encrypted credential payload, a termination/error status code,
 * or both.
 *
 * ```
 * SessionData = {
 *     ? "data" : bstr,
 *     ? "status" : uint
 * }
 * ```
 *
 * @param data The encrypted ciphertext and authentication tag, or null if not present.
 * @param status The termination status code, or null if not present.
 */
data class SessionData(val data: ByteArray? = null, val status: SessionDataStatus? = null)
