package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne

@RunWith(AndroidJUnit4::class)
@Config(
    shadows = [ShadowLog::class]
)
class ScannedInvalidQrScreenTest {

    @get:Rule
    val errorScreenRule = ScannedInvalidQrScreenRule(createComposeRule())

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
            render(inputUri = exampleUriOne)
            performTryAgainButtonClick()
            assertTryAgainButtonWasClicked()
        }
    }
}
