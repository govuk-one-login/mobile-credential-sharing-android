package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

enum class MdocState(val code: Byte) {
    START(0x01);

    companion object {
        fun fromByte(byte: Byte): MdocState? = entries.firstOrNull {
            it.code == byte
        }
    }
}
