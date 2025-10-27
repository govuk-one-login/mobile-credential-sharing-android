package uk.gov.onelogin.sharing.models

import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_CIPHER_ID
import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_EDEVICE_KEY
import uk.gov.onelogin.sharing.models.MdocStubStrings.UUID
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.security.Security

/**
 * Dummy object to provide some content within the test fixtures source set.
 *
 */
object EmbeddedCborStub {
    val SECURITY = Security(
        cipherSuiteIdentifier = FAKE_CIPHER_ID,
        eDeviceKeyBytes = EmbeddedCbor(FAKE_EDEVICE_KEY.toByteArray())
    )
    val EXPECTED_PREFIX = byteArrayOf(
        0xd8.toByte(),
        EMBEDDED_CBOR_TAG.toByte(),
        0x58.toByte(),
        0x24.toByte()
    )
    val EXPECTED_BYTES = EXPECTED_PREFIX + UUID.toByteArray()
}
