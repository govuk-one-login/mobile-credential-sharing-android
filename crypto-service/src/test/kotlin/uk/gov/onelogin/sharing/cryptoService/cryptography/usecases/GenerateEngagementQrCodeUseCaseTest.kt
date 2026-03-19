package uk.gov.onelogin.sharing.cryptoService.cryptography.usecases

import java.util.UUID
import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.engagement.Engagement
import uk.gov.onelogin.sharing.cryptoService.engagement.FakeEngagementGenerator
import uk.gov.onelogin.sharing.cryptoService.engagement.GenerateEngagementQrCodeUseCase

class GenerateEngagementQrCodeUseCaseTest {
    private val testQrCode = "${Engagement.QR_CODE_SCHEME}TEST_QR"

    private val logger = SystemLogger()
    private val sessionSecurity = FakeSessionSecurity()
    private val fakeEngagementGenerator = FakeEngagementGenerator(
        data = testQrCode
    )

    @Test
    fun `should return qr data as string`() {
        val generateEngagementQrCode = GenerateEngagementQrCodeUseCase(
            logger,
            sessionSecurity,
            fakeEngagementGenerator
        )

        val result = generateEngagementQrCode.generateQrCode(UUID.randomUUID())

        assertEquals(result, testQrCode)
    }
}
