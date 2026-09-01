package uk.gov.onelogin.sharing.verification.cose.internal.signature

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import dev.zacsweers.metro.Inject
import java.security.Signature
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.InvalidSignature
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.InternalCoseSign1

@Inject
internal class CoseSignatureVerifier(private val headerValidator: CoseHeaderValidator) {
    private val cborMapper = ObjectMapper(CBORFactory())

    fun verify(coseSign1: InternalCoseSign1, publicKey: ECPublicKey, payload: ByteArray) {
        headerValidator.validate(coseSign1)
        val sigStructure = buildSigStructure(coseSign1.protectedHeader, payload)
        val derSignature = EcdsaSignatureTranscoder.rawToDer(coseSign1.signature)
        verifyEcdsa(sigStructure, derSignature, publicKey)
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
        publicKey: ECPublicKey
    ) {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initVerify(publicKey)
        sig.update(sigStructure)
        if (!sig.verify(derSignature)) {
            throw InvalidSignature
        }
    }
}
