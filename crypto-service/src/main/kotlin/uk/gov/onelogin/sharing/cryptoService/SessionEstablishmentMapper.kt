package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.cbor.deriveUntaggedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionEstablishmentDto
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishment

fun SessionEstablishmentDto.toSessionEstablishment(): SessionEstablishment = SessionEstablishment(
    eReaderKey = eReaderKey.encoded,
    data = data
)

fun SessionEstablishment.toDto(): SessionEstablishmentDto = SessionEstablishmentDto(
    eReaderKey = EmbeddedCbor(deriveUntaggedCbor(eReaderKey)),
    data = data
)
