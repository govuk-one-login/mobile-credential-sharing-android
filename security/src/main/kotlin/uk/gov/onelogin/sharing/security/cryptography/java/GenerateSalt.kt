package uk.gov.onelogin.sharing.security.cryptography.java

import java.security.MessageDigest
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cryptography.Constants.HASH_ALGORITHM_SHA256

/**
 * Generate salt bytes via a cryptographic hashing function using SHA-256
 *
 * @return A [ByteArray] object representing the fixed length hashed bytes
 */

fun generateSalt(byteArray: ByteArray): ByteArray {
    val saltInput = EmbeddedCbor(byteArray).encodeCbor()
    val result = MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(saltInput)

    return result
}
