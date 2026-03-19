package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

fun interface SessionEncryption {
    fun decryptPayload(
        key: ByteArray,
        data: ByteArray,
        role: DeviceRole,
        decryptCounter: UInt
    ): ByteArray
}
