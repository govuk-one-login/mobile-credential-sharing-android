package uk.gov.onelogin.sharing.cryptoService.cbor

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

object ItemsRequestEncoderStub {
    const val MDL_DOC_TYPE = "org.iso.18013.5.1.mDL"
    const val MDL_NAMESPACE = "org.iso.18013.5.1"
    const val CBOR_TAG_24_BYTE_0 = 0xD8
    const val CBOR_TAG_24_BYTE_1 = 0x18

    val over21Request = ItemsRequest(
        docType = MDL_DOC_TYPE,
        nameSpaces = mapOf(
            MDL_NAMESPACE to mapOf(
                "portrait" to false,
                "age_over_21" to false
            )
        )
    )

    val over18Request = ItemsRequest(
        docType = MDL_DOC_TYPE,
        nameSpaces = mapOf(
            MDL_NAMESPACE to mapOf(
                "given_name" to true,
                "family_name" to true,
                "age_over_18" to false
            )
        )
    )
}
