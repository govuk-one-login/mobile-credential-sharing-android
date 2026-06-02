package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedStub.sharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedStub.sharingIssuerSignedJson

class SharingIssuerSignedSerializerTest {
    @Test
    fun `Successfully encodes to a String`() {
        val result = Json.encodeToString(
            SharingIssuerSignedSerializer(),
            sharingIssuerSigned
        )

        assertThat(
            result,
            equalTo(sharingIssuerSignedJson)
        )
    }

    @Test
    fun `Successfully decodes a String`() {
        val result = Json.decodeFromString(
            SharingIssuerSignedSerializer(),
            sharingIssuerSignedJson
        )

        assertThat(
            result,
            equalTo(sharingIssuerSigned)
        )
    }
}