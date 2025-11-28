package uk.gov.onelogin.sharing.verifier.scan.state.data

import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriTwo
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR

/**
 * Wrapper object containing test data for [BarcodeDataResult].
 */
object BarcodeDataResultStubs {
    val invalidBarcodeDataResultOne = BarcodeDataResult.Invalid(exampleUriOne)
    val invalidBarcodeDataResultTwo = BarcodeDataResult.Invalid(exampleUriTwo)

    val validBarcodeDataResult = BarcodeDataResult.Valid(VALID_CBOR)
}
