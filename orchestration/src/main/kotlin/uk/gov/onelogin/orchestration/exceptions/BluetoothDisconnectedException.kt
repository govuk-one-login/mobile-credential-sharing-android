package uk.gov.onelogin.orchestration.exceptions

data class BluetoothDisconnectedException(
    override val message: String,
    override val cause: Throwable
) : IllegalStateException(message, cause)
