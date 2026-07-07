package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.security.interfaces.ECPublicKey

object CoseSign1Stubs {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    val validCoseSign1WithNullPayload: ByteArray = buildCoseSign1(nullPayload = true)
    val coseSign1WithNonNullPayload: ByteArray = buildCoseSign1(nullPayload = false)
    val malformedCoseSign1: ByteArray = byteArrayOf(0xFF.toByte(), 0x01)

    val emptyDeviceNameSpacesBytes: ByteArray = run {
        val emptyMap = ByteArrayOutputStream().also { out ->
            cborFactory.createGenerator(out).use { gen ->
                gen.writeStartObject(0)
                gen.writeEndObject()
            }
        }.toByteArray()
        wrapInTag24(emptyMap)
    }

    val sessionTranscriptBytes: ByteArray = ByteArrayOutputStream().also { out ->
        cborFactory.createGenerator(out).use { gen ->
            gen.writeStartArray(null, 3)
            gen.writeNull()
            gen.writeNull()
            gen.writeNull()
            gen.writeEndArray()
        }
    }.toByteArray()

    fun wrapInTag24(content: ByteArray): ByteArray = ByteArrayOutputStream().also { out ->
        cborFactory.createGenerator(out).use { gen ->
            gen.writeTag(24)
            gen.writeBinary(content)
        }
    }.toByteArray()

    fun coseKeyBytes(key: ECPublicKey): ByteArray {
        val x = fixCoordinate(key.w.affineX.toByteArray())
        val y = fixCoordinate(key.w.affineY.toByteArray())
        val node = cborMapper.createObjectNode()
        node.put("1", 2)
        node.put("-1", 1)
        node.put("-2", x)
        node.put("-3", y)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun fixCoordinate(bytes: ByteArray): ByteArray = when {
        bytes.size == 33 && bytes[0] == 0.toByte() -> bytes.copyOfRange(1, 33)
        bytes.size < 32 -> ByteArray(32 - bytes.size) + bytes
        else -> bytes
    }

    private fun buildCoseSign1(nullPayload: Boolean): ByteArray {
        val protectedHeader = ByteArrayOutputStream().also { out ->
            cborFactory.createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(-7L)
                gen.writeEndObject()
            }
        }.toByteArray()

        return ByteArrayOutputStream().also { out ->
            cborFactory.createGenerator(out).use { gen ->
                gen.writeStartArray(null, 4)
                gen.writeBinary(protectedHeader)
                gen.writeStartObject(0)
                gen.writeEndObject()
                if (nullPayload) gen.writeNull() else gen.writeBinary(byteArrayOf(0x01, 0x02))
                gen.writeBinary(ByteArray(64))
                gen.writeEndArray()
            }
        }.toByteArray()
    }
}
