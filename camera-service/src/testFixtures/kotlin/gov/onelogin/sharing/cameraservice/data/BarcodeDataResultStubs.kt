package gov.onelogin.sharing.cameraservice.data

import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriTwo
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT

/**
 * Wrapper object containing test data for [BarcodeDataResult].
 */
object BarcodeDataResultStubs {
    val invalidBarcodeDataResultOne = exampleUriOne
    val invalidBarcodeDataResultTwo = exampleUriTwo

    val validBarcodeDataResult = VALID_ENCODED_DEVICE_ENGAGEMENT
    val undecodeableBarcodeDataResult = INVALID_CBOR
}
