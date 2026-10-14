package uk.gov.onelogin.sharing.models.mdoc.cbor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptionsDto
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementDto
import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ReaderAuthenticationDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto.DeviceAuthDTO
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto.DeviceResponseDTO
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto.DeviceSignedDTO
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto.DocumentDTO
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseDto.IssuerSignedDTO
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

/**
 * Enforces that all mDOC CBOR data models and wire payloads are strictly serialized
 * using definite-length headers as required by ISO/IEC 18013-5 and ISO/IEC 18013-6.
 *
 * Structurally walks CBOR payload trees to verify that no indefinite-length headers
 * (0xBF, 0x9F, 0x5F, 0x7F) or break markers (0xFF) exist in any serialized CBOR output.
 */
class CborDefiniteLengthContractTest {

    private val sampleItemsRequest = ItemsRequestDto(
        docType = "org.iso.18013.5.1.mDL",
        nameSpaces = mapOf(
            "org.iso.18013.5.1" to mapOf(
                "family_name" to true,
                "given_name" to false
            )
        )
    )

    private val sampleItemsRequestBytes = EmbeddedCbor(
        CborMapper.default.writeValueAsBytes(sampleItemsRequest)
    ).toCbor()

    private val sampleReaderAuth = byteArrayOf(
        0x84.toByte(),
        0x43.toByte(), 0xA1.toByte(), 0x01.toByte(), 0x26.toByte(),
        0xA1.toByte(), 0x18.toByte(), 0x21.toByte(), 0x41.toByte(), 0x00.toByte(),
        0xF6.toByte(),
        0x40.toByte()
    )

    private val sampleDocRequestWithAuth = DocRequestDto(
        itemsRequest = sampleItemsRequest,
        itemsRequestBytes = sampleItemsRequestBytes,
        readerAuth = sampleReaderAuth
    )

    private val sampleDocRequestWithoutAuth = DocRequestDto(
        itemsRequest = sampleItemsRequest,
        itemsRequestBytes = sampleItemsRequestBytes,
        readerAuth = null
    )

    @Test
    fun `ItemsRequestDto is serialized with definite-length CBOR`() {
        val bytes = sampleItemsRequest.toCbor()
        assertDefiniteLengthCbor(bytes, "ItemsRequestDto")
    }

    @Test
    fun `DocRequestDto with readerAuth is serialized with definite-length CBOR`() {
        val bytes = CborMapper.default.writeValueAsBytes(sampleDocRequestWithAuth)
        assertDefiniteLengthCbor(bytes, "DocRequestDto with readerAuth")
    }

    @Test
    fun `DocRequestDto without readerAuth is serialized with definite-length CBOR`() {
        val bytes = CborMapper.default.writeValueAsBytes(sampleDocRequestWithoutAuth)
        assertDefiniteLengthCbor(bytes, "DocRequestDto without readerAuth")
    }

    @Test
    fun `DeviceRequestDto with multiple DocRequests is serialized with definite-length CBOR`() {
        val dto = DeviceRequestDto(
            version = "1.0",
            docRequest = listOf(sampleDocRequestWithAuth, sampleDocRequestWithoutAuth)
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "DeviceRequestDto")
    }

    @Test
    fun `ReaderAuthenticationDto is serialized with definite-length CBOR`() {
        val sessionTranscript = byteArrayOf(0x83.toByte(), 0x01, 0x02, 0x03)
        val bytes = ReaderAuthenticationDto.createReaderAuthenticationBytes(
            untaggedSessionTranscriptBytes = sessionTranscript,
            itemsRequestBytes = sampleItemsRequestBytes
        )
        assertDefiniteLengthCbor(bytes, "ReaderAuthenticationDto")
    }

    @Test
    fun `BleOptionsDto without uuid is serialized with definite-length CBOR and omits key 10`() {
        val options = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = null
        )
        val bytes = options.toCbor()
        assertDefiniteLengthCbor(bytes, "BleOptionsDto without UUID")

