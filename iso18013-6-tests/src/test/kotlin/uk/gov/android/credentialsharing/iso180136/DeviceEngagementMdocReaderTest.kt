package uk.gov.android.credentialsharing.iso180136

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto

/**
 * ISO/IEC TS 18013-6:2025 conformance tests for Device Engagement mdoc reader.
 *
 * These tests validate that the mdoc reader correctly ignores unknown/RFU keys
 * in the DeviceEngagement CBOR structure per ISO/IEC 18013-5:2021, 8.2.1.1.
 *
 * DeviceEngagement = {
 *   0 : tstr,           ; version
 *   1 : Security,       ; security
 *   2 : DeviceRetrievalMethods, ; deviceRetrievalMethods
 *   ? 4 : ProtocolInfo  ; RFU
 * }
 */
class DeviceEngagementMdocReaderTest {
    private val cborMapper = CborMapper.default

    /**
     * mDLR_MS_DE_01 - Device Engagement mdoc reader
     *
     * Validates that the mdoc reader ignores the ProtocolInfo key-value pair
     * (key = 4) in the DeviceEngagement structure, because it is RFU.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDLR_MS_DE_01 - mdoc reader ignores ProtocolInfo RFU key in DeviceEngagement`() {
        // Arrange: DeviceEngagement with key=4 (ProtocolInfo) containing an arbitrary CBOR map
        val cborBytes = buildDeviceEngagementWithExtraKeys(
            extraKeys = mapOf(
                4 to
                    { gen ->
                        gen.writeStartObject(1)
                        gen.writeStringField("a", "b")
                        gen.writeEndObject()
                    }
            )
        )

        // Act: decode as the mdoc reader would
        val dto: DeviceEngagementDto = cborMapper.readValue(cborBytes)

        // Assert: valid fields are correctly parsed, RFU key ignored
        assertValidDeviceEngagement(dto)
    }

    /**
     * mDLR_MS_DE_02 - Device Engagement mdoc reader
     *
     * Validates that the mdoc reader ignores any additional key-value pairs in
     * the DeviceEngagement structure with a positive value for the key, because
     * these are RFU.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDLR_MS_DE_02 - mdoc reader ignores additional RFU positive key-value pairs`() {
        // Arrange: DeviceEngagement with multiple RFU keys (5, 24, 65535)
        val cborBytes = buildDeviceEngagementWithExtraKeys(
            extraKeys = mapOf(
                5 to { gen -> gen.writeString("rfu-text-value") },
                24 to
                    { gen ->
                        gen.writeStartObject(1)
                        gen.writeStringField("x", "y")
                        gen.writeEndObject()
                    },
                65535 to { gen -> gen.writeBoolean(false) }
            )
        )

        // Act
        val dto: DeviceEngagementDto = cborMapper.readValue(cborBytes)

        // Assert
        assertValidDeviceEngagement(dto)
    }

    /**
     * mDLR_MS_DE_03 - Device Engagement mdoc reader
     *
     * Validates that the mdoc reader ignores any key-value pairs in the
     * DeviceEngagement structure with a negative key value that it is not
     * able to interpret.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.2.1.1
     */
    @Test
    fun `mDLR_MS_DE_03 - mdoc reader ignores negative key values in DeviceEngagement`() {
        // Arrange: DeviceEngagement with key = -487 and value = a valid CBOR tstr
        val cborBytes = buildDeviceEngagementWithExtraKeys(
            extraKeys = mapOf(-487 to { gen -> gen.writeString("negative-key-value") })
        )

        // Act
        val dto: DeviceEngagementDto = cborMapper.readValue(cborBytes)

        // Assert
        assertValidDeviceEngagement(dto)
    }

    /**
     * mDLR_MS_DE_04 - Device Engagement mdoc reader
     *
     * Validates that the mdoc reader continues the transaction in case the
     * DeviceEngagement structure contains an unknown minor version number
     * but a known major version number.
     *
     * Reference: ISO/IEC 18013-5:2021, 8.1 and 8.2.1.1
     */
    @Test
    fun `mDLR_MS_DE_04 - mdoc reader accepts unknown minor version number`() {
        // Arrange: DeviceEngagement with version "1.5" (known major, unknown minor)
        val cborBytes = buildDeviceEngagementCbor(version = "1.5")

        // Act
        val dto: DeviceEngagementDto = cborMapper.readValue(cborBytes)

        // Assert: decoder successfully parses the structure
        assertEquals("1.5", dto.version)
        assertNotNull(dto.security)
        assertNotNull(dto.deviceRetrievalMethods)
        assertEquals(1, dto.deviceRetrievalMethods.size)
        assertEquals(2, dto.deviceRetrievalMethods.first().type)
    }

