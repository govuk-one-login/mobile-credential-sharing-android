package uk.gov.onelogin.sharing.security.cryptography.java

import java.security.MessageDigest
import uk.gov.onelogin.sharing.security.cryptography.Constants.HASH_ALGORITHM_SHA256

/**
 * Generate salt bytes via a cryptographic hashing function using SHA-256
 *
 * @return A [ByteArray] object representing the fixed length hashed bytes
 */
/*fun generateSalt(byteArray: ByteArray): ByteArray =
    MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(byteArray)*/

fun generateSalt(byteArray: ByteArray): ByteArray {
    val saltInput = saltInputTag24Bstr(byteArray)
    return MessageDigest.getInstance(HASH_ALGORITHM_SHA256).digest(saltInput)
}

private fun saltInputTag24Bstr(sessionTranscript: ByteArray): ByteArray {
    val n = sessionTranscript.size
    val out = java.io.ByteArrayOutputStream()

    // Tag(24) = 0xD8 0x18
    out.write(0xD8)
    out.write(0x18)

    // bstr header
    when {
        n < 24 -> out.write(0x40 + n)
        n <= 0xFF -> { out.write(0x58); out.write(n) }
        n <= 0xFFFF -> {
            out.write(0x59)
            out.write((n ushr 8) and 0xFF)
            out.write(n and 0xFF)
        }
        else -> {
            out.write(0x5A)
            out.write((n ushr 24) and 0xFF)
            out.write((n ushr 16) and 0xFF)
            out.write((n ushr 8) and 0xFF)
            out.write(n and 0xFF)
        }
    }

    out.write(sessionTranscript)
    return out.toByteArray()
}