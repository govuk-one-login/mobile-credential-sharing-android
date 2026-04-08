package uk.gov.onelogin.sharing.cryptoService.verifier

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.SessionTranscriptStub.validSessionTranscript
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.SHARED_SECRET_BYTES
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_SK_DEVICE_KEY
import uk.gov.onelogin.sharing.cryptoService.cryptography.java.CryptoStub.VALID_SK_READER_KEY
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.EcKeyPairGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.HkdfSessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val sessionKeyGenerator = HkdfSessionKeyGenerator(logger)
    private val service = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger),
        sessionKeyGenerator = sessionKeyGenerator
    )

    @Test
    fun `processEngagement decorates context successfully`() = runTest {
        var decoratedContext: VerifierCryptoContext? = null

        service.processEngagement(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            decoratedContext = it
            it
        }

        val context = assertNotNull(decoratedContext)
        assertEquals(VALID_ENCODED_DEVICE_ENGAGEMENT, context.engagementString)
        assertNotNull(context.serviceUuid)
        val eReaderKey = assertNotNull(context.eReaderKeyTagged)
        assertTrue(eReaderKey[0] == 0xD8.toByte())
        assertTrue(eReaderKey[1] == 0x18.toByte())
        assertNotNull(context.sessionTranscriptBytes)
        assert("SessionTranscriptBytes constructed successfully" in logger)
    }

    @Test
    fun `processEngagement throws when DeviceEngagementBytes is blank`() = runTest {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.processEngagement("") { it }
        }

        assertEquals("DeviceEngagementBytes must not be blank", exception.message)
        assert(
            "error constructing SessionTranscript array due to " +
                "DeviceEngagementBytes is blank" in logger
        )
    }

    @Test
    fun `deriveSessionKeys generates correct SKReader key`() {
        val (skReader, _) = service.deriveSessionKeys(
            SHARED_SECRET_BYTES,
            validSessionTranscript
        )

        assertEquals(VALID_SK_READER_KEY, skReader.toHexString())
        assert("SKReader key generated" in logger)
    }

    @Test
    fun `deriveSessionKeys generates correct SKDevice key`() {
        val (_, skDevice) = service.deriveSessionKeys(
            SHARED_SECRET_BYTES,
            validSessionTranscript
        )

        assertEquals(VALID_SK_DEVICE_KEY, skDevice.toHexString())
        assert("SKDevice key generated" in logger)
    }

    @Test
    fun `deriveSessionKeys produces distinct SKReader and SKDevice keys`() {
        val (skReader, skDevice) = service.deriveSessionKeys(
            SHARED_SECRET_BYTES,
            validSessionTranscript
        )

        assertNotEquals(skReader.toHexString(), skDevice.toHexString())
    }

    @Test
    fun `deriveSessionKeys produces wrong keys when sessionTranscriptBytes differ`() {
        val alteredTranscript = validSessionTranscript.copyOf().apply { set(0, 0x00) }

        val (skReader, skDevice) = service.deriveSessionKeys(
            SHARED_SECRET_BYTES,
            alteredTranscript
        )

        assertNotEquals(VALID_SK_READER_KEY, skReader.toHexString())
        assertNotEquals(VALID_SK_DEVICE_KEY, skDevice.toHexString())
    }

    @Test
    fun `deriveSessionKeys produces wrong keys when shared secret differs`() {
        val wrongSecret = ByteArray(32) { 0xFF.toByte() }

        val (skReader, skDevice) = service.deriveSessionKeys(
            wrongSecret,
            validSessionTranscript
        )

        assertNotEquals(VALID_SK_READER_KEY, skReader.toHexString())
        assertNotEquals(VALID_SK_DEVICE_KEY, skDevice.toHexString())
    }

    @Test
    fun `deriveSessionKeys logs failure when SKReader derivation fails`() {
        val failingGenerator = SessionKeyGenerator { _, _, role ->
            if (role == DeviceRole.VERIFIER) {
                throw SessionKeyDerivationException("SKReader error", RuntimeException())
            }
            byteArrayOf()
        }
        val failingService = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = EcKeyPairGenerator(logger),
            sessionKeyGenerator = failingGenerator
        )

        assertThrows(SessionKeyDerivationException::class.java) {
            failingService.deriveSessionKeys(SHARED_SECRET_BYTES, validSessionTranscript)
        }

        assert("SKReader key derivation failed" in logger)
    }

    @Test
    fun `deriveSessionKeys logs failure when SKDevice derivation fails`() {
        val failingGenerator = SessionKeyGenerator { _, _, role ->
            if (role == DeviceRole.HOLDER) {
                throw SessionKeyDerivationException("SKDevice error", RuntimeException())
            }
            byteArrayOf()
        }
        val failingService = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = EcKeyPairGenerator(logger),
            sessionKeyGenerator = failingGenerator
        )

        assertThrows(SessionKeyDerivationException::class.java) {
            failingService.deriveSessionKeys(SHARED_SECRET_BYTES, validSessionTranscript)
        }

        assert("SKDevice key derivation failed" in logger)
    }
}
