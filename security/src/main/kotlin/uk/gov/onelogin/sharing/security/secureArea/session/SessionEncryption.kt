package uk.gov.onelogin.sharing.security.secureArea.session

import uk.gov.onelogin.sharing.security.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

fun interface SessionEncryption {
    fun decryptPayload(key: ByteArray, data: ByteArray, role: DeviceRole): ByteArray
}
