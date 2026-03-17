package uk.gov.onelogin.sharing.orchestration.exceptions

data class BluetoothDisconnectedException(
    override val message: String,
    override val cause: Throwable
) : IllegalStateException(message, cause)
