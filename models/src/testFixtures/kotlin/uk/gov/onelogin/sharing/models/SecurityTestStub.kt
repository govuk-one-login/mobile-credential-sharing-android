package uk.gov.onelogin.sharing.models

import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_CIPHER_ID
import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_EDEVICE_KEY
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.security.Security

object SecurityTestStub {
    val SECURITY = Security(
        cipherSuiteIdentifier = FAKE_CIPHER_ID,
        eDeviceKeyBytes = EmbeddedCbor(FAKE_EDEVICE_KEY.toByteArray())
    )
}
