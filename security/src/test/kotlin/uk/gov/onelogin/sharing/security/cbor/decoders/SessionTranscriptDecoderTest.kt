package uk.gov.onelogin.sharing.security.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.math.exp
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.LogEntry
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_TRANSCRIPT
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_E_READER_KEY
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.base64Decode
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.cbor.deriveUntaggedCbor
import uk.gov.onelogin.sharing.security.cbor.encodeCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

@RunWith(TestParameterInjector::class)
class SessionTranscriptDecoderTest
@TestParameters(valuesProvider = SessionTranscriptDecoders::class)
constructor(
    private val decoder: (String, ByteArray, Logger) -> ByteArray
) {
    private val logger = SystemLogger()

    @Test
    fun `Derives session transcript array from device engagement and session establishment`() =
        runTest {
            val eReaderKeyTagged = MOCK_E_READER_KEY.hexToByteArray()

            val actual = decoder(
                VALID_ENCODED_DEVICE_ENGAGEMENT,
                eReaderKeyTagged,
                logger
            )

            assertEquals(VALID_TRANSCRIPT, actual.toHexString())

            assert(
                "Successfully derived session transcript from encoded device engagement and " +
                    "eReader bytes" in logger
            )
        }

    @Test
    @TestParameters(valuesProvider = InvalidSessionEstablishments::class)
    fun `Invalid session establishments cause errors during decoding`(
        sessionEstablishmentBytes: ByteArray
    ) = runTest {
        val exception = assertThrows(
            IllegalArgumentException::class.java
        ) {
            decoder(
                VALID_ENCODED_DEVICE_ENGAGEMENT,
                sessionEstablishmentBytes,
                logger
            )
        }

        assert(
            "Cannot derive session transcript from encoded device engagement " +
                "and eReader bytes"
                in logger
        ) {
            "Cannot find expected entry in logger: $logger"
        }

        assertEquals(
            "CBOR parsing error: eReaderKey must be tag(24)",
            exception.message
        )
    }
}
