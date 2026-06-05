package uk.gov.android.credentialsharing.iso180136

import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.assertEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementCborBuilder
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementCborBuilder.BLE_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementCborBuilder.BLE_VERSION
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementCborBuilder.VERSION
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDtoMatchers.hasDeviceRetrievalMethods
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDtoMatchers.hasSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDtoMatchers.hasVersion

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
        val cborBytes = DeviceEngagementCborBuilder.build(
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
        val cborBytes = DeviceEngagementCborBuilder.build(
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
        val cborBytes = DeviceEngagementCborBuilder.build(
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
        val cborBytes = DeviceEngagementCborBuilder.build(version = "1.5")

        // Act
        val dto: DeviceEngagementDto = cborMapper.readValue(cborBytes)

        // Assert: decoder successfully parses the structure
        assertThat(dto, hasVersion("1.5"))
        assertThat(dto, hasSecurity(notNullValue()))
        assertThat(dto, hasDeviceRetrievalMethods(hasSize(1)))
        assertEquals(2, dto.deviceRetrievalMethods.first().type)
    }

    private fun assertValidDeviceEngagement(dto: DeviceEngagementDto) {
        assertThat(dto, hasVersion(VERSION))
        assertThat(dto, hasSecurity(notNullValue()))
        assertThat(dto.security.cipherSuiteIdentifier, equalTo(1))
        assertThat(dto, hasDeviceRetrievalMethods(hasSize(1)))
        assertEquals(BLE_TYPE, dto.deviceRetrievalMethods.first().type)
        assertEquals(BLE_VERSION, dto.deviceRetrievalMethods.first().version)
    }
}
