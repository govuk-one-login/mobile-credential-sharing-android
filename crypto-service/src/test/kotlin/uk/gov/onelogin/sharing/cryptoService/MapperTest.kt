package uk.gov.onelogin.sharing.cryptoService

import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.expectedSessionEstablishmentDto

class MapperTest {

    @Test
    fun `mapper should map SessionEstablishmentDto to SessionEstablishment model`() {
        val validSessionEstablishmentDto = expectedSessionEstablishmentDto

        val sessionEstablishmentModel = validSessionEstablishmentDto.toSessionEstablishment()

        assertEquals(expectedSessionEstablishmentDto.data, sessionEstablishmentModel.data)
        assertEquals(
            expectedSessionEstablishmentDto.eReaderKey.encoded,
            sessionEstablishmentModel.eReaderKey
        )
    }
}
