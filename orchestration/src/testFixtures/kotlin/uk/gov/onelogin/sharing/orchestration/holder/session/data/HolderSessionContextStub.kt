package uk.gov.onelogin.sharing.orchestration.holder.session.data

import java.util.UUID
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionContext
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub

object HolderSessionContextStub {

    val holderSessionContextStub = HolderSessionContext(
        sessionUuid = UUID.randomUUID(),
        keyPair = SessionSecurityTestStub.generateValidKeyPair(),
        engagement = "engagement",
        qrCode = "qr_code"
    )
}
