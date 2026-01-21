package uk.gov.onelogin.sharing.security.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.assertContentEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.LogEntry
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.cbor.decoders.SessionTranscriptStub.validSessionTranscript

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
            val actual = decoder(
                VALID_ENCODED_DEVICE_ENGAGEMENT,
                MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
                logger
            )

            assertContentEquals(
                validSessionTranscript,
                actual
            )

            assert(
                "Created session transcript array from encoded device engagement and " +
                    "eReader bytes" in logger
            )
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
            LogEntry.Error(
                tag = SessionTranscriptDecoderImpl::class.java.simpleName,
                message = "Cannot derive session transcript from encoded device engagement " +
                    "and eReader bytes",
                throwable = exception
            ) in logger
        ) {
            "Cannot find expected entry in logger: $logger"
        }

        assert(
            exception.message?.startsWith(
                "CBOR parsing error: SessionEstablishment missing mandatory keys"
            ) ?: false
        )
    }
}
