package uk.gov.onelogin.sharing.cryptoService

import uk.gov.onelogin.sharing.cryptoService.cbor.deriveUntaggedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishment
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishmentDto

fun SessionEstablishmentDto.toSessionEstablishment(): SessionEstablishment = SessionEstablishment(
    eReaderKey = eReaderKey.encoded,
    data = data
)

fun SessionEstablishment.toDto(): SessionEstablishmentDto = SessionEstablishmentDto(
    eReaderKey = EmbeddedCbor(deriveUntaggedCbor(eReaderKey)),
    data = data
)
