package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.ui.test.junit4.createComposeRule
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector

@RunWith(RobolectricTestParameterInjector::class)
class SelectCredentialAttributesScreenTest {

    @get:Rule
    val composeTestRule = SelectCredentialAttributesScreenRule(createComposeRule())

    @Test
    fun `Attribute groups are passed to lambda when tapping 'Verify credential' button`(
        @TestParameter option: VerifierAttributeOption
    ) = runTest {
        composeTestRule.run {
            setContent {
                SelectCredentialAttributesScreen(
                    onSelectAttributeGroup = composeTestRule::updateConfirmedAttributeGroup
                )
            }

            performOptionClick(option)
            assertOptionIsSelected(option)
            performVerifyCredentialClick()
            assertConfirmedAttributeGroupEquals(option.attributeGroup)
        }
    }
}
