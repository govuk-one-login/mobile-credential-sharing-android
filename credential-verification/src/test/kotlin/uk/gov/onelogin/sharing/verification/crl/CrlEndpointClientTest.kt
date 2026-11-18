package uk.gov.onelogin.sharing.verification.crl

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.android.network.api.v2.ApiRequest
import uk.gov.android.network.client.v2.GenericHttpResponse
import uk.gov.android.network.client.v2.GenericResponseException
import uk.gov.android.network.client.v2.StubHttpClient

class CrlEndpointClientTest {
    private val stub = StubHttpClient()
    private val client = CrlEndpointClient(stub)

    @Test
    fun `sends an unauthenticated GET to the given url`() = runBlocking {
        stub.response = GenericHttpResponse(status = 200, body = "not a crl")

        client.fetch(URL)

        assertEquals(ApiRequest.Get(url = URL), stub.receivedRequest)
    }

    @Test
    fun `non-crl body is reported as malformed`() = runBlocking {
        stub.response = GenericHttpResponse(status = 200, body = "not a crl")

        val result = client.fetch(URL)

        assertTrue(result is CrlFetchResult.Malformed)
    }

    @Test
    fun `non-success status is reported as http error`() = runBlocking {
        stub.exception = GenericResponseException(
            response = GenericHttpResponse(status = 404, body = ""),
            cause = IllegalStateException("404")
        )

        assertEquals(CrlFetchResult.HttpError(404), client.fetch(URL))
    }

    @Test
    fun `connection failure is reported as unreachable`() = runBlocking {
        val failure = IOException("offline")
        stub.exception = failure

        assertEquals(CrlFetchResult.Unreachable(failure), client.fetch(URL))
    }

    private companion object {
        const val URL = "http://dvs.verifier-ca.integration.account.gov.uk/crl/" +
            "85b2ae92-97ab-406b-93b2-f765795de390.crl"
    }
}
