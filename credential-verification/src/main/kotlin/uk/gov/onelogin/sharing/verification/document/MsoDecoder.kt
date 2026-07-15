package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
class MsoDecoder {

    private val cborMapper = JsonMapper.builder(CBORFactory())
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .build()

    fun decode(encodedMso: ByteArray): MobileSecurityObject = try {
        val innerBytes = CborMapper.default.readValue(encodedMso, EmbeddedCbor::class.java).encoded
        cborMapper.readValue(innerBytes, MsoDto::class.java).toDomain()
    } catch (e: VerificationResult.Failure) {
        throw e
    } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
        throw malformed
    }

    private val malformed
        get() = VerificationResult.Failure(VerificationError.MALFORMED_MSO)
}
