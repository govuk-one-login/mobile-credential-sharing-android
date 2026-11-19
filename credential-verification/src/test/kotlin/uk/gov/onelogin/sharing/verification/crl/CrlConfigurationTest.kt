package uk.gov.onelogin.sharing.verification.crl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.verification.crl.CrlConfigurationException.InvalidResource

class CrlConfigurationTest {

    @Test
    fun `reads prefetch urls`() {
        val config = CrlConfiguration.parse("""{"prefetchURLs": ["$A"]}""")

        assertEquals(listOf(A), config.prefetchUrls)
    }

    @Test
    fun `empty array is valid`() {
        assertEquals(emptyList<String>(), CrlConfiguration.parse("""{"prefetchURLs": []}""").prefetchUrls)
    }

    @Test
    fun `urls are deduplicated and sorted`() {
        val config = CrlConfiguration.parse("""{"prefetchURLs": ["$B", "$A", "$B"]}""")

        assertEquals(listOf(A, B), config.prefetchUrls)
    }

    @Test
    fun `https is accepted`() {
        val https = "https://example.gov.uk/crl/x.crl"

        assertEquals(listOf(https), CrlConfiguration.parse("""{"prefetchURLs": ["$https"]}""").prefetchUrls)
    }

    @Test
    fun `unknown extra fields are ignored`() {
        val config = CrlConfiguration.parse("""{"prefetchURLs": ["$A"], "other": 1}""")

        assertEquals(listOf(A), config.prefetchUrls)
    }

    @Test
    fun `invalid resources are rejected with a reason naming the field`() {
        val cases = mapOf(
            "" to "root must be a JSON object",
            "not json" to "not valid JSON",
            "[]" to "root must be a JSON object",
            "{}" to "missing field prefetchURLs",
            """{"prefetchURLs": "x"}""" to "prefetchURLs must be an array",
            """{"prefetchURLs": [1]}""" to "prefetchURLs[0] must be a string",
            """{"prefetchURLs": ["$A", "/crl/relative.crl"]}""" to "prefetchURLs[1] is not an absolute",
            """{"prefetchURLs": ["ftp://example.gov.uk/x.crl"]}""" to "prefetchURLs[0] is not an absolute",
            """{"prefetchURLs": ["http://"]}""" to "prefetchURLs[0] is not an absolute",
            """{"prefetchURLs": ["not a url"]}""" to "prefetchURLs[0] is not an absolute"
        )

        cases.forEach { (json, expectedReason) ->
            val e = assertThrows("input: $json", InvalidResource::class.java) {
                CrlConfiguration.parse(json)
            }
            assertTrue("'${e.reason}' should contain '$expectedReason'", expectedReason in e.reason)
        }
    }

    private companion object {
        const val A = "http://dvs.verifier-ca.integration.account.gov.uk/crl/a.crl"
        const val B = "http://root.verifier-root-ca.integration.account.gov.uk/crl/b.crl"
    }
}
