package uk.gov.onelogin.sharing.security.secureArea.session

fun interface SessionKeyGenerator {
    /**
     * Generates a single session key from a given [sharedKey], a generated cryptographic
     * salt created from the [sessionTranscriptBytes] and a string containing the
     * corresponding role: "SkReader" and "SkDevice"
     *
     * Session keys are generated deterministically by each party, and used in the subsequent
     * encryption and decryption of messages between devices
     *
     * @return [ByteArray] object representing the session key
     */
    fun deriveSessionKey(
        sharedKey: ByteArray,
        sessionTranscriptBytes: ByteArray,
        role: DeviceRole
    ): ByteArray

    companion object {
        enum class DeviceRole {
            VERIFIER,
            HOLDER
        }
    }
}
