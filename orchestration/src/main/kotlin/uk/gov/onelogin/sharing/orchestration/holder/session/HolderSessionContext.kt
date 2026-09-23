package uk.gov.onelogin.sharing.orchestration.holder.session

import java.security.KeyPair
import java.security.cert.X509Certificate
import java.util.UUID
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.verification.format.document.IssuerSigned

data class HolderSessionContext(
    val sessionUuid: UUID,
    val keyPair: KeyPair?,
    val engagement: String,
    val qrCode: String,
    val trustedReaderCertificates: List<X509Certificate> = emptyList(),
    val decryptCounter: UInt = 1u,
    val encryptCounter: UInt = 1u,
    val skDevice: ByteArray? = null,
    val sessionTranscriptBytes: ByteArray? = null,
    val validatedCredential: ValidatedCredential? = null,
    val filteredIssuerSigned: IssuerSigned? = null
)
