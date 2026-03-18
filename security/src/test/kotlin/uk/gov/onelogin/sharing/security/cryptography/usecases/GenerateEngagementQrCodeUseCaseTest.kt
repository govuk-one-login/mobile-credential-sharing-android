package uk.gov.onelogin.sharing.security.cryptography.usecases

import java.util.UUID
import kotlin.test.assertEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator
import uk.gov.onelogin.sharing.security.engagement.GenerateEngagementQrCodeUseCase

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
