package uk.gov.onelogin.sharing.security

import org.junit.Test
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.expectedSessionEstablishmentDto
import kotlin.test.assertEquals

class MapperTest {

    @Test
    fun `mapper should map SessionEstablishmentDto to SessionEstablishment model`() {
        val validSessionEstablishmentDto = expectedSessionEstablishmentDto

        val sessionEstablishmentModel = validSessionEstablishmentDto.toSessionEstablishment()

        assertEquals(expectedSessionEstablishmentDto.data, sessionEstablishmentModel.data)
        assertEquals(expectedSessionEstablishmentDto.eReaderKey.encoded, sessionEstablishmentModel.eReaderKey)
    }
}