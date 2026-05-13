package uk.gov.onelogin.sharing.cryptoService.holder

/**
 * Service for handling COSE_Sign1 construction from a raw signature.
 */
interface DeviceSignatureUseCase {
    /**
     * Constructs the COSE_Sign1, DeviceAuth, and DeviceSigned structures from a raw signature.
     *
     * @param signatureBytes The raw signature bytes returned by the credential provider
     * @return DeviceSignatureResult containing the constructed structures
     */
    fun buildDeviceSignedStructures(signatureBytes: ByteArray): DeviceSignatureResult

    /**
     * Builds the COSE Sig_structure for COSE_Sign1 per RFC 9052 4.4:
     * https://www.rfc-editor.org/rfc/rfc9052#name-signing-and-verification-pr
     *
     * `["Signature1", body_protected, external_aad, payload]`
     *
     * Note: `body_protected` can also be referred to as `protected_header` (outdated RFC 8152)
     *
     * @param payload The DeviceAuthentication bytes to be signed
     * @return The serialized CBOR Sig_structure
     */
    fun buildCoseSignStructure(payload: ByteArray): ByteArray
}

/**
 * Result of device signature creation containing all constructed structures.
 *
 * @property coseSign1Array The untagged COSE_Sign1 array with 4 elements
 * @property deviceAuth The DeviceAuth object containing deviceSignature
 * @property deviceSigned The complete DeviceSigned structure
 */
data class DeviceSignatureResult(
    val coseSign1Array: ByteArray,
    val deviceAuth: ByteArray,
    val deviceSigned: ByteArray
) {
    override fun toString(): String = "DeviceSignatureResult(redacted)"
}

/**
 * Exception thrown when device signature creation fails.
 */
class DeviceSignatureException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
