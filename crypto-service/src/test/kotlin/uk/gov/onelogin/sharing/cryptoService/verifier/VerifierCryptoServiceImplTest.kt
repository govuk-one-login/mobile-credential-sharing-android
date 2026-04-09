package uk.gov.onelogin.sharing.cryptoService.verifier

import java.security.KeyPairGenerator as JKeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.EcKeyPairGenerator
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.KeyPairGeneratorStubs.validKeyPair
import uk.gov.onelogin.sharing.cryptoService.secureArea.secret.EcdhSharedSecretGenerator

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val service = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger),
        sharedSecretGenerator = EcdhSharedSecretGenerator(logger)
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
    fun `AC1 - computeSharedSecret succeeds with valid P-256 keys`() {
        val context = buildContextFromEngagement()

        val sharedSecret = service.computeSharedSecret(context)

        assertNotNull(sharedSecret)
        assertTrue(sharedSecret.isNotEmpty())
        assert("Shared secret computed successfully" in logger)
    }

    @Test
    fun `AC2 - computeSharedSecret throws IncompatibleCurve when EDeviceKey uses wrong curve`() {
        val p384KeyPair = JKeyPairGenerator.getInstance("EC").apply {
            initialize(ECGenParameterSpec("secp384r1"))
        }.generateKeyPair()

        val context = VerifierCryptoContext(
            eReaderKeyPair = validKeyPair,
            eDevicePublicKey = p384KeyPair.public as ECPublicKey
        )

        assertThrows(SharedSecretException.IncompatibleCurve::class.java) {
            service.computeSharedSecret(context)
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
    fun `AC3 - computeSharedSecret throws when EDeviceKey is not available`() {
        val context = VerifierCryptoContext(
            eReaderKeyPair = validKeyPair,
            eDevicePublicKey = null
        )

        val exception = assertThrows(IllegalStateException::class.java) {
            service.computeSharedSecret(context)
        }
        assertEquals("EDeviceKey.Pub not available", exception.message)
    }

    private fun buildContextFromEngagement(): VerifierCryptoContext {
        var context: VerifierCryptoContext? = null
        service.processEngagement(VALID_ENCODED_DEVICE_ENGAGEMENT) {
            context = it
            it
        }
        return context!!
    }
}
