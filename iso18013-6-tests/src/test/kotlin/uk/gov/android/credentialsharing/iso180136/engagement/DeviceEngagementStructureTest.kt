package uk.gov.android.credentialsharing.iso180136.engagement

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.cbor.CBORConstants
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_OBJECT_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.BYTE_STRING_INDEFINITE
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORConstants.SUFFIX_INDEFINITE
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.Test
import kotlin.test.assertTrue
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper.default as mapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementCborBuilder
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementCborBuilder.VERSION
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementDto

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for the DeviceEngagement structure (mdoc side).
 *
 * These tests exercise the raw CBOR encoding produced for a DeviceEngagement structure,
 * verifying the CBOR major types, additional-information bits, tag values and item ordering
 * mandated by ISO/IEC 18013-5:2021, 8.2.1.1 and RFC 7049 (RFC 8949) section 2.1.
 *
 * ```
 * DeviceEngagement = {
 *   0 : tstr,                    ; Version
 *   1 : Security,                ; Security
 *   ? 2 : DeviceRetrievalMethods,; DeviceRetrievalMethods
 *   ? 3 : ServerRetrievalMethods,; ServerRetrievalMethods
 *   * int => RFU
 * }
 *
 * Security = [
 *   int,                         ; Cipher suite identifier
 *   EDeviceKeyBytes              ; #6.24(bstr .cbor COSE_Key)
 * ]
 *
 * DeviceRetrievalMethods = [ + DeviceRetrievalMethod ]
 *
 * DeviceRetrievalMethod = [
 *   uint,                        ; Type
 *   uint,                        ; Version
 *   RetrievalOptions             ; map
 * ]
 * ```
 *
 * The valid engagement bytes are produced by [DeviceEngagementCborBuilder], the same fixture used
 * by the existing mdoc-reader tests, so no existing source or test files are modified.
 */
@RunWith(TestParameterInjector::class)
class DeviceEngagementStructureTest {

    private val deviceEngagementBytes = DeviceEngagementCborBuilder.build()

    private val deviceEngagementHexString = deviceEngagementBytes.toHexString()

    private val rootNode: JsonNode = mapper.readTree(deviceEngagementBytes)

    /**
     * The [DeviceEngagementDto] deserializer models the Security array as an object with two
     * elements at indices "0" (cipher suite identifier) and "1" (EDeviceKeyBytes).
     */
    private val securityNode: JsonNode = rootNode.get(SECURITY_KEY)

    private val deviceRetrievalMethodsNode: JsonNode = rootNode.get(DEVICE_RETRIEVAL_METHODS_KEY)

