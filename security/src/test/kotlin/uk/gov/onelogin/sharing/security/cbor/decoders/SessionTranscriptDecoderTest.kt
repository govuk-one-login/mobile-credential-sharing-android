package uk.gov.onelogin.sharing.security.cbor.decoders

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import kotlin.test.assertContentEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.security.DecoderStub.validSessionTranscript
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingDataParameter
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingEReader

@RunWith(TestParameterInjector::class)
class SessionTranscriptDecoderTest {
    private val logger = SystemLogger()

    @Test
    @TestParameters(valuesProvider = SessionTranscriptDecoders::class)
    fun `Derives session transcript array from device engagement and session establishment`(
        decoder: (String, ByteArray, Logger) -> ByteArray
    ) = runTest {
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
    @TestParameters(valuesProvider = SessionTranscriptDecoders::class)
    fun `Malformed session establishment errors during session transcript derival`(
        decoder: (String, ByteArray, Logger) -> ByteArray
    ) = runTest {
        val exception = assertThrows(
            IllegalArgumentException::class.java
        ) {
            decoder(
                VALID_ENCODED_DEVICE_ENGAGEMENT,
                invalidCborMissingDataParameter.hexToByteArray(),
                logger
            )
        }

        assert(
            logger.any {
                it.message.startsWith(
                    "CBOR parsing error: SessionEstablishment missing mandatory keys"
                )
            }
        )

        assert(
            exception.message?.startsWith(
                "CBOR parsing error: SessionEstablishment missing mandatory keys"
            ) ?: false
        )
    }

    @Test
    @TestParameters(valuesProvider = SessionTranscriptDecoders::class)
    fun `Missing session establishment eReader key errors during session transcript derival`(
        decoder: (String, ByteArray, Logger) -> ByteArray
    ) = runTest {
        val exception = assertThrows(
            IllegalArgumentException::class.java
        ) {
            decoder(
                VALID_ENCODED_DEVICE_ENGAGEMENT,
                invalidCborMissingEReader.hexToByteArray(),
                logger
            )
        }

        assert(
            logger.any {
                it.message.startsWith(
                    "CBOR parsing error: SessionEstablishment missing mandatory keys"
                )
            }
        )

        assert(
            exception.message?.startsWith(
                "CBOR parsing error: SessionEstablishment missing mandatory keys"
            ) ?: false
        )
    }
}
