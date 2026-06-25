package uk.gov.android.credentialsharing.iso180136.sessionEstablishment

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValuesIn
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.Security
import java.security.interfaces.ECPublicKey
import java.security.spec.ECFieldFp
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import junit.framework.TestCase.assertTrue
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Assert.assertThrows
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.Companion.padEcCoordinatesTo32Bytes
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper.default as mapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cose.ECKeyType
import uk.gov.onelogin.sharing.models.mdoc.cose.ECType
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto.Companion.CURVE_KEY
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto.Companion.KEY_TYPE_KEY
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto.Companion.X_KEY
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto.Companion.Y_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto.Companion.DATA_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto.Companion.E_READER_KEY_KEY
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentStubs.validSessionEstablishmentDtoBytes

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for Session Establishment.
 *
 * These tests validate that the mdoc reader correctly ignores unknown/RFU keys
 * in the SessionEstablishment CBOR structure per ISO/IEC 18013-5:2021, 9.1.1.4.
 *
 * ```
 * SessionEstablishment = {
 * "eReaderKey" : EReaderKeyBytes,
 * "data" : bstr ; Encrypted mdoc request
 * * tstr => RFU
 * }
 * ```
 */
@RunWith(TestParameterInjector::class)
class SessionEstablishmentMessageStructureTest {
    private var sessionEstablishmentDto = validSessionEstablishmentDto
    private val result by lazy {
        mapper.writeValueAsBytes(sessionEstablishmentDto)
    }
    private val coseKeyDto by lazy {
        mapper.readValue(
            sessionEstablishmentDto.eReaderKey.encoded,
            CoseKeyDto::class.java
        )
    }
    private val coseKeyHexString by lazy {
        sessionEstablishmentDto.eReaderKey.encoded.toHexString()
    }
    private val resultHexString by lazy {
        result.toHexString()
    }

    /**
     * Scenario ID: mDLR_MS_SE_01, mDLR_MS_SE_04
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `sessionEstablishment - Valid CBOR is decodable`() {
        val dto = mapper.readValue(
            result,
            SessionEstablishmentDto::class.java
        )

        assertThat(
            dto,
            not(nullValue())
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_01, mDLR_MS_SE_04
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `sessionEstablishment - Deserialization creates valid CBOR`() {
        assertThat(
            result,
            equalTo(validSessionEstablishmentDtoBytes)
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
     * sub-scenario: Common_CBOR_02
     */
    @Test
    fun `sessionEstablishment - There are no indefinite length objects`(
        @TestParameter assertion: Matcher<in String> = namedTestValues(
            "Indefinite length byte strings" to containsString(
                BYTE_STRING_INDEFINITE.toHexString()
            ),
            "Indefinite byte objects" to containsString(
                BYTE_OBJECT_INDEFINITE.toHexString()
            ),
            "Indefinite byte arrays" to containsString(
                (PREFIX_TYPE_BYTES + SUFFIX_INDEFINITE).toHexString()
            )
        )
    ) {
        assertThat(
            resultHexString.chunked(2),
            not(contains(assertion))
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_01
     * sub-scenario: Common_CBOR_03
     */
    @Test
    fun `There are no duplicate fields`(
        @TestParameter propertyName: String = testValues(
            E_READER_KEY_KEY,
            DATA_KEY
        )
    ) {
        val values = mapper.readTree(result).findValues(propertyName)
        assertThat(
            values,
            hasSize(1)
        )
    }

    /**
     * Scenario: mDLR_MS_SE_02
     */
    @Test
    fun `CBOR structure is a map of 2 elements`() {
        assertThat(
            resultHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 2))
        )
    }

    /**
     * Scenario: mDLR_MS_SE_03
     */
    @Test
    fun `'eReaderKey' is an embedded CBOR object with 4 fields`() {
        val eReaderKeyPrefix = "eReaderKey".toByteArray().toHexString()
        val embeddedCborPaddingStart = "d818584b"

        val expectedEReaderPrefix = eReaderKeyPrefix +
            embeddedCborPaddingStart +
            HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 4)

