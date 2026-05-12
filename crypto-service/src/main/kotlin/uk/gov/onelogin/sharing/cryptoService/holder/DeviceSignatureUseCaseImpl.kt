package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeDeviceNameSpacesBytes

private const val CBOR_TAG_COSE_SIGN1 = 18
private const val COSE_SIGN1_ARRAY_SIZE = 4
private const val ES256_ALGORITHM = -7
private const val CBOR_MAP_MAJOR_TYPE = 0xa0
private const val CBOR_TEXT_MAJOR_TYPE = 0x60
private const val P256_COORDINATE_SIZE = 32

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<DeviceSignatureUseCase>())
class DeviceSignatureUseCaseImpl(private val logger: Logger) : DeviceSignatureUseCase {

    override fun buildDeviceSignedStructures(signatureBytes: ByteArray): DeviceSignatureResult {
        val coseSign1Array = createCoseSign1Array(signatureBytes)
        logger.debug(logTag, "Successfully constructed COSE_Sign1 array")

        val deviceAuth = createDeviceAuth(coseSign1Array)
        logger.debug(logTag, "Successfully constructed DeviceAuth")

        val deviceSigned = createDeviceSigned(deviceAuth)
        logger.debug(logTag, "Successfully constructed DeviceSigned")
        logger.debug(logTag, "DeviceSigned hex: ${deviceSigned.toHexString()}")

        return DeviceSignatureResult(
            coseSign1Array = coseSign1Array,
            deviceAuth = deviceAuth,
            deviceSigned = deviceSigned
        )
    }

    override fun buildCoseSignStructure(payload: ByteArray): ByteArray =
        ByteArrayOutputStream().also { output ->
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeStartArray(null, COSE_SIGN1_ARRAY_SIZE)
                gen.writeString("Signature1")
                gen.writeBinary(createProtectedHeader())
                gen.writeBinary(ByteArray(0))
                gen.writeBinary(payload)
                gen.writeEndArray()
            }
        }.toByteArray()

    private fun createCoseSign1Array(signatureBytes: ByteArray): ByteArray =
        ByteArrayOutputStream().also { output ->
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeStartArray(null, COSE_SIGN1_ARRAY_SIZE)
                gen.writeBinary(createProtectedHeader())
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeNull()
                gen.writeBinary(derSignatureToRaw(signatureBytes))
                gen.writeEndArray()
            }
        }.toByteArray()

    private fun createProtectedHeader(): ByteArray = ByteArrayOutputStream().also { output ->
        CBORFactory().createGenerator(output).use { gen ->
            gen.writeStartObject(1)
            gen.writeFieldId(1)
            gen.writeNumber(ES256_ALGORITHM.toLong())
            gen.writeEndObject()
        }
    }.toByteArray()

    private fun createDeviceAuth(coseSign1Array: ByteArray): ByteArray =
        ByteArrayOutputStream().also { output ->
            output.write(cborMap(1))
            output.write(cborTextKey("deviceSignature"))
            output.write(coseSign1Array)
        }.toByteArray()

    private fun createDeviceSigned(deviceAuth: ByteArray): ByteArray =
        ByteArrayOutputStream().also { output ->
            output.write(cborMap(2))
            output.write(cborTextKey("nameSpaces"))
            output.write(encodeDeviceNameSpacesBytes())
            output.write(cborTextKey("deviceAuth"))
            output.write(deviceAuth)
        }.toByteArray()

    private fun cborMap(size: Int): ByteArray = byteArrayOf((CBOR_MAP_MAJOR_TYPE or size).toByte())

    private fun cborTextKey(key: String): ByteArray {
        val keyBytes = key.toByteArray(Charsets.UTF_8)
        return byteArrayOf((CBOR_TEXT_MAJOR_TYPE or keyBytes.size).toByte()) + keyBytes
    }

    /**
     * Converts a DER-encoded ECDSA signature to the raw (r||s) format required by COSE.
     * Each integer is zero-padded to [P256_COORDINATE_SIZE] bytes.
     */
    private fun derSignatureToRaw(derSignature: ByteArray): ByteArray {
        // DER: 0x30 <len> 0x02 <rLen> <r> 0x02 <sLen> <s>
        require(derSignature[0].toInt() == 0x30) { "Not a DER signature" }
        var offset = 2 // skip SEQUENCE tag and length byte

        require(derSignature[offset].toInt() == 0x02) { "Expected INTEGER tag for r" }
        offset++
        val rLen = derSignature[offset++].toInt() and 0xFF
        val r = BigInteger(1, derSignature.copyOfRange(offset, offset + rLen))
        offset += rLen

        require(derSignature[offset].toInt() == 0x02) { "Expected INTEGER tag for s" }
        offset++
        val sLen = derSignature[offset++].toInt() and 0xFF
        val s = BigInteger(1, derSignature.copyOfRange(offset, offset + sLen))

        return r.toFixedByteArray(P256_COORDINATE_SIZE) +
            s.toFixedByteArray(P256_COORDINATE_SIZE)
    }

    private fun BigInteger.toFixedByteArray(size: Int): ByteArray {
        val bytes = toByteArray()
        return when {
            bytes.size == size -> bytes
            bytes.size > size -> bytes.copyOfRange(bytes.size - size, bytes.size)
            else -> ByteArray(size - bytes.size) + bytes
        }
    }
}
