package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment

/**
 * Represents the essential data required for establishing a secure session between an mDoc
 * holder and a verifier device.
 *
 * This data class is the internal domain model that holds the cryptographic materials needed
 * to initiate a session, typically after this information has been decoded from a QR code
 * or other engagement method.
 *
 * @param eReaderKey A byte array representing the public key of the verifier's ephemeral key pair.
 *                   This key is used by the mDoc to establish a secure channel. The mapper will
 *                   store the key as tagged CBOR.
 * @param data A byte array containing additional, often encrypted or session-specific,
 *             handover information that the mDoc needs to proceed with the connection.
 */
data class SessionEstablishment(val eReaderKey: ByteArray, val data: ByteArray)
