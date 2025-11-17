package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute

@RunWith(AndroidJUnit4::class)
class AppNavHostTest {
    @get:Rule
    val navHostRule = AppNavHostRule(createComposeRule())

    @Test
    fun holderStartDestination() = runTest {
        navHostRule.render(HolderHomeRoute)
        navHostRule.assertCurrentRoute(HolderHomeRoute::class)
    }

    @Test
    fun verifierStartDestination() = runTest {
        navHostRule.render(VerifierScanRoute)
        navHostRule.assertCurrentRoute(VerifierScanRoute::class)
    }

    @Test
    fun controllerHandlesNavigation() = runTest {
        navHostRule.run {
            render(HolderHomeRoute)
            navigate(VerifierScanRoute)
            assertCurrentRoute(VerifierScanRoute::class)
        }
    }
}
