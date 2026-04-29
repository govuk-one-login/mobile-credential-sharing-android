package uk.gov.onelogin.sharing.cryptoService.holder

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import java.io.ByteArrayOutputStream
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeDeviceNameSpacesBytes

private const val CBOR_TAG_COSE_SIGN1 = 18
private const val COSE_SIGN1_ARRAY_SIZE = 4
private const val ES256_ALGORITHM = -7
private const val CBOR_MAP_MAJOR_TYPE = 0xa0
private const val CBOR_TEXT_MAJOR_TYPE = 0x60

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

    private fun createCoseSign1Array(signatureBytes: ByteArray): ByteArray =
        ByteArrayOutputStream().also { output ->
            CBORFactory().createGenerator(output).use { gen ->
                (gen as CBORGenerator).writeTag(CBOR_TAG_COSE_SIGN1)
                gen.writeStartArray(null, COSE_SIGN1_ARRAY_SIZE)
                gen.writeBinary(createProtectedHeader())
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeNull()
                gen.writeBinary(signatureBytes)
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
}
