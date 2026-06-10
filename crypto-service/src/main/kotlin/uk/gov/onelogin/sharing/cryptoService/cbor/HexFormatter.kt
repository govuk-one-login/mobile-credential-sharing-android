package uk.gov.onelogin.sharing.cryptoService.cbor

import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter as ExchangeHexFormatter

object HexFormatter {
    @Suppress("SpreadOperator")
    operator fun invoke(vararg input: Any?) = ExchangeHexFormatter(*input)
}
