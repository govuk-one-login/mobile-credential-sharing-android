package uk.gov.onelogin.sharing.orchestration.holder.session

import java.security.KeyPair
import java.util.UUID

data class HolderSessionContext(
    val sessionUuid: UUID,
    val keyPair: KeyPair?,
    val engagement: String,
    val qrCode: String
)
