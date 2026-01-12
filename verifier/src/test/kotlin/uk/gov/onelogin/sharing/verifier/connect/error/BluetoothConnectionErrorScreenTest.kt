package uk.gov.onelogin.sharing.verifier.connect.error

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.verifier.R

@RunWith(RobolectricTestParameterInjector::class)
class BluetoothConnectionErrorScreenTest {

    @get:Rule
    val errorScreenRule = BluetoothConnectionErrorScreenRule(createComposeRule())

    private var hasClickedTryAgain = false

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun rendersErrorScreen(
        @TestParameter(valuesProvider = BluetoothConnectionErrorTitlesProvider::class)
        titleString: Int
    ) = runTest {
        errorScreenRule.run {
            render(title = resources.getString(titleString))
            assertErrorIconIsDisplayed()
            assertTitleIsDisplayed()
            assertTryAgainButtonIsDisplayed()
        }
    }

    @Test
    fun tappingTryAgainDefersToProvidedLambda() = runTest {
        errorScreenRule.run {
            render(title = resources.getString(R.string.bluetooth_connection_error_generic)) {
                hasClickedTryAgain = true
            }
            performTryAgainButtonClick()
            assertTryAgainButtonWasClicked()
        }

        testScheduler.advanceUntilIdle()

        assertTrue(hasClickedTryAgain)
    }
}

class BluetoothConnectionErrorTitlesProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<Int> = listOf(
        R.string.bluetooth_connection_error_generic,
        R.string.bluetooth_connection_error_invalid_configuration
    )
}
