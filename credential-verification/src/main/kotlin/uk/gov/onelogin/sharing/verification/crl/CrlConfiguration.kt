package uk.gov.onelogin.sharing.verification.crl

import android.content.Context
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import uk.gov.onelogin.sharing.verification.crl.CrlConfigurationException.InvalidResource
import uk.gov.onelogin.sharing.verification.crl.CrlConfigurationException.MissingResource

/**
 * CRL configuration supplied by the host app as `assets/credential-sharing-crl.json`:
 *
 * ```json
 * { "prefetchURLs": ["http://example.gov.uk/crl/abc.crl"] }
 * ```
 *
 * `prefetchURLs` is mandatory and may be empty. Every entry must be an absolute http or https
 * URL. [prefetchUrls] is deduplicated and sorted. Reading makes no network request.
 */
class CrlConfiguration private constructor(val prefetchUrls: List<String>) {

    companion object {
        const val ASSET_NAME = "credential-sharing-crl.json"
        private const val PREFETCH_URLS = "prefetchURLs"
        private val SCHEMES = setOf("http", "https")
        private val mapper = JsonMapper()

        /**
         * Reads the host app's asset through [context].
         *
         * @throws MissingResource if the asset does not exist
         * @throws InvalidResource if it is unreadable, malformed or has an invalid URL
         */
        fun load(context: Context): CrlConfiguration {
            val json = try {
                context.assets.open(ASSET_NAME).use { it.readBytes().toString(Charsets.UTF_8) }
            } catch (e: FileNotFoundException) {
                throw MissingResource().apply { initCause(e) }
            } catch (e: IOException) {
                throw InvalidResource("$ASSET_NAME: unreadable").apply { initCause(e) }
            }
            return parse(json)
        }

        internal fun parse(json: String): CrlConfiguration {
            val root = try {
                mapper.readTree(json)
            } catch (e: JsonProcessingException) {
                throw InvalidResource("$ASSET_NAME: not valid JSON").apply { initCause(e) }
            }
            if (root == null || !root.isObject) {
                throw InvalidResource("$ASSET_NAME: root must be a JSON object")
            }
            val array = root.get(PREFETCH_URLS)
                ?: throw InvalidResource("$ASSET_NAME: missing field $PREFETCH_URLS")
            if (!array.isArray) {
                throw InvalidResource("$ASSET_NAME: $PREFETCH_URLS must be an array")
            }
            val urls = array.mapIndexed { index, node ->
                val field = "$ASSET_NAME: $PREFETCH_URLS[$index]"
                val value = node.takeIf { it.isTextual }?.textValue()
                    ?: throw InvalidResource("$field must be a string")
                if (!isAbsoluteHttpUrl(value)) {
                    throw InvalidResource("$field is not an absolute http(s) URL")
                }
                value
            }
            return CrlConfiguration(urls.distinct().sorted())
        }

        private fun isAbsoluteHttpUrl(value: String): Boolean = try {
            val uri = URI(value)
            uri.isAbsolute && uri.scheme.lowercase() in SCHEMES && !uri.host.isNullOrEmpty()
        } catch (_: URISyntaxException) {
            false
        }
    }
}
