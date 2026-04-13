package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.cose.Cose
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey

object CoseKeyStub {
    // ISO 18013-5 Appendix D.3.1 (EDeviceKey)
    val D_3_1_EDEVICE_KEY = CoseKey(
        keyType = Cose.KEY_TYPE_EC2,
        curve = Cose.CURVE_P256,
        x = "5a88d182bce5f42efa59943f33359d2e8a968ff289d93e5fa444b624343167fe"
            .hexToByteArray(),
        y = "b16e8cf858ddc7690407ba61d4c338237a8cfcf3de6aa672fc60a557aa32fc67"
            .hexToByteArray()
    )

    const val D_3_1_EDEVICE_KEY_HEX =
        "a4010220012158205a88d182bce5f42efa59943f33359d2e" +
            "8a968ff289d93e5fa444b624343167fe225820b16e8cf858ddc7690407ba61d4" +
            "c338237a8cfcf3de6aa672fc60a557aa32fc67"
}
