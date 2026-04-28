package uk.gov.onelogin.sharing.cryptoService.holder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeDeviceNameSpacesBytes

private const val CBOR_INDEFINITE_MAP = 0xBF
private const val CBOR_BREAK = 0xFF
private const val CBOR_TAG_COSE_SIGN1 = 0xD2
private const val CBOR_ARRAY_4 = 0x84
private const val CBOR_EMPTY_MAP = 0xA0
private const val CBOR_NULL = 0xF6
private const val ES256_ALGORITHM = -7

@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<DeviceSignatureUseCase>())
class DeviceSignatureUseCaseImpl(
    private val logger: Logger
) : DeviceSignatureUseCase {

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

    private fun createCoseSign1Array(signatureBytes: ByteArray): ByteArray {
        return ByteArrayOutputStream().use { output ->
            output.write(CBOR_TAG_COSE_SIGN1)
            output.write(CBOR_ARRAY_4)
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeBinary(createProtectedHeader())
            }
            output.write(CBOR_EMPTY_MAP)
            output.write(CBOR_NULL)
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeBinary(signatureBytes)
            }
            output.toByteArray()
        }
    }

    private fun createProtectedHeader(): ByteArray {
        return ByteArrayOutputStream().use { output ->
            CBORFactory().createGenerator(output).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(ES256_ALGORITHM.toLong())
                gen.writeEndObject()
            }
            output.toByteArray()
        }
    }

    private fun cborTextKey(key: String): ByteArray {
        val keyBytes = key.toByteArray(Charsets.UTF_8)
        return byteArrayOf((0x60 or keyBytes.size).toByte()) + keyBytes
    }

    private fun createDeviceAuth(coseSign1Array: ByteArray): ByteArray {
        return ByteArrayOutputStream().use { output ->
            output.write(CBOR_INDEFINITE_MAP)
            output.write(cborTextKey("deviceSignature"))
            output.write(coseSign1Array)
            output.write(CBOR_BREAK)
            output.toByteArray()
        }
    }

    private fun createDeviceSigned(deviceAuth: ByteArray): ByteArray {
        return ByteArrayOutputStream().use { output ->
            output.write(CBOR_INDEFINITE_MAP)
            output.write(cborTextKey("nameSpaces"))
            output.write(encodeDeviceNameSpacesBytes())
            output.write(cborTextKey("deviceAuth"))
            output.write(deviceAuth)
            output.write(CBOR_BREAK)
            output.toByteArray()
        }
    }
}