        // Verify key 10 (0x0A) is omitted
        val hex = bytes.joinToString("") { "%02x".format(it) }
        assertFalse(
            "Key 10 (0x0A) must be omitted when peripheralServerModeUuid is null",
            hex.contains("0a")
        )
    }

    @Test
    fun `BleOptionsDto with uuid is serialized with definite-length CBOR`() {
        val options = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = byteArrayOf(
                1,
                2,
                3,
                4,
                5,
                6,
                7,
                8,
                9,
                10,
                11,
                12,
                13,
                14,
                15,
                16
            )
        )
        val bytes = options.toCbor()
        assertDefiniteLengthCbor(bytes, "BleOptionsDto with UUID")
    }

    @Test
    fun `DeviceRetrievalMethodDto is serialized with definite-length CBOR`() {
        val dto = DeviceRetrievalMethodDto(
            type = 0,
            version = 1,
            options = BleOptionsDto(
                serverMode = true,
                clientMode = false,
                peripheralServerModeUuid = null
            )
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "DeviceRetrievalMethodDto")
    }

    @Test
    fun `CoseKeyDto is serialized with definite-length CBOR`() {
        val dto = CoseKeyDto(
            keyType = 2,
            curve = 1,
            x = ByteArray(32) { 0x01 },
            y = ByteArray(32) { 0x02 }
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "CoseKeyDto")
    }

    @Test
    fun `SecurityDto is serialized with definite-length CBOR`() {
        val dto = SecurityDto(
            cipherSuiteIdentifier = 1,
            eDeviceKeyBytes = byteArrayOf(0xD8.toByte(), 0x18, 0x41, 0x01)
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "SecurityDto")
    }

    @Test
    fun `DeviceEngagementDto is serialized with definite-length CBOR`() {
        val dto = DeviceEngagementDto(
            version = "1.0",
            security = SecurityDto(1, byteArrayOf(0xD8.toByte(), 0x18, 0x41, 0x01)),
            deviceRetrievalMethods = listOf(
                DeviceRetrievalMethodDto(0, 1, BleOptionsDto(true, false, null))
            )
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "DeviceEngagementDto")
    }

    @Test
    fun `SessionEstablishmentDto is serialized with definite-length CBOR`() {
        val dto = SessionEstablishmentDto(
            eReaderKey = EmbeddedCbor(byteArrayOf(0xA1.toByte(), 0x01, 0x02)),
            data = byteArrayOf(0x01, 0x02, 0x03)
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "SessionEstablishmentDto")
    }

    @Test
    fun `SessionDataDto is serialized with definite-length CBOR`() {
        val dto = SessionDataDto(
            data = byteArrayOf(0x01, 0x02),
            status = 20u
        )
        val bytes = dto.toCbor()
        assertDefiniteLengthCbor(bytes, "SessionDataDto")
    }

    @Test
    fun `DeviceResponseDTO with documents is serialized with definite-length CBOR`() {
        val item = EmbeddedCbor(byteArrayOf(0xD8.toByte(), 0x18, 0x41, 0x01))
        val issuerSigned = IssuerSignedDTO(
            nameSpaces = mapOf("org.iso.18013.5.1" to listOf(item)),
            issuerAuth = RawCbor(
                byteArrayOf(
                    0x84.toByte(),
                    0x40,
                    0xA0.toByte(),
                    0xF6.toByte(),
                    0x40
                )
            )
        )
        val deviceSigned = DeviceSignedDTO(
            nameSpaces = EmbeddedCbor(byteArrayOf(0xA0.toByte())),
            deviceAuth = DeviceAuthDTO(
                deviceSignature = RawCbor(
                    byteArrayOf(
                        0x84.toByte(),
                        0x40,
                        0xA0.toByte(),
                        0xF6.toByte(),
                        0x40
                    )
                )
            )
        )
        val document = DocumentDTO(
            docType = "org.iso.18013.5.1.mDL",
            issuerSigned = issuerSigned,
            deviceSigned = deviceSigned
        )
        val response = DeviceResponseDTO(
            version = "1.0",
            documents = listOf(document),
            status = Status.OK.code
        )

        val bytes = response.toCbor()
        assertDefiniteLengthCbor(bytes, "DeviceResponseDTO")
    }

    private fun assertDefiniteLengthCbor(bytes: ByteArray, description: String) {
        assertTrue("$description must not be empty", bytes.isNotEmpty())
        val walker = CborHeaderWalker(bytes)
        walker.walkAndAssertDefinite(description)
    }

    private class CborHeaderWalker(private val bytes: ByteArray) {
        private var offset = 0

        fun walkAndAssertDefinite(description: String) {
            walkItem(description)
            assertFalse(
                "Trailing unparsed bytes in $description at offset $offset / ${bytes.size}",
                offset < bytes.size
            )
        }

        private fun walkItem(description: String) {
            assertTrue(
                "Unexpected end of CBOR stream in $description at offset $offset",
                offset < bytes.size
            )
            val initial = bytes[offset].toInt() and 0xFF
            val majorType = initial ushr 5
            val addInfo = initial and 0x1F

            assertFalse(
                "Indefinite-length CBOR header found in $description at offset $offset: 0x${
                    "%02X".format(
                        initial
                    )
                } (majorType=$majorType, addInfo=$addInfo)",
                addInfo == 31
            )

            offset++

            val length = when {
                addInfo < 24 -> addInfo
                addInfo == 24 -> readBytes(1)
                addInfo == 25 -> readBytes(2)
                addInfo == 26 -> readBytes(4)
                addInfo == 27 -> readBytes(8)
                else -> error("Invalid additional info $addInfo at offset ${offset - 1}")
            }

            when (majorType) {
                0, 1, 7 -> {
                }

                2, 3 -> {
                    offset += length
                }

                4 -> {
                    repeat(length) {
                        walkItem(description)
                    }
                }

                5 -> {
                    repeat(length) {
                        walkItem(description)
                        walkItem(description)
                    }
                }

                6 -> {
                    walkItem(description)
                }
            }
        }

        private fun readBytes(count: Int): Int {
            var value = 0
            repeat(count) {
                assertTrue("Truncated length argument at offset $offset", offset < bytes.size)
                value = (value shl 8) or (bytes[offset].toInt() and 0xFF)
                offset++
            }
            return value
        }
    }
}