    /**
     * Scenario ID: mDL_MS_DE_Gen_01
     *
     * "This test case validates the CBOR structure, canonicalization rules and uniqueness of
     * pairs of the DeviceEngagement CBOR structure." Verifies the DeviceEngagement passes the
     * Common_CBOR test cases: it is decodable, contains no indefinite-length items and has no
     * duplicate keys.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDL_MS_DE_Gen_01 - Common_CBOR - valid CBOR is decodable`() {
        val dto = mapper.readValue(deviceEngagementBytes, DeviceEngagementDto::class.java)

        assertThat(dto, not(nullValue()))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_01
     * sub-scenario: Common_CBOR - no indefinite length items (canonicalization).
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDL_MS_DE_Gen_01 - Common_CBOR - there are no indefinite length objects`(
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
            deviceEngagementHexString.chunked(2),
            not(Matchers.contains(assertion))
        )
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_01
     * sub-scenario: Common_CBOR - key-value pairs are unique.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDL_MS_DE_Gen_01 - Common_CBOR - there are no duplicate keys`(
        @TestParameter key: String = testValues(
            VERSION_KEY,
            SECURITY_KEY,
            DEVICE_RETRIEVAL_METHODS_KEY
        )
    ) {
        // Count occurrences of the key among the top-level map's direct field names only
        // (findValues recurses into nested maps such as the COSE_Key and BLE options).
        val occurrences = rootNode.fieldNames().asSequence().count { it == key }

        assertThat(occurrences, equalTo(1))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_02
     *
     * "Verify the major type encoded on the first three bits of the first byte of the
     * DeviceEngagement structure. The major type value is equal to 5 (i.e., a map)."
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_02 - DeviceEngagement major type is 5 (map)`() {
        assertThat(rootNode.isObject, equalTo(true))
        assertThat(majorTypeOf(deviceEngagementBytes.first()), equalTo(MAJOR_TYPE_MAP))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_03
     *
     * "Verify the additional information encoded on the last five bits of the first byte of the
     * DeviceEngagement map ... The value ... is 2 or 3 or 4 or 5 or 6." Also verifies that the
     * only key-value pairs present have the expected key/value major types.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_03 - DeviceEngagement map size is between 2 and 6`() {
        assertThat(
            additionalInfoOf(deviceEngagementBytes.first()),
            anyOf(greaterThanOrEqualTo(2), lessThanOrEqualTo(6))
        )
        assertThat(rootNode.size(), greaterThanOrEqualTo(2))
        assertThat(rootNode.size(), lessThanOrEqualTo(6))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_03
     *
     * The mandatory key-value pairs have the correct value major types:
     * key 0 (Version) -> tstr, key 1 (Security) -> array, optional key 2
     * (DeviceRetrievalMethods) -> array.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDL_MS_DE_Gen_03 - DeviceEngagement entries have the correct value types`(
        @TestParameter input: Pair<String, (JsonNode) -> Boolean> = namedTestValues(
            "Version is a tstr" to (VERSION_KEY to JsonNode::isTextual),
            "Security is an array" to (SECURITY_KEY to JsonNode::isArray),
            "DeviceRetrievalMethods is an array" to
                (DEVICE_RETRIEVAL_METHODS_KEY to JsonNode::isArray)
        )
    ) {
        val (key, assertion) = input
        assertTrue(assertion(rootNode.get(key)))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_04
     *
     * "Verify the value of the key-value pair 0 (Version). The value equals 0x31 2E 30 ("1.0")."
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDL_MS_DE_Gen_04 - Version value equals 1_0`() {
        val versionNode = rootNode.get(VERSION_KEY)

        assertThat(versionNode.isTextual, equalTo(true))
        assertThat(versionNode.asText(), equalTo(VERSION))
        assertThat(versionNode.asText(), equalTo("1.0"))
        // 0x31 0x2E 0x30 is the UTF-8 encoding of "1.0"
        assertThat(
            versionNode.asText().toByteArray().toHexString(),
            equalTo("312e30")
        )
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_05
     *
     * "Verify the additional information ... of the Security array ... equal to 2 ... The only
     * data items present have the ... major types: 0 or 1 (int, cipher suite identifier) and 6
     * (tagged item, EDeviceKeyBytes) ... in the order given."
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_05 - Security array has 2 items in the correct order`() {
        assertThat(securityNode.isArray, equalTo(true))
        assertThat(securityNode.size(), equalTo(SECURITY_ELEMENT_COUNT))

        // 1st item: cipher suite identifier is an integer (major type 0 or 1)
        assertTrue(securityNode.get(0).isIntegralNumber)
        // 2nd item: EDeviceKeyBytes decodes to a binary value (the tag 24 wraps a bstr)
        assertTrue(securityNode.get(1).isBinary)
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_06
     *
     * "Verify the value of the cipher suite identifier data item. The value ... is equal to 1."
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 9.1.5.2
     */
    @Test
    fun `mDL_MS_DE_Gen_06 - cipher suite identifier equals 1`() {
        val cipherSuiteIdentifier = securityNode.get(0)

        assertTrue(cipherSuiteIdentifier.isIntegralNumber)
        assertThat(cipherSuiteIdentifier.intValue(), equalTo(CIPHER_SUITE_IDENTIFIER))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_07
     *
     * "Verify the additional information of the EDeviceKeyBytes data item [= 24] ... the tag value
     * encoded in the second byte [= 24] ... the major type of the encoded CBOR item [= 2, bstr]."
     *
     * This inspects the raw CBOR bytes of the Security array to find the `0xD8 0x18` (tag 24, encoded
     * on one following byte) prefix that must precede the byte string, per the EDeviceKeyBytes
     * definition #6.24(bstr).
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 9.1.1.4; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_07 - EDeviceKeyBytes is tag 24 wrapping a bstr`() {
        // 0xD8 = major type 6 (tag), additional info 24 (tag value on next 1 byte)
        // 0x18 = tag value 24 (encoded CBOR data item)
        // followed by a byte string, major type 2 -> 0x58 (bstr, length on next byte)
        val tag24Prefix = TAG_24_ONE_BYTE.toHexString() + TAG_VALUE_24.toHexString()
        val tag24WithBstr = tag24Prefix + BSTR_ONE_BYTE_LENGTH.toHexString()

        assertThat(deviceEngagementHexString, containsString(tag24Prefix))
        assertThat(deviceEngagementHexString, containsString(tag24WithBstr))

        // The additional information of the tag byte is 24
        assertThat(additionalInfoOf(TAG_24_ONE_BYTE), equalTo(TAG_ONE_BYTE_ADDITIONAL_INFO))
        // The major type of the wrapped item is 2 (bstr)
        assertThat(majorTypeOf(BSTR_ONE_BYTE_LENGTH), equalTo(MAJOR_TYPE_BSTR))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_08
     *
     * "For the EDeviceKey data item in the EDeviceKeyBytes data item, perform all Common_CBOR test
     * cases." The inner COSE_Key must be decodable, canonicalized (no indefinite length items) and
     * contain no duplicate keys.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 9.1.1.4
     */
    @Test
    fun `mDL_MS_DE_Gen_08 - EDeviceKey is decodable canonical CBOR`() {
        val eDeviceKeyBytes = securityNode.get(1).binaryValue()
        val eDeviceKeyNode = mapper.readTree(eDeviceKeyBytes)

        assertThat(eDeviceKeyNode, not(nullValue()))
        assertThat(eDeviceKeyNode.isObject, equalTo(true))

        val eDeviceKeyHex = eDeviceKeyBytes.toHexString()
        assertThat(eDeviceKeyHex, not(containsString(BYTE_STRING_INDEFINITE.toHexString())))
        assertThat(eDeviceKeyHex, not(containsString(BYTE_OBJECT_INDEFINITE.toHexString())))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_08
     *
     * The COSE_Key map keys are unique.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 9.1.1.4
     */
    @Test
    fun `mDL_MS_DE_Gen_08 - EDeviceKey has no duplicate keys`(
        @TestParameter key: String = testValues(
            COSE_KEY_TYPE_KEY,
            COSE_KEY_CURVE_KEY,
            COSE_KEY_X_KEY,
            COSE_KEY_Y_KEY
        )
    ) {
        val eDeviceKeyNode = mapper.readTree(securityNode.get(1).binaryValue())
        val occurrences = eDeviceKeyNode.fieldNames().asSequence().count { it == key }

        assertThat(occurrences, equalTo(1))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_09
     *
     * "Validate that the EDeviceKey ... is a valid COSE_Key, using either the compressed or
     * uncompressed format." The fixture uses the uncompressed EC2 P-256 format: kty (1) = 2 (EC2),
     * crv (-1) = 1 (P-256), x (-2) = 32-byte bstr, y (-3) = 32-byte bstr.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 9.1.1.4
     */
    @Test
    fun `mDL_MS_DE_Gen_09 - EDeviceKey is a valid COSE_Key`() {
        val eDeviceKeyNode = mapper.readTree(securityNode.get(1).binaryValue())

        // kty (1) = 2 (EC2)
        assertTrue(eDeviceKeyNode.get(COSE_KEY_TYPE_KEY).isIntegralNumber)
        assertThat(eDeviceKeyNode.get(COSE_KEY_TYPE_KEY).intValue(), equalTo(COSE_KEY_TYPE_EC2))
        // crv (-1) = 1 (P-256)
        assertTrue(eDeviceKeyNode.get(COSE_KEY_CURVE_KEY).isIntegralNumber)
        assertThat(eDeviceKeyNode.get(COSE_KEY_CURVE_KEY).intValue(), equalTo(COSE_KEY_CURVE_P256))
        // x (-2) uncompressed, 32-byte coordinate
        assertTrue(eDeviceKeyNode.get(COSE_KEY_X_KEY).isBinary)
        assertThat(eDeviceKeyNode.get(COSE_KEY_X_KEY).binaryValue().size, equalTo(P256_COORDINATE_SIZE))
        // y (-3) uncompressed, 32-byte coordinate
        assertTrue(eDeviceKeyNode.get(COSE_KEY_Y_KEY).isBinary)
        assertThat(eDeviceKeyNode.get(COSE_KEY_Y_KEY).binaryValue().size, equalTo(P256_COORDINATE_SIZE))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_11
     *
     * "Verify the additional information ... of the DeviceRetrievalMethods array ... at least 1 and
     * at most 3 ... The data item(s) present have the ... major types: 4 (array,
     * DeviceRetrievalMethod)." (QR-code engagement.)
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_11 - DeviceRetrievalMethods array size is between 1 and 3`() {
        assertThat(deviceRetrievalMethodsNode.isArray, equalTo(true))
        assertThat(deviceRetrievalMethodsNode.size(), greaterThanOrEqualTo(1))
        assertThat(deviceRetrievalMethodsNode.size(), lessThanOrEqualTo(3))

        // Every present data item is itself an array (major type 4)
        assertTrue(deviceRetrievalMethodsNode.all(JsonNode::isArray))
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_12
     *
     * For each DeviceRetrievalMethod array: it holds 2 or 3 items with major types 0 (uint, Type),
     * 0 (uint, Version) and 5 (map, RetrievalOptions) in that order; the Type value is 1, 2 or 3;
     * and every Type is unique across all DeviceRetrievalMethod arrays. (QR-code engagement.)
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 8.2.2.3; RFC 7049 section 2.1
     */
    @Test
    fun `mDL_MS_DE_Gen_12 - each DeviceRetrievalMethod is well-formed`() {
        deviceRetrievalMethodsNode.forEach { method ->
            assertThat(method.isArray, equalTo(true))
            // 2 or 3 items
            assertThat(method.size(), anyOf(equalTo(2), equalTo(3)))
            // 1st item: Type (uint)
            assertTrue(method.get(0).isIntegralNumber)
            // 2nd item: Version (uint)
            assertTrue(method.get(1).isIntegralNumber)
            // optional 3rd item: RetrievalOptions (map)
            if (method.size() == 3) {
                assertTrue(method.get(2).isObject)
            }
            // Type value is 1, 2 or 3
            assertThat(
                method.get(0).intValue(),
                anyOf(equalTo(1), equalTo(2), equalTo(3))
            )
        }
    }

    /**
     * Scenario ID: mDL_MS_DE_Gen_12 (step 5)
     *
     * Every DeviceRetrievalMethod Type value is unique within the DeviceRetrievalMethods array.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1; ISO/IEC 18013-5:2021, 8.2.2.3
     */
    @Test
    fun `mDL_MS_DE_Gen_12 - DeviceRetrievalMethod Type values are unique`() {
        val types = deviceRetrievalMethodsNode.map { it.get(0).intValue() }

        assertThat(types.toSet().size, equalTo(types.size))
    }

    /**
     * Sanity check that the fixture is a definite-length map whose first byte encodes the map and
     * the number of key-value pairs, aligning the byte-level checks above with the HexFormatter
     * convention used across the module's structural tests.
     */
    @Test
    fun `DeviceEngagement first byte encodes a definite-length map`() {
        assertThat(
            deviceEngagementHexString,
            startsWith(HexFormatter(CBORConstants.PREFIX_TYPE_OBJECT + rootNode.size()))
        )
    }

    private companion object {
        // DeviceEngagement map keys (as string keys in the decoded JsonNode tree)
        const val VERSION_KEY = "0"
        const val SECURITY_KEY = "1"
        const val DEVICE_RETRIEVAL_METHODS_KEY = "2"

        // COSE_Key labels (as string keys in the decoded JsonNode tree)
        const val COSE_KEY_TYPE_KEY = "1"
        const val COSE_KEY_CURVE_KEY = "-1"
        const val COSE_KEY_X_KEY = "-2"
        const val COSE_KEY_Y_KEY = "-3"

        const val COSE_KEY_TYPE_EC2 = 2
        const val COSE_KEY_CURVE_P256 = 1
        const val P256_COORDINATE_SIZE = 32

        const val SECURITY_ELEMENT_COUNT = 2
        const val CIPHER_SUITE_IDENTIFIER = 1

        // CBOR major types (top 3 bits of the initial byte)
        const val MAJOR_TYPE_MAP = 5
        const val MAJOR_TYPE_BSTR = 2

        // EDeviceKeyBytes tag encoding bytes
        const val TAG_24_ONE_BYTE: Byte = 0xD8.toByte() // major type 6, additional info 24
        const val TAG_VALUE_24: Byte = 0x18 // tag value 24
        const val BSTR_ONE_BYTE_LENGTH: Byte = 0x58 // major type 2, length on next byte
        const val TAG_ONE_BYTE_ADDITIONAL_INFO = 24

        /** Extracts the CBOR major type (top 3 bits) from an initial byte. */
        fun majorTypeOf(initialByte: Byte): Int = (initialByte.toInt() and 0xFF) ushr 5

        /** Extracts the CBOR additional information (bottom 5 bits) from an initial byte. */
        fun additionalInfoOf(initialByte: Byte): Int = initialByte.toInt() and 0x1F
    }
}
