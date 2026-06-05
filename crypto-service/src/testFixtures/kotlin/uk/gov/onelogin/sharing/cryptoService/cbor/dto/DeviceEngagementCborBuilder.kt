package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.ByteArrayOutputStream

object DeviceEngagementCborBuilder {
    const val VERSION = "1.0"
    const val BLE_TYPE = 2
    const val BLE_VERSION = 1

    // A minimal valid COSE_Key for EC2 P-256 (32-byte x and y coordinates)
    val EPHEMERAL_KEY_CBOR = byteArrayOf(
        0xA4.toByte(), 0x01, 0x02, 0x20, 0x01,
        0x21, 0x58, 0x20,
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
        0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
        0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20,
        0x22, 0x58, 0x20,
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

    fun build(
        version: String = VERSION,
        extraKeys: Map<Int, (CBORGenerator) -> Unit> = emptyMap()
    ): ByteArray {
        val output = ByteArrayOutputStream()
        val gen = CBORFactory().createGenerator(output) as CBORGenerator
        val fieldCount = 3 + extraKeys.size
        gen.writeStartObject(fieldCount)

        writeVersion(gen, version)
        writeSecurity(gen)
        writeDeviceRetrievalMethods(gen)

        extraKeys.forEach { (key, writer) ->
            gen.writeFieldId(key.toLong())
            writer(gen)
        }

        gen.writeEndObject()
        gen.close()
        return output.toByteArray()
    }

    private fun writeVersion(gen: CBORGenerator, version: String) {
        gen.writeFieldId(0)
        gen.writeString(version)
    }

    private fun writeSecurity(gen: CBORGenerator) {
        gen.writeFieldId(1)
        gen.writeStartArray(null, 2)
        gen.writeNumber(1)
        gen.writeTag(24)
        gen.writeBinary(EPHEMERAL_KEY_CBOR)
        gen.writeEndArray()
    }

    private fun writeDeviceRetrievalMethods(gen: CBORGenerator) {
        gen.writeFieldId(2)
        gen.writeStartArray(null, 1)
        gen.writeStartArray(null, 3)
        gen.writeNumber(BLE_TYPE)
        gen.writeNumber(BLE_VERSION)
        gen.writeStartObject(3)
        gen.writeFieldId(0)
        gen.writeBoolean(true)
        gen.writeFieldId(1)
        gen.writeBoolean(false)
        gen.writeFieldId(10)
        gen.writeBinary(PERIPHERAL_UUID)
        gen.writeEndObject()
        gen.writeEndArray()
        gen.writeEndArray()
    }
}
