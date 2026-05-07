package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = MainActivityRule(
        composeTestRule = createAndroidComposeRule<MainActivity>()
    )

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun mainActivityShowsContent() {
        composeRule.assertHolderIsDisplayed()
        composeRule.assertVerifierIsDisplayed()
    }
}
