package uk.gov.onelogin.sharing.cryptoService.secureArea.session

import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

interface SessionEncryption {
    fun decryptPayload(
        key: ByteArray,
        data: ByteArray,
        role: DeviceRole,
        decryptCounter: UInt
    ): ByteArray

    fun encryptPayload(
        key: ByteArray,
        data: ByteArray,
        role: DeviceRole,
        encryptCounter: UInt
    ): ByteArray
}
