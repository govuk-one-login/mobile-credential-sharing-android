package uk.gov.onelogin.sharing.models.mdoc.cose

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.security.KeyPairGenerator
import java.security.Security
import java.security.spec.ECGenParameterSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cose.ECKeyType.EC
import uk.gov.onelogin.sharing.models.mdoc.cose.ECKeyType.OKP
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.BRAINPOOL_P256
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.BRAINPOOL_P320
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.BRAINPOOL_P384
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.BRAINPOOL_P512
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.Ed25519
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.Ed448
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.P256
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.P384
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.P521
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.X25519
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType.X448

@RunWith(TestParameterInjector::class)
class ECTypeTest {

    @Test
    fun `There are a finite number of defined ECTypes`() {
        assertEquals(
            11,
            ECType.entries.size
        )
    }

    @Test
    fun `Has an elliptic curve key type`(
        @TestParameter inputs: Pair<ECType, ECKeyType> = testValues(
            P256 to EC,
            P384 to EC,
            P521 to EC,
            X25519 to OKP,
            X448 to OKP,
            Ed25519 to OKP,
            Ed448 to OKP,
            BRAINPOOL_P256 to EC,
            BRAINPOOL_P320 to EC,
            BRAINPOOL_P384 to EC,
            BRAINPOOL_P512 to EC
        )
    ) {
        val (type, expectedKeyType) = inputs

        assertEquals(
            expectedKeyType,
            type.expectedKeyType
        )
    }

    @Test
    fun `Has an ID associated with the curve for COSE Key generation`(
        @TestParameter inputs: Pair<ECType, UInt> = testValues(
            P256 to 1u,
            P384 to 2u,
            P521 to 3u,
            X25519 to 4u,
            X448 to 5u,
            Ed25519 to 6u,
            Ed448 to 7u,
            BRAINPOOL_P256 to 256u,
            BRAINPOOL_P320 to 257u,
            BRAINPOOL_P384 to 258u,
            BRAINPOOL_P512 to 259u
        )
    ) {
        val (type, expectedCurveId) = inputs

        assertEquals(
            expectedCurveId,
            type.curveId
        )
    }

    @Test
    fun `Has an expected coordinate byte length`(
        @TestParameter inputs: Pair<ECType, Int> = testValues(
            P256 to 32,
            P384 to 48,
            P521 to 65,
            X25519 to 32,
            X448 to 56,
            Ed25519 to 32,
            Ed448 to 56,
            BRAINPOOL_P256 to 32,
            BRAINPOOL_P320 to 40,
            BRAINPOOL_P384 to 48,
            BRAINPOOL_P512 to 64
        )
    ) {
        val (type, expectedCoordinateByteLength) = inputs
        assertEquals(
            expectedCoordinateByteLength,
            type.expectedCoordinateByteLength
        )
    }

    @Test
    fun `Can generate an EC key pair using type properties`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == EC }
        )
    ) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())

        val keyPairGenerator = KeyPairGenerator.getInstance(
            type.expectedKeyType.name,
            BouncyCastleProvider.PROVIDER_NAME
        )

        keyPairGenerator.initialize(ECGenParameterSpec(type.parameterSpecName))

        assertNotNull(
            keyPairGenerator.generateKeyPair()
        )
    }
}
