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
import uk.gov.onelogin.sharing.cryptoService.usecases.FakeDecryptDeviceResponseUseCase
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoServiceImpl.Companion.LOG_SESSION_ESTABLISHMENT_ERROR
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoServiceImpl.Companion.LOG_SESSION_ESTABLISHMENT_SUCCESS
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val fakeEncrypt = FakeEncryptDeviceRequestUseCase()
    private val fakeDecrypt = DecryptDeviceResponseUseCase { _, _, _ -> byteArrayOf() }
    private val service = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger),
        sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
        sessionKeyGenerator = HkdfSessionKeyGenerator(logger),
        encryptDeviceRequestUseCase = fakeEncrypt,
        decryptDeviceResponseUseCase = fakeDecrypt
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
            sessionKeyGenerator = HkdfSessionKeyGenerator(logger),
            encryptDeviceRequestUseCase = fakeEncrypt,
            decryptDeviceResponseUseCase = fakeDecrypt
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
            sessionKeyGenerator = HkdfSessionKeyGenerator(logger),
            encryptDeviceRequestUseCase = fakeEncrypt,
            decryptDeviceResponseUseCase = fakeDecrypt
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
            sessionKeyGenerator = failingGenerator,
            encryptDeviceRequestUseCase = fakeEncrypt,
            decryptDeviceResponseUseCase = fakeDecrypt
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
            sessionKeyGenerator = failingGenerator,
            encryptDeviceRequestUseCase = fakeEncrypt,
            decryptDeviceResponseUseCase = fakeDecrypt
        )

        assertThrows(SessionKeyDerivationException::class.java) {
            service.establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) { it }
        }

        assert("SKDevice key derivation failed" in logger)
    }

    @Test
    fun `buildDeviceRequest returns non-empty CBOR bytes`() {
        val itemsRequest = ItemsRequest(
            docType = "org.iso.18013.5.1.mDL",
            nameSpaces = mapOf("org.iso.18013.5.1" to mapOf("family_name" to true))
        )

        val result = service.buildDeviceRequest(itemsRequest)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `encryptDeviceRequest passes bytes and crypto params to encrypt`() {
        val deviceRequestBytes = byteArrayOf(0x01, 0x02)
        val skReader = ByteArray(32) { 0x01 }
        val encryptCounter = 1u

        service.encryptDeviceRequest(deviceRequestBytes, skReader, encryptCounter)

        assertEquals(deviceRequestBytes, fakeEncrypt.lastDeviceRequestBytes)
        assertEquals(skReader, fakeEncrypt.lastSkReader)
        assertEquals(encryptCounter, fakeEncrypt.lastEncryptCounter)
    }

    @Test
    fun `encryptDeviceRequest returns encrypted bytes`() {
        val encryptedResult = byteArrayOf(0xAA.toByte(), 0xBB.toByte())
        val fakeEncrypt = FakeEncryptDeviceRequestUseCase(encryptedToReturn = encryptedResult)
        val service = createService(encryptDeviceRequestUseCase = fakeEncrypt)

        val result = service.encryptDeviceRequest(byteArrayOf(0x01), ByteArray(32), 1u)

        assertEquals(encryptedResult, result)
    }

    @Test
    fun `encryptDeviceRequest propagates EncryptDeviceRequestException`() {
        val failingEncrypt = FakeEncryptDeviceRequestUseCase(
            exceptionToThrow = EncryptDeviceRequestException(
                "Error encrypting DeviceRequest",
                RuntimeException("AES failure")
            )
        )
        val service = createService(encryptDeviceRequestUseCase = failingEncrypt)

        assertThrows(EncryptDeviceRequestException::class.java) {
            service.encryptDeviceRequest(byteArrayOf(0x01), ByteArray(32), 1u)
        }
    }

    @Test
    fun `buildSessionEstablishment returns CBOR map with eReaderKey and data and logs success`() {
        val eReaderKeyTagged = service.run {
            var tagged: ByteArray? = null
            establishSession(VALID_ENCODED_DEVICE_ENGAGEMENT) {
                tagged = it.eReaderKeyTagged
                it
            }
            tagged!!
        }
        val encryptedDeviceRequest = ByteArray(48) { 0x02 }

        val result = service.buildSessionEstablishment(
            eReaderKeyBytes = eReaderKeyTagged,
            encryptedDeviceRequest = encryptedDeviceRequest
        )

        assertTrue(result.isNotEmpty())
        assertTrue(logger.any { it.message == LOG_SESSION_ESTABLISHMENT_SUCCESS })
    }

    @Test
    fun `buildSessionEstablishment logs error and throws on invalid input`() {
        assertThrows(SessionEstablishmentException::class.java) {
            service.buildSessionEstablishment(
                eReaderKeyBytes = byteArrayOf(0x00),
                encryptedDeviceRequest = ByteArray(16) { 0x02 }
            )
        }

        assertTrue(logger.any { it.message == LOG_SESSION_ESTABLISHMENT_ERROR })
    }

    @Test
    fun `decryptDeviceResponse delegates to use case and returns plaintext`() {
        val fakeDecryptUseCase = FakeDecryptDeviceResponseUseCase().apply {
            fakeDeviceResponse = byteArrayOf(0x0A, 0x0B)
        }
        val service = createService(decryptDeviceResponseUseCase = fakeDecryptUseCase)

        val deviceResponseBytes = byteArrayOf(0x01, 0x02, 0x03)
        val skDevice = ByteArray(32) { 0x04 }
        val decryptCounter = 2u

        val result = service.decryptDeviceResponse(deviceResponseBytes, skDevice, decryptCounter)

        assertEquals(fakeDecryptUseCase.fakeDeviceResponse.toList(), result.toList())
        assertEquals(deviceResponseBytes, fakeDecryptUseCase.lastDeviceResponseBytes)
        assertEquals(skDevice, fakeDecryptUseCase.lastSkDevice)
        assertEquals(decryptCounter, fakeDecryptUseCase.lastEncryptCounter)
    }

    @Test
    fun `decryptDeviceResponse propagates DecryptDeviceResponseException`() {
        val fakeDecryptUseCase = FakeDecryptDeviceResponseUseCase().apply {
            exception = DecryptDeviceResponseException("decrypt failed", RuntimeException())
        }
        val service = createService(decryptDeviceResponseUseCase = fakeDecryptUseCase)

        assertThrows(DecryptDeviceResponseException::class.java) {
            service.decryptDeviceResponse(byteArrayOf(0x01), ByteArray(32), 1u)
        }
    }

    private fun createService(
        encryptDeviceRequestUseCase: EncryptDeviceRequestUseCase = fakeEncrypt,
        decryptDeviceResponseUseCase: DecryptDeviceResponseUseCase = fakeDecrypt
    ) = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger),
        sharedSecretGenerator = EcdhSharedSecretGenerator(logger),
        sessionKeyGenerator = HkdfSessionKeyGenerator(logger),
        encryptDeviceRequestUseCase = encryptDeviceRequestUseCase,
        decryptDeviceResponseUseCase = decryptDeviceResponseUseCase
    )
}
