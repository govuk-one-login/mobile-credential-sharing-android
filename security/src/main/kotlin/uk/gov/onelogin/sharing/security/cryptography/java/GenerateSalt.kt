package uk.gov.onelogin.sharing.security.cryptography.java

import uk.gov.onelogin.sharing.security.cryptography.Constants.HASH_ALGORITHM_SHA256
import java.security.MessageDigest

/**
 * Generate salt bytes via a cryptographic hashing function using SHA-256
 *
 * @return A [ByteArray] object representing the fixed length hashed bytes
 */
fun generateSalt(
    byteArray: ByteArray
): ByteArray {
    return MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(byteArray)
}
