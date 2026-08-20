package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

class ReaderAuthenticationDtoTest {

    private val dto = ReaderAuthenticationDto(
        sessionTranscript = TODO(),
        itemsRequestBytes = TODO()
    )
}
