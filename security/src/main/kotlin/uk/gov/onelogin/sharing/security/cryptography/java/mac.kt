package uk.gov.onelogin.sharing.security.cryptography.java

import uk.gov.onelogin.sharing.security.cryptography.Constants.MAC_ALGORITHM_ID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Return a message authentication code from a given key and message using algorithm HmacSha256
 *
 * Copy of 'com.android.identity:identity-jvm --> Crypto.mac
 *
 * @return A [ByteArray] object representing a message authentication code as bytes
 */


fun mac(
    key: ByteArray,
    message: ByteArray
): ByteArray {
    return Mac.getInstance(MAC_ALGORITHM_ID).run {
        init(SecretKeySpec(key, ""))
        update(message)
        doFinal()
    }
}
