package uk.gov.onelogin.sharing.verification.trust.cose

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.security.Signature
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.format.cose.CoseSign1
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@Inject
internal class CoseSignatureVerifier(private val headerValidator: CoseHeaderValidator) {
    private val cborMapper = ObjectMapper(CBORFactory())

    fun verify(
        coseSign1: CoseSign1,
        publicKey: ECPublicKey,
        payload: ByteArray,
        error: VerificationError
    ) {
        headerValidator.validate(coseSign1, error)
        val sigStructure = buildSigStructure(coseSign1.protectedHeader, payload)
        val derSignature = EcdsaSignatureTranscoder.rawToDer(coseSign1.signature, error)
        verifyEcdsa(sigStructure, derSignature, publicKey, error)
    }

    internal fun buildSigStructure(protectedHeader: ByteArray, payload: ByteArray): ByteArray {
        val array = cborMapper.createArrayNode()
        array.add("Signature1")
        array.add(protectedHeader)
        array.add(byteArrayOf())
        array.add(payload)
        return cborMapper.writeValueAsBytes(array)
    }

    private fun verifyEcdsa(
        sigStructure: ByteArray,
        derSignature: ByteArray,
        publicKey: ECPublicKey,
        error: VerificationError
    ) {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initVerify(publicKey)
        sig.update(sigStructure)
        if (!sig.verify(derSignature)) {
            throw VerificationResult.Failure(error)
        }
    }
}
