package uk.gov.onelogin.sharing.cryptoService.cbor

object HexFormatter {
    @Suppress("SpreadOperator")
    operator fun invoke(vararg input: Any?) = "%02x".format(*input)
}
