package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne

@RunWith(AndroidJUnit4::class)
class ScannedInvalidQrScreenTest {

    @get:Rule
    val errorScreenRule = ScannedInvalidQrScreenRule(createComposeRule())

    private var hasClickedTryAgain = false

    @Test
    fun rendersErrorScreen() = runTest {
        errorScreenRule.run {
            render(inputUri = exampleUriOne)
            assertErrorIconIsDisplayed()
            assertTitleIsDisplayed()
            assertTryAgainButtonIsDisplayed()
            assertBodyTextIsDisplayed()
        }
    }

    @Test
    fun tappingTryAgainDefersToProvidedLambda() = runTest {
        errorScreenRule.run {
            render(inputUri = exampleUriOne) { hasClickedTryAgain = true }
            performTryAgainButtonClick()
            assertTryAgainButtonWasClicked()
        }

        testScheduler.advanceUntilIdle()

        assertTrue(hasClickedTryAgain)
    }
}
