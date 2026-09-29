package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

/**
 * Classifies the leaf certificate backing a [ReaderAuthOption] so the UI can
 * fail closed when a DVS option has not been provisioned by the build pipeline.
 */
class ReaderAuthCertificateValidator(
    private val context: Context,
    private val certificateFactory: CertificateFactory
) {
    @Inject
    constructor(
        @ApplicationContext
        context: Context
    ) : this(context, CertificateFactory.getInstance("X.509"))

    /**
     * Reads the leaf certificate asset for [option] and classifies it.
     */
    fun validate(option: ReaderAuthOption): ReaderAuthCertificateStatus {
        val bytes = runCatching {
            context.assets.open(option.leafCertificateAsset).use { it.readBytes() }
        }.getOrElse { return ReaderAuthCertificateStatus.PLACEHOLDER }

        return classify(bytes)
    }

    /**
     * Classifies raw leaf certificate [bytes]. The first X.509 certificate is
     * used, so a leaf-first chain file resolves to its leaf. Anything that is
     * not a parseable, in-date certificate is treated as an unprovisioned
     * placeholder so the UI fails closed.
     */
    internal fun classify(bytes: ByteArray): ReaderAuthCertificateStatus {
        if (bytes.decodeToString().contains(PLACEHOLDER_SENTINEL)) {
            return ReaderAuthCertificateStatus.PLACEHOLDER
        }

        val certificate = runCatching {
            certificateFactory.generateCertificates(bytes.inputStream())
                .filterIsInstance<X509Certificate>()
                .firstOrNull()
        }.getOrNull() ?: return ReaderAuthCertificateStatus.PLACEHOLDER

        return runCatching {
            certificate.checkValidity()
            ReaderAuthCertificateStatus.VALID
        }.getOrDefault(ReaderAuthCertificateStatus.PLACEHOLDER)
    }

    private companion object {
        const val PLACEHOLDER_SENTINEL = "DVS_PLACEHOLDER_CERTIFICATE_REPLACE_IN_CI"
    }
}

/**
 * Result of validating a reader-auth leaf certificate.
 */
enum class ReaderAuthCertificateStatus {
    /** A parseable, in-date certificate. */
    VALID,

    /**
     * The build was not provisioned: the asset is an unreplaced CI placeholder,
     * or is otherwise missing / unusable as an in-date X.509 certificate.
     */
    PLACEHOLDER
}
