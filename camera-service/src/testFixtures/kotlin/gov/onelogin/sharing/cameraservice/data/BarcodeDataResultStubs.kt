package gov.onelogin.sharing.cameraservice.data

import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriTwo
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT

/**
 * Wrapper object containing test data for [BarcodeDataResult].
 */
object BarcodeDataResultStubs {
    val invalidBarcodeDataResultOne = BarcodeDataResult.Invalid(exampleUriOne)
    val invalidBarcodeDataResultTwo = BarcodeDataResult.Invalid(exampleUriTwo)

    val validBarcodeDataResult = BarcodeDataResult.Valid(VALID_ENCODED_DEVICE_ENGAGEMENT)
    val undecodeableBarcodeDataResult = BarcodeDataResult.Valid(INVALID_CBOR)
}
