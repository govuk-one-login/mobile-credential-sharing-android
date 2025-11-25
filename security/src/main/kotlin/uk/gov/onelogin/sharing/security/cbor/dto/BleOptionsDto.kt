package uk.gov.onelogin.sharing.security.cbor.dto

data class BleOptionsDto(
    val serverMode: Boolean,
    val clientMode: Boolean,
    val peripheralServerModeUuid: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleOptionsDto

        if (serverMode != other.serverMode) return false
        if (clientMode != other.clientMode) return false
        if (peripheralServerModeUuid != null) {
            if (other.peripheralServerModeUuid == null) return false
            if (!peripheralServerModeUuid.contentEquals(
                    other.peripheralServerModeUuid
                )
            ) {
                return false
            }
        } else if (other.peripheralServerModeUuid != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = serverMode.hashCode()
        result = 31 * result + clientMode.hashCode()
        result = 31 * result + (peripheralServerModeUuid?.contentHashCode() ?: 0)
        return result
    }
}
