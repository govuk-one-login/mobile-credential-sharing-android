package uk.gov.onelogin.sharing.security.secureArea.keypair

import org.junit.Assert.assertThrows
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.ALGORITHM
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.keyPairWithNullEntries
import uk.gov.onelogin.sharing.security.secureArea.keypair.KeyPairGeneratorStubs.keyPairWithPublicKey
import kotlin.test.Test
import kotlin.test.assertEquals

class MemorisedKeyGeneratorTest {
    private var keyPairGenerator = FakeKeyPairGenerator(
        keyPairWithNullEntries,
        keyPairWithPublicKey
    )

    private val generator by lazy {
        MemorisedKeyGenerator(keyPairGenerator)
    }

    @Test
    fun remembersInitialKeyPairOnMultipleInvocations() {
        keyPairGenerator = FakeKeyPairGenerator(keyPairWithNullEntries)
        assertEquals(
            keyPairWithNullEntries,
            performJourney()
        )

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
}
