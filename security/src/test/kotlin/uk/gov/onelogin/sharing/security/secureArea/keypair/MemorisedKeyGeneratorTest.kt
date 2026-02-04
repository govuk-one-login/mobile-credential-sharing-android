package uk.gov.onelogin.sharing.security.secureArea.keypair

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.Assert.assertThrows
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.ALGORITHM
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.keyPairWithNullEntries
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.keyPairWithPublicKey

class MemorisedKeyGeneratorTest {
    private var keyPairGenerator = FakeKeyPairGenerator(
        keyPairWithNullEntries,
        keyPairWithPublicKey
    )

    private val logger = SystemLogger()

    private val generator by lazy {
        MemorisedKeyGenerator(keyPairGenerator, logger)
    }

    @Test
    fun remembersInitialKeyPairOnMultipleInvocations() {
        keyPairGenerator = FakeKeyPairGenerator(keyPairWithNullEntries)
        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )
        assert("Generated new session KeyPair" in logger) {
            expectedMessageNotFound(logger)
        }
        assert("Using stored session KeyPair" !in logger) {
            "Found unexpected message in logs: $logger"
        }

        assertThrows(
            "Backing implementation should've been exhausted!",
            ArrayIndexOutOfBoundsException::class.java
        ) {
            keyPairGenerator.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        }

        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )
        assert("Using stored session KeyPair" in logger) {
            expectedMessageNotFound(logger)
        }
    }

    @Test
    fun ignoresAdditionallyCreatedKeyPairsFromBackingImplementation() {
        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )

        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )
    }

    @Test
    fun resettingStateRemembersTheNextApplicableKeyPair() {
        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )

        generator.reset()
        assert("Cleared session KeyPair" in logger) {
            expectedMessageNotFound(logger)
        }

        assertEquals(
            keyPairWithPublicKey,
            performJourney()
        )

        assertThrows(
            "Backing implementation should've been exhausted!",
            ArrayIndexOutOfBoundsException::class.java
        ) {
            keyPairGenerator.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
        }

        assertEquals(
            keyPairWithPublicKey,
            performJourney()
        )
    }

    private fun performJourney() = generator.generateEcKeyPair(ALGORITHM, PARAMETER_SPEC)
    private fun expectedMessageNotFound(logger: Logger) =
        "Didn't find expected message in logs: $logger"
}
