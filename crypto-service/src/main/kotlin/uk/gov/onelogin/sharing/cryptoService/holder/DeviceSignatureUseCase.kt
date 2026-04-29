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
)

/**
 * Exception thrown when device signature creation fails.
 */
class DeviceSignatureException(message: String, cause: Throwable? = null) :
    Exception(message, cause)