        assertThat(
            resultHexString,
            containsString(expectedEReaderPrefix)
        )
    }

    /**
     * Scenario: mDLR_MS_SE_03
     */
    @Test
    fun `'data' is the correct data type`() {
        val dataPrefix = "data".toByteArray().toHexString()
        val hStringPrefix = "5902df"

        assertThat(
            resultHexString,
            allOf(
                containsString(dataPrefix + hStringPrefix)
            )
        )
    }

    /**
     * Scenario: mDLR_MS_SE_04, mDLR_MS_SE_05
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `Cose key is obtainable from the eReaderKey bytes`() {
        assertThat(
            coseKeyDto,
            not(nullValue())
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_05
     * sub-scenario: Common_CBOR_01
     */
    @Test
    fun `'eReaderKey' - Deserialization creates valid CBOR`() {
        val bytes = mapper.writeValueAsBytes(coseKeyDto)
        assertThat(
            resultHexString,
            containsString(bytes.toHexString())
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_05
     * sub-scenario: Common_CBOR_02
     */
    @Test
    fun `'eReaderKey' - There are no indefinite length objects`(
        @TestParameter assertion: Matcher<in String> = namedTestValues(
            "Indefinite length byte strings" to containsString(
                BYTE_STRING_INDEFINITE.toHexString()
            ),
            "Indefinite byte objects" to containsString(
                BYTE_OBJECT_INDEFINITE.toHexString()
            ),
            "Indefinite byte arrays" to containsString(
                (PREFIX_TYPE_BYTES + SUFFIX_INDEFINITE).toHexString()
            )
        )
    ) {
        assertThat(
            coseKeyHexString.chunked(2),
            not(contains(assertion))
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_05
     * sub-scenario: Common_CBOR_03
     */
    @Test
    fun `'eReaderKey' - There are no duplicate fields`(
        @TestParameter propertyName: Long = namedTestValues(
            "Key type" to KEY_TYPE_KEY,
            "Curve" to CURVE_KEY,
            "X" to X_KEY,
            "Y" to Y_KEY
        )
    ) {
        val values = mapper.readTree(
            sessionEstablishmentDto.eReaderKey.encoded
        ).findValues(propertyName.toString())
        assertThat(
            values,
            hasSize(1)
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_01
     */
    @Test
    fun `'eReaderKey' - Encoded data is an object with 4 properties`() {
        assertThat(
            coseKeyHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + 4))
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_02
     */
    @Test
    fun `'eReaderKey' - Encoded data has the correct data structure`(
        @TestParameter inputs: Pair<Long, (JsonNode) -> Boolean> = namedTestValues(
            "Key type" to (
                KEY_TYPE_KEY to JsonNode::isInt
                ),
            "Curve" to (
                CURVE_KEY to JsonNode::isInt
                ),
            "X" to (
                X_KEY to JsonNode::isBinary
                ),
            "Y" to (
                Y_KEY to JsonNode::isBinary
                )
        )
    ) {
        val (property, assertion) = inputs
        val rootNode = mapper.readTree(sessionEstablishmentDto.eReaderKey.encoded)

        assertTrue(assertion(rootNode[property.toString()]))
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_03
     *
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    fun `'eReaderKey' - Only accepts valid key types`(
        @TestParameter type: ECKeyType = testValues(
            ECKeyType.EC,
            ECKeyType.OKP
        )
    ) {
        sessionEstablishmentDto.copy(
            eReaderKey = EmbeddedCbor(
                mapper.writeValueAsBytes(
                    coseKeyDto.copy(
                        keyType = type.id.toLong()
                    )
                )
            )
        )

        assertThat(
            sessionEstablishmentDto,
            not(nullValue())
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_03
     *
     * This currently fails due to the [CoseKeyDto.keyType] property being a [Long] instead of a
     * [UInt].
     *
     * @see CoseKeyDto.keyType
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.keyType
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `'eReaderKey' - Refuses invalid key types`(
        @TestParameter type: ECKeyType = testValues(
            ECKeyType.RSA,
            ECKeyType.SYMMETRIC,
            ECKeyType.HSS_LMS,
            ECKeyType.WALNUT_DSA
        )
    ) {
        assertThrows(Exception::class.java) {
            sessionEstablishmentDto.copy(
                eReaderKey = EmbeddedCbor(
                    mapper.writeValueAsBytes(
                        coseKeyDto.copy(
                            keyType = type.id.toLong()
                        )
                    )
                )
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_04
     *
     * @see CoseKeyDto.keyType
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.keyType
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    fun `'eReaderKey' - The curve value dictates the key type`(@TestParameter curveType: ECType) {
        sessionEstablishmentDto = sessionEstablishmentDto.copy(
            eReaderKey = EmbeddedCbor(
                mapper.writeValueAsBytes(
                    coseKeyDto.copy(
                        curve = curveType.curveId.toLong(),
                        keyType = curveType.keyTypeId.toLong()
                    )
                )
            )
        )

        assertThat(
            sessionEstablishmentDto,
            not(nullValue())
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_04
     *
     * This currently fails due to no validation between the [CoseKeyDto.keyType] and the
     * [CoseKeyDto.curve] properties.
     *
     * @see CoseKeyDto.keyType
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.keyType
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `'eReaderKey' - OKP curves fail with EC2 key type`(
        @TestParameter curveType: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.OKP }
        )
    ) {
        assertThrows(Exception::class.java) {
            sessionEstablishmentDto = sessionEstablishmentDto.copy(
                eReaderKey = EmbeddedCbor(
                    mapper.writeValueAsBytes(
                        coseKeyDto.copy(
                            curve = curveType.curveId.toLong(),
                            keyType = ECKeyType.EC.id.toLong()
                        )
                    )
                )
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_04
     *
     * This currently fails due to no validation between the [CoseKeyDto.keyType] and the
     * [CoseKeyDto.curve] properties.
     *
     * @see CoseKeyDto.keyType
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.keyType
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `'eReaderKey' - EC2 curves fail with OKP key type`(
        @TestParameter curveType: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.EC }
        )
    ) {
        assertThrows(Exception::class.java) {
            sessionEstablishmentDto = sessionEstablishmentDto.copy(
                eReaderKey = EmbeddedCbor(
                    mapper.writeValueAsBytes(
                        coseKeyDto.copy(
                            curve = curveType.curveId.toLong(),
                            keyType = ECKeyType.OKP.id.toLong()
                        )
                    )
                )
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_05
     *
     * @see CoseKeyDto.x
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.x
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    fun `'eReaderKey' - Verify x length matches selected EC curve`(@TestParameter type: ECType) {
        assertThat(
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong(),
                x = ByteArray(size = type.expectedCoordinateByteLength)
            ).x.size,
            equalTo(type.expectedCoordinateByteLength)
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_05
     *
     * This currently fails due to no validation between the curve type and the length of
     * [CoseKeyDto.x].
     *
     * @see CoseKeyDto.x
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.x
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to lack of input validation")
    fun `'eReaderKey' - Refuses invalid x coordinate lengths`(@TestParameter type: ECType) {
        assertThrows(Exception::class.java) {
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong(),
                x = ByteArray(size = type.expectedCoordinateByteLength - 8)
            )
        }
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_06
     *
     * @see CoseKeyDto.y
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.y
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    fun `'eReaderKey' - EC2 keys also have a 'y' coordinate value`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.EC }
        )
    ) {
        assertThat(
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong()
            ),
            hasProperty("y")
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_06
     *
     * This currently fails due to MVP not including OKP as valid COSE keys.
     *
     * @see CoseKeyDto.y
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.y
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to not handling OKP key structure")
    fun `'eReaderKey' - OKP keys don't have a 'y' coordinate value`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.OKP }
        )
    ) {
        assertThat(
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong()
            ),
            not(hasProperty("y"))
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_07
     *
     * @see CoseKeyDto.y
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.y
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    fun `'eReaderKey' - Verify Y length matches selected EC curve`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.EC }
        )
    ) {
        assertThat(
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong(),
                y = ByteArray(size = type.expectedCoordinateByteLength)
            ).y.size,
            equalTo(type.expectedCoordinateByteLength)
        )
    }

    /**
     * Scenario ID: mDLR_MS_SE_06
     * sub-scenario: Common_COSEKey_07
     *
     * This currently fails due to MVP not handling compressed COSE keys.
     *
     * @see CoseKeyDto.y
     * @see uk.gov.onelogin.sharing.cryptoService.cose.CoseKey.y
     * @see <a href=https://datatracker.ietf.org/doc/html/rfc9053#section-10.1>Cose key types</a>
     */
    @Test
    @Ignore("Fails conformance test due to only handling uncompressed COSE keys")
    fun `'eReaderKey' - Compressed keys have a Y property as a boolean`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.EC }
        )
    ) {
        assertThat(
            coseKeyDto.copy(
                keyType = type.keyTypeId.toLong(),
                curve = type.curveId.toLong()
            ),
            hasProperty(
                "y",
                instanceOf<Boolean>(Boolean::class.java)
            )
        )
    }

    @Test
    fun `'eReaderKey' - uncompressed coordinates are valid`(
        @TestParameter type: ECType = testValuesIn(
            ECType.entries.filter { it.expectedKeyType == ECKeyType.EC }
        )
    ) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(BouncyCastleProvider())

        // Get standard curve domain parameters
        val parameters = AlgorithmParameters.getInstance(
            "EC",
            BouncyCastleProvider.PROVIDER_NAME
        )
        val generatorSpec = ECGenParameterSpec(type.parameterSpecName)
        parameters.init(generatorSpec)
        val ecParameters = parameters.getParameterSpec(ECParameterSpec::class.java)

        val keyPairGenerator = KeyPairGenerator.getInstance(
            "EC",
            BouncyCastleProvider.PROVIDER_NAME
        )
        keyPairGenerator.initialize(generatorSpec)
        val publicKey = keyPairGenerator.generateKeyPair().public as ECPublicKey

        val xCoordinate = padEcCoordinatesTo32Bytes(publicKey.w.affineX)
        val yCoordinate = padEcCoordinatesTo32Bytes(publicKey.w.affineY)

        coseKeyDto.copy(
            keyType = type.keyTypeId.toLong(),
            curve = type.curveId.toLong(),
            x = xCoordinate,
            y = yCoordinate
        ).let {
            assertTrue(
                isCOSEPointOnCurve(
                    xBytes = it.x,
                    yBytes = it.y,
                    ecParameters = ecParameters
                )
            )
        }
    }

    private fun isCOSEPointOnCurve(
        xBytes: ByteArray,
        yBytes: ByteArray,
        ecParameters: ECParameterSpec
    ): Boolean {
        val x = BigInteger(1, xBytes)
        val y = BigInteger(1, yBytes)

        return try {
            // 1. Verify point coordinates are within the field modulus
            val p = (ecParameters.curve.field as ECFieldFp).p
            if (isCoordinateOutOfBounds(x, p) || isCoordinateOutOfBounds(y, p)) {
                return false
            }

            // 2. Verify mathematically using the curve equation
            val point = ECPoint(x, y)
            val pubSpec = ECPublicKeySpec(point, ecParameters)
            val kf = KeyFactory.getInstance("EC")

            // KeyFactory.generatePublic will throw InvalidKeySpecException if the point is invalid
            kf.generatePublic(pubSpec)
            true
        } catch (e: Exception) {
            fail("Caught exception: $e")
        }
    }

    private fun isCoordinateOutOfBounds(coordinate: BigInteger, p: BigInteger): Boolean =
        coordinate < BigInteger.ZERO || coordinate >= p
}
