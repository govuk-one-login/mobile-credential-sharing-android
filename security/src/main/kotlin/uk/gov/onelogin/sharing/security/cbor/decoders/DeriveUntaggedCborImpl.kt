package uk.gov.onelogin.sharing.security.cbor.decoders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import com.fasterxml.jackson.dataformat.cbor.CBORFactory

class DeriveUntaggedCborImpl : DeriveUntaggedCbor {

    override fun deriveUntaggedCbor(tagged: ByteArray): ByteArray {
        val cborMapper = ObjectMapper(CBORFactory())

        val tree = cborMapper.readTree(tagged)

        val keyNode = tree as? BinaryNode
            ?: throw IllegalArgumentException("Expected tag 24 containing binary COSE_Key")

        return keyNode.binaryValue()
    }
}
