package uk.gov.onelogin.sharing.cryptoService.verifier

import java.security.KeyPair
import java.security.interfaces.ECPublicKey
import java.util.UUID

/**
 * Holds ephemeral cryptographic resources for a single Verifier transaction.
 *
 * Created by [VerifierCryptoService.establishSession] with all fields populated.
 * When the session is discarded, these resources are released.
 */
data class VerifierCryptoContext(
    /** The base64url-encoded Device Engagement string from the scanned QR code. */
    val engagementString: String,
    /** The raw, base64url-decoded Device Engagement bytes from the scanned QR code. */
    val deviceEngagementBytes: ByteArray,
    /** The BLE peripheral server mode UUID extracted from the Device Engagement. */
    val serviceUuid: UUID,
    /** The Verifier's ephemeral public key in COSE format, wrapped in CBOR Tag 24. */
    val eReaderKeyTagged: ByteArray,
    /**
     * The CBOR Tag 24 wrapped SessionTranscript, used as salt for key derivation.
     */
    val sessionTranscriptBytes: ByteArray,
    /**
     * The raw (untagged) SessionTranscript CBOR array, used for ReaderAuthentication.
     */
    val rawSessionTranscript: ByteArray,
    /** The Verifier's ephemeral key pair (EReaderKey), retained for shared secret computation. */
    val eReaderKeyPair: KeyPair,
    /** The Holder's ephemeral public key (EDeviceKey.Pub), parsed from the DeviceEngagement. */
    val eDevicePublicKey: ECPublicKey,
    /** The 32-byte session key used by the Verifier to encrypt requests. */
    val skReader: ByteArray,
    /** The 32-byte session key used by the Verifier to decrypt responses. */
    val skDevice: ByteArray,
    /** The message counter for encryption, starts at 1 for SessionEstablishment. */
    val encryptCounter: UInt = 1u,
    /** The message counter for decryption, starts at 1 for the first received message. */
    val decryptCounter: UInt = 1u
)
