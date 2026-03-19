package uk.gov.onelogin.sharing.cryptoService.cryptography.java

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import uk.gov.onelogin.sharing.cryptoService.cryptography.Constants.MAC_ALGORITHM_SHA256

/**
 * Return a message authentication code from a given key and message using algorithm HmacSha256
 *
 * Copy of 'com.android.identity:identity-jvm --> Crypto.mac
 *
 * @return A [ByteArray] object representing a message authentication code as bytes
 */

fun mac(key: ByteArray, message: ByteArray): ByteArray = Mac.getInstance(MAC_ALGORITHM_SHA256).run {
    init(SecretKeySpec(key, ""))
    update(message)
    doFinal()
}
