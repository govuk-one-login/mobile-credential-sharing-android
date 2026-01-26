package uk.gov.onelogin.sharing.security.cryptography.java

import kotlinx.io.bytestring.ByteStringBuilder

/**
 * Generate deterministic hkdf key from given initial key material, salt bytes and appending information, using HMAC_SHA256
 *
 * Copy of 'com.android.identity:identity-jvm --> Crypto.hkdf
 *
 * @return A [ByteArray] object representing a deterministic session key
 */

fun hkdfKeyGeneration(
    ikm: ByteArray,
    salt: ByteArray?,
    info: ByteArray?,
): ByteArray {
    val size = 32
    val macLength = 32
    val key = if (salt == null || salt.isEmpty()) {
        ByteArray(macLength)
    } else {
        salt
    }
    val prk = mac(key, ikm)
    val result = ByteArray(size)
    var ctr = 1
    var pos = 0
    var digest = ByteArray(0)
    while (true) {
        val bsb = ByteStringBuilder()
        bsb.append(digest, 0, digest.size)
        if (info != null) {
            bsb.append(info, 0, info.size)
        }
        bsb.append(ctr.toByte())
        digest = mac(prk, bsb.toByteString().toByteArray())
        if (pos + digest.size < size) {
            System.arraycopy(digest, 0, result, pos, digest.size)
            pos += digest.size
            ctr++
        } else {
            System.arraycopy(digest, 0, result, pos, size - pos)
            break
        }
    }
    return result
}