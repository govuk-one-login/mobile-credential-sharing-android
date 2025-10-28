package uk.gov.onelogin.sharing.models

import uk.gov.onelogin.sharing.models.MdocStubStrings.UUID
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

object BleRetrievalStub {

    val BLE_OPTIONS = BleOptions(
        serverMode = true,
        clientMode = false,
        peripheralServerModeUuid = EmbeddedCbor(UUID.toByteArray())
    )
    val BLE_RETRIEVAL_METHOD_SERVER_MODE =
        BleDeviceRetrievalMethod(
            type = BLE_TYPE,
            version = BLE_VERSION,
            options = BLE_OPTIONS
        )
}
