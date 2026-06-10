package uk.gov.onelogin.sharing.models.mdoc.cbor

object HexFormatter {
    @Suppress("SpreadOperator")
    operator fun invoke(vararg input: Any?) = "%02x".format(*input)
}
