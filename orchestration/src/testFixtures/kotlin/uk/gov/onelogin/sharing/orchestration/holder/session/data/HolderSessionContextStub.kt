package uk.gov.onelogin.sharing.orchestration.holder.session.data

import java.util.UUID
import uk.gov.onelogin.sharing.cryptoService.SessionSecurityTestStub
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionContext

object HolderSessionContextStub {

    val holderSessionContextStub = HolderSessionContext(
        sessionUuid = UUID.randomUUID(),
        keyPair = SessionSecurityTestStub.generateValidKeyPair(),
        engagement = "engagement",
        qrCode = "qr_code",
        decryptCounter = 1u
    )
}
