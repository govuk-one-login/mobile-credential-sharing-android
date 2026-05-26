package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

sealed interface DeviceRetrievalMethod {
    val type: Int
    val version: Int
    val options: Any?
}