    private fun assertValidDeviceEngagement(dto: DeviceEngagementDto) {
        assertEquals(VERSION, dto.version)
        assertNotNull(dto.security)
        assertEquals(1, dto.security.cipherSuiteIdentifier)
        assertNotNull(dto.security.ephemeralPublicKey)
        assertNotNull(dto.deviceRetrievalMethods)
        assertEquals(1, dto.deviceRetrievalMethods.size)
        assertEquals(BLE_TYPE, dto.deviceRetrievalMethods.first().type)
        assertEquals(BLE_VERSION, dto.deviceRetrievalMethods.first().version)
    }

    /**
     * Builds a valid DeviceEngagement CBOR map with optional extra key-value pairs.
     * Each extra key maps to a lambda that writes the CBOR value using the generator.
     */
    private fun buildDeviceEngagementWithExtraKeys(
        extraKeys: Map<Int, (CBORGenerator) -> Unit>
    ): ByteArray = buildDeviceEngagementCbor(VERSION, extraKeys)

    /**
     * Constructs a DeviceEngagement CBOR map:
     * {
     *   0: version (tstr),
     *   1: [cipherSuiteId, tagged(ephemeralPublicKey)],  ; Security
     *   2: [[type, version, {options}]],                  ; DeviceRetrievalMethods
     *   ...extraKeys
     * }
     */
    private fun buildDeviceEngagementCbor(
        version: String = VERSION,
        extraKeys: Map<Int, (CBORGenerator) -> Unit> = emptyMap()
    ): ByteArray {
        val output = ByteArrayOutputStream()
        val gen = CBORFactory().createGenerator(output) as CBORGenerator
        val fieldCount = 3 + extraKeys.size
        gen.writeStartObject(fieldCount)

        // Key 0: version
        gen.writeFieldId(0)
        gen.writeString(version)

        // Key 1: Security = [cipherSuiteIdentifier, tagged(COSE_Key)]
        gen.writeFieldId(1)
        gen.writeStartArray(null, 2)
        gen.writeNumber(1) // cipher suite 1
        gen.writeTag(24) // CBOR tag 24 (embedded CBOR)
        gen.writeBinary(EPHEMERAL_KEY_CBOR)
        gen.writeEndArray()

        // Key 2: DeviceRetrievalMethods = [[type, version, options]]
        gen.writeFieldId(2)
        gen.writeStartArray(null, 1) // outer array
        gen.writeStartArray(null, 3) // inner [type, version, options]
        gen.writeNumber(BLE_TYPE)
        gen.writeNumber(BLE_VERSION)
        // BLE options map: {0: true, 1: false, 10: peripheralUuid}
        gen.writeStartObject(3)
        gen.writeFieldId(0)
        gen.writeBoolean(true)
        gen.writeFieldId(1)
        gen.writeBoolean(false)
        gen.writeFieldId(10)
        gen.writeBinary(PERIPHERAL_UUID)
        gen.writeEndObject()
        gen.writeEndArray() // end inner array
        gen.writeEndArray() // end outer array

        // Extra keys (RFU / negative / unknown)
        extraKeys.forEach { (key, writer) ->
            gen.writeFieldId(key.toLong())
            writer(gen)
        }

        gen.writeEndObject()
        gen.close()
        return output.toByteArray()
    }

    private companion object {
        const val VERSION = "1.0"
        const val BLE_TYPE = 2
        const val BLE_VERSION = 1

        // A minimal valid COSE_Key for EC2 P-256 (32-byte x and y coordinates)
        val EPHEMERAL_KEY_CBOR = byteArrayOf(
            0xA4.toByte(), 0x01, 0x02, 0x20, 0x01,
            0x21, 0x58, 0x20,
            // x-coordinate (32 bytes)
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
            0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
            0x22, 0x58, 0x20,
            // y-coordinate (32 bytes)
            0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28,
            0x29, 0x2A, 0x2B, 0x2C, 0x2D, 0x2E, 0x2F, 0x30,
            0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38,
            0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F, 0x40
        )

        // 16-byte peripheral UUID
        val PERIPHERAL_UUID = byteArrayOf(
            0x11, 0x11, 0x11, 0x11, 0x22, 0x22, 0x33, 0x33,
            0x44, 0x44, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55
        )
    }
}
