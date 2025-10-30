package uk.gov.onelogin.sharing.models

import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.security.Security

object SecurityTestStub {
    const val FAKE_CIPHER_ID = 1
    const val FAKE_EDEVICE_KEY = "FAKE_EDEVICE_KEY"
    const val SECURITY_EXPECTED_BASE64 = "nwFQRkFLRV9FREVWSUNFX0tFWf8="

    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance
    val SECURITY = Security(
        cipherSuiteIdentifier = FAKE_CIPHER_ID,
        eDeviceKeyBytes = EmbeddedCbor(FAKE_EDEVICE_KEY.toByteArray())
    )

    fun securityNodes(
        cipherId: Int = FAKE_CIPHER_ID,
        keyBytes: ByteArray = FAKE_EDEVICE_KEY.toByteArray()
    ): ArrayNode = jsonNodeFactory.arrayNode()
        .add(cipherId)
        .add(keyBytes)
}
