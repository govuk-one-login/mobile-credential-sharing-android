package uk.gov.onelogin.sharing.cryptoService.cose

import uk.gov.onelogin.sharing.models.mdoc.security.CoseKeyDto

fun CoseKey.toDto(): CoseKeyDto = CoseKeyDto(
    keyType = keyType,
    curve = curve,
    x = x,
    y = y
)
