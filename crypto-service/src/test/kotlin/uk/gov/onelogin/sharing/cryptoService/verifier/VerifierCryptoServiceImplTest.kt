package uk.gov.onelogin.sharing.cryptoService.verifier

import java.security.InvalidKeyException
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.EcKeyPairGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.EcdhSharedSecretGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.HkdfSessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyDerivationException
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val service = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger),
        sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
        sessionKeyGenerator = HkdfSessionKeyGenerator(logger)
    )

    @Test
    fun `establishSession decorates context successfully`() = runTest {
        var context: VerifierCryptoContext? = null

        service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            context = it
            it
        }

        assertNotNull(context)
        assertEquals(VALID_ENCODED_DEVICE_ENGAGEMENT, context.engagementString)
        assertNotNull(context.serviceUuid)
        val eReaderKey = assertNotNull(context.eReaderKeyTagged)
        assertTrue(eReaderKey[0] == 0xD8.toByte())
        assertTrue(eReaderKey[1] == 0x18.toByte())
        assertNotNull(context.sessionTranscriptBytes)
        assertNotNull(context.eReaderKeyPair)
        val eDeviceKey = assertNotNull(context.eDevicePublicKey)
        val expectedKey = validDeviceEngagementDto.security.ephemeralPublicKey
        assertEquals(
            expectedKey.x.toList(),
            eDeviceKey.w.affineX.toByteArray().takeLast(32).map { it }
        )
        assert("SessionTranscriptBytes constructed successfully" in logger)
    }

    @Test
    fun `establishSession throws when DeviceEngagementBytes is blank`() = runTest {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.establishSession("") { it }
        }

        assertEquals("DeviceEngagementBytes must not be blank", exception.message)
        assert(
            "error constructing SessionTranscript array due to " +
                "DeviceEngagementBytes is blank" in logger
        )
    }

    @Test
    fun `shared secret computed successfully`() = runTest {
        service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }

        assert("Shared secret computed successfully" in logger)
    }

    @Test
    fun `incompatible curve logs error and throws`() {
        val p384KeyPair = KeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp384r1"))
        }.generateKeyPair()

        val service = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = { _, _ -> p384KeyPair },
            sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
            sessionKeyGenerator = HkdfSessionKeyGenerator(logger)
        )

        assertThrows(SharedSecretException.IncompatibleCurve::class.java) {
            service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }
        }

        assert(
            logger.any {
                it.message.contains(
                    "Error computing shared secret due to EDeviceKey.Pub with incompatible curve"
                )
            }
        )
    }

    @Test
    fun `malformed EDeviceKey logs error and throws`() {
        val service = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = EcKeyPairGenerator(logger),
            sharedSecretGenerator = { _, _ ->
                throw InvalidKeyException("malformed key")
            },
            sessionKeyGenerator = HkdfSessionKeyGenerator(logger)
        )

        assertThrows(SharedSecretException.MalformedKey::class.java) {
            service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }
        }

        assert(
            logger.any {
                it.message.contains(
                    "Error computing shared secret due to malformed EDeviceKey.Pub"
                )
            }
        )
    }

    @Test
    fun `salt calculated from SessionTranscriptBytes`() = runTest {
        var context: VerifierCryptoContext? = null

        service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            context = it
            it
        }

        assertNotNull(context!!.sessionTranscriptBytes)
    }

    @Test
    fun `SKReader key derived successfully`() = runTest {
        var context: VerifierCryptoContext? = null

        service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            context = it
            it
        }

        val skReader = assertNotNull(context!!.skReader)
        assertEquals(32, skReader.size)
        assert("SKReader key generated" in logger)
    }

    @Test
    fun `SKDevice key derived and distinct from SKReader`() = runTest {
        var context: VerifierCryptoContext? = null

        service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            context = it
            it
        }

        val skReader = assertNotNull(context!!.skReader)
        val skDevice = assertNotNull(context.skDevice)
        assertEquals(32, skDevice.size)
        assertNotEquals(skReader.toList(), skDevice.toList())
        assert("SKDevice key generated" in logger)
    }

    @Test
    fun `SKReader derivation failure logs and throws`() {
        val failingGenerator = SessionKeyGenerator { _, _, role ->
            if (role == DeviceRole.VERIFIER) {
                throw SessionKeyDerivationException("SKReader error", RuntimeException())
            }
            byteArrayOf()
        }
        val service = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = EcKeyPairGenerator(logger),
            sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
            sessionKeyGenerator = failingGenerator
        )

        assertThrows(SessionKeyDerivationException::class.java) {
            service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }
        }

        assert("SKReader key derivation failed" in logger)
    }

    @Test
    fun `SKDevice derivation failure logs and throws`() {
        val failingGenerator = SessionKeyGenerator { _, _, role ->
            if (role == DeviceRole.HOLDER) {
                throw SessionKeyDerivationException("SKDevice error", RuntimeException())
            }
            byteArrayOf()
        }
        val service = VerifierCryptoServiceImpl(
            logger = logger,
            keyPairGenerator = EcKeyPairGenerator(logger),
            sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
            sessionKeyGenerator = failingGenerator
        )

        assertThrows(SessionKeyDerivationException::class.java) {
            service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }
        }

        assert("SKDevice key derivation failed" in logger)
    }
}
