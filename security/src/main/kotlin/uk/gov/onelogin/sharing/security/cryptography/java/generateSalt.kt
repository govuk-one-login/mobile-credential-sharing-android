package uk.gov.onelogin.sharing.security.cryptography.java

import uk.gov.onelogin.sharing.security.engagement.EngagementAlgorithms.JVM_DIGEST_ALGORITHM_NAME
import java.security.MessageDigest

/**
 * Generate salt bytes via a cryptographic hashing function using SHA-256
 *
 * @return A [ByteArray] object representing the fixed length hashed bytes
 */
fun generateSalt(
    byteArray: ByteArray
): ByteArray {
    return MessageDigest.getInstance(JVM_DIGEST_ALGORITHM_NAME).digest(byteArray)
}