package uk.gov.onelogin.sharing.security.cose

import kotlin.test.Test
import kotlin.test.assertEquals
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.security.SecurityDeserializerStub.embeddedCoseKey
import uk.gov.onelogin.sharing.security.SecurityDeserializerStub.validCoseKey
import uk.gov.onelogin.sharing.security.cbor.encodeCbor

class DefaultCoseKeyToStringTest {
    private val logger = SystemLogger()

    private val converter by lazy {
        DefaultCoseKeyToString(logger)
    }

    @Test
    fun `Successfully converts a CoseKey into a hexadecimal string`() {
        val actual = converter.convert(validCoseKey)

        assertEquals(
            embeddedCoseKey.encodeCbor().toHexString(),
            actual
        )

        assert("Encoded public CoseKey into EReaderKeyBytes: $actual" in logger)
    }
}
