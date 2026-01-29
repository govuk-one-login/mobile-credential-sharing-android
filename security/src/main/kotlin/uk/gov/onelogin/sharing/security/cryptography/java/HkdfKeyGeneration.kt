package uk.gov.onelogin.sharing.security.cryptography.java

import kotlinx.io.bytestring.ByteStringBuilder
import uk.gov.onelogin.sharing.security.cryptography.Constants.HKDF_KEY_SIZE

/**
 * Generate deterministic hkdf key from given initial key material, salt bytes
 * and appending information, using HMAC_SHA256
 *
 * Copy of 'com.android.identity:identity-jvm --> Crypto.hkdf
 *
 * @return A [ByteArray] object representing a deterministic session key
 */

fun hkdfKeyGeneration(ikm: ByteArray, salt: ByteArray, info: ByteArray?): ByteArray {
    val prk = mac(salt, ikm)
    val result = ByteArray(HKDF_KEY_SIZE)
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
        if (pos + digest.size < HKDF_KEY_SIZE) {
            System.arraycopy(digest, 0, result, pos, digest.size)
            pos += digest.size
            ctr++
        } else {
            System.arraycopy(digest, 0, result, pos, HKDF_KEY_SIZE - pos)
            break
        }
    }
    return result
}
