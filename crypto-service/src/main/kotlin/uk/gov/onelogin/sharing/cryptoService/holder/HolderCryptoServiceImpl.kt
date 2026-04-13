package uk.gov.onelogin.sharing.cryptoService.holder

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus

@ContributesBinding(scope = AppScope::class, binding = binding<HolderCryptoService>())
class HolderCryptoServiceImpl : HolderCryptoService {
    override fun buildTerminationSessionData(status: SessionDataStatus): ByteArray =
        SessionData(status = status).encodeCbor()
}
