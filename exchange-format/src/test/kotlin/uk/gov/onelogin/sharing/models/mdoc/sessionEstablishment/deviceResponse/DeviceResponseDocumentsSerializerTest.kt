package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import kotlinx.serialization.json.Json
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DeviceResponseDocumentsSerializerTest {
    private val documentList = listOf(DeviceResponseStub.document)
    private val documentJsonArray = "[${DeviceResponseStub.documentJson}]"

    @Test
    fun `Successfully encodes to a String`() {
        val result = Json.encodeToString(
            DeviceResponseDocumentsSerializer(),
            documentList
        )

        assertThat(
            result,
            equalTo(documentJsonArray)
        )
    }

    @Test
    fun `Successfully decodes a String`() {
        val result = Json.decodeFromString(
            DeviceResponseDocumentsSerializer(),
            documentJsonArray
        )

        assertThat(
            result,
            equalTo(documentList)
        )
    }
}
