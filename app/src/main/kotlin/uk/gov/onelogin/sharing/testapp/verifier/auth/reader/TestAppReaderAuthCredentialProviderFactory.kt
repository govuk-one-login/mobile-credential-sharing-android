package uk.gov.onelogin.sharing.testapp.verifier.auth.reader

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSigStructureGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1ProtectedHeaders
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1UnprotectedHeaderGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ECReaderAuthProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ReaderAuthCredentialProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.SigningSignatureStructure
import uk.gov.onelogin.sharing.testapp.credential.SIGNING_ALGORITHM
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.ReaderAuthOption

/**
 * Sample implementation of [ReaderAuthCredentialProvider.Factory].
 *
 * Internally manages the currently selected [ReaderAuthOption] that points to the reader
 * authentication's private key to use within the created [ReaderAuthCredentialProvider].
 */
@Singleton
class TestAppReaderAuthCredentialProviderFactory(
    @ApplicationContext
    private val context: Context,
    private val logger: Logger,
    initialState: ReaderAuthOption,
    private val keyFactory: KeyFactory,
    private val certificateFactory: CertificateFactory
) : ReaderAuthCredentialProvider.Factory {

    @Inject
    constructor(
        @ApplicationContext
        context: Context,
        logger: Logger
    ) : this(
        context = context,
        logger = logger,
        initialState = ReaderAuthOption.VALID,
        keyFactory = KeyFactory.getInstance("EC"),
        certificateFactory = CertificateFactory.getInstance("X.509")
    )

    private val _readerAuthOption: MutableStateFlow<ReaderAuthOption> = MutableStateFlow(
        initialState
    )

    val readerAuthOption: Flow<ReaderAuthOption> = _readerAuthOption

    /**
     * Builds a [ReaderAuthCredentialProvider] for the currently selected [ReaderAuthOption].
     *
     * This reads and parses the reader authentication key and certificate chain assets.
     * It is a blocking operation and callers are expected to invoke it off the main thread.
     */
    override fun create(): ReaderAuthCredentialProvider {
        val option = _readerAuthOption.value
        val privateKey = processPrivateKeyAssetChain(
            sequenceOf(
                option.privateKeyChain.first()
            )
        ).first()

        val certificateChain = getTransmittedCertificateChain(option)

        val signingAlgorithm = if (privateKey.params.order.bitLength() == P384_KEY_BIT_LENGTH) {
            SHA384_WITH_ECDSA
        } else {
            SIGNING_ALGORITHM
        }

        return ECReaderAuthProvider(
            logger = logger,
            certificateChain = certificateChain,
            protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger),
            unprotectedHeaderGenerator = CoseSign1UnprotectedHeaderGenerator(logger),
            sigStructureGenerator = SigningSignatureStructure(
                logger = logger,
                signature = Signature.getInstance(signingAlgorithm),
                privateKey = privateKey,
                decorated = CoseSigStructureGenerator(
                    logger = logger,
                    protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger = logger)
                )
            )
        )
    }

    fun update(option: ReaderAuthOption) {
        _readerAuthOption.value = option
    }

    private fun getTransmittedCertificateChain(option: ReaderAuthOption): List<X509Certificate> {
        val parsedCerts = processCertificateAssetChain(option.certificateChain.asSequence())

        require(parsedCerts.isNotEmpty()) {
            "Certificate chain for option $option is empty or an unprovisioned placeholder."
        }

        val nonRootCerts = parsedCerts.filterNot(::isSelfSigned)
        if (nonRootCerts.isEmpty()) {
            return parsedCerts
        }

        val leaf = nonRootCerts.firstOrNull { cert ->
            nonRootCerts.none { other -> other != cert && isSignedBy(other, cert) }
        } ?: nonRootCerts.last()

        val intermediates = nonRootCerts.filter { it != leaf }
        return listOf(leaf) + intermediates
    }

    private fun isSelfSigned(cert: X509Certificate): Boolean = try {
        cert.verify(cert.publicKey)
        true
    } catch (_: java.security.GeneralSecurityException) {
        false
    }

    private fun isSignedBy(cert: X509Certificate, issuer: X509Certificate): Boolean = try {
        cert.verify(issuer.publicKey)
        true
    } catch (_: java.security.GeneralSecurityException) {
        false
    }

    private fun processPrivateKeyAssetChain(chain: Sequence<String>): List<ECPrivateKey> = chain
        .map { assetPath ->
            val text = context.assets.open(assetPath).use { InputStreamReader(it).readText() }
            require(!text.contains("DVS_PLACEHOLDER")) {
                "Private key asset '$assetPath' is an unprovisioned placeholder."
            }
            text
        }
        .map(CharSequence::lines)
        .map { privateKeyLines ->
            privateKeyLines.filterNot { it.startsWith("-----") }
        }
        .map { it.joinToString("") }
        .map(Base64::decode)
        .map(::PKCS8EncodedKeySpec)
        .map(keyFactory::generatePrivate)
        .map { it as ECPrivateKey }
        .toList()

    @Suppress("TooGenericExceptionCaught")
    private fun processCertificateAssetChain(chain: Sequence<String>): List<X509Certificate> = try {
        chain
            .map(context.assets::open)
            .flatMap { input -> input.use(certificateFactory::generateCertificates).asSequence() }
            .filterIsInstance<X509Certificate>()
            .toList()
    } catch (e: Exception) {
        throw IllegalArgumentException("Unable to parse certificate chain asset", e)
    }

    private companion object {
        private const val P384_KEY_BIT_LENGTH = 384
        private const val SHA384_WITH_ECDSA = "SHA384withECDSA"
    }
}
