package uk.gov.onelogin.sharing.security

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.SessionEstablishment
import uk.gov.onelogin.sharing.security.cbor.dto.SessionEstablishmentDto

/**
 * Converts a [SessionEstablishmentDto] into its corresponding domain model, [SessionEstablishment].
 *
 * This extension function serves as a mapper to transform the Data Transfer Object (DTO), into the
 * application's internal domain model. It extracts the raw byte array from the
 * [EmbeddedCbor] wrapper for the `eReaderKey`.
 *
 * @receiver The [SessionEstablishmentDto] instance to be converted.
 * @return A new instance of [SessionEstablishment] containing the mapped data.
 * @see SessionEstablishment
 * @see SessionEstablishmentDto
 */
fun SessionEstablishmentDto.toSessionEstablishment(): SessionEstablishment = SessionEstablishment(
    eReaderKey = eReaderKey.encoded,
    data = data
)
