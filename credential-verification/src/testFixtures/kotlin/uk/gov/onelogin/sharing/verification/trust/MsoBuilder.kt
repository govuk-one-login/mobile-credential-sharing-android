package uk.gov.onelogin.sharing.verification.trust

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * Builds minimal MSO (Mobile Security Object) CBOR payloads for testing.
 * The payload is Tag-24 wrapped as expected by the decoder.
 */
object MsoBuilder {
    private val cborFactory = CBORFactory()
    private val cborMapper = ObjectMapper(cborFactory)

    fun build(
        docType: String = "org.iso.18013.5.1.mDL",
        validFrom: Date = Date(),
        validUntil: Date = Date(System.currentTimeMillis() + 365L * 86400000L)
    ): ByteArray {
        val mso = cborMapper.createObjectNode()
        mso.put("version", "1.0")
        mso.put("digestAlgorithm", "SHA-256")
        mso.put("docType", docType)
        val validity = cborMapper.createObjectNode()
        validity.put("signed", formatDate(validFrom))
        validity.put("validFrom", formatDate(validFrom))
        validity.put("validUntil", formatDate(validUntil))
        mso.set<ObjectNode>("validityInfo", validity)
        mso.set<ObjectNode>("valueDigests", cborMapper.createObjectNode())

        val msoBytes = cborMapper.writeValueAsBytes(mso)

        // Tag-24 wrap
        val out = ByteArrayOutputStream()
        cborFactory.createGenerator(out).use { gen ->
            gen.writeTag(24)
            gen.writeBinary(msoBytes)
        }
        return out.toByteArray()
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }
}
