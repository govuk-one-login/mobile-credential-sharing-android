package uk.gov.onelogin.sharing.testapp

import CredentialSharingAppGraphStub
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

class MainActivityRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    private lateinit var controller: TestNavHostController
    private val lazyColumnTestTag = "menuItems"

    fun assertMenuItem(menuText: String): SemanticsNodeInteraction {
        onNodeWithTag(lazyColumnTestTag)
            .performScrollToNode(hasText(menuText))

        return onNode(
            hasParent(hasTestTag(lazyColumnTestTag)) and hasText(menuText)
        ).assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    fun assertMenuItemsCount(expected: Int) = onAllNodes(
        hasParent(hasTestTag(lazyColumnTestTag))
    ).assertCountEquals(expected)

    fun performHolderTabClick() = onNodeWithText("Holder")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    fun performMenuItemClick(textToClick: String) = assertMenuItem(textToClick).performClick()

    fun performVerifierTabClick() = onNodeWithText("Verifier")
        .assertIsDisplayed()
        .assertHasClickAction()
        .performClick()

    fun render(
        currentTabDestination: PrimaryTabDestination,
        startDestination: Any,
        onUpdateTabDestination: (PrimaryTabDestination) -> Unit = {}
    ) {
        val appGraph = CredentialSharingAppGraphStub(
            ApplicationProvider.getApplicationContext()
        )

        setContent {
            controller = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            MainActivityContentUi(
                currentTab = currentTabDestination,
                onSelectTab = { destination: PrimaryTabDestination ->
                    controller.navigate(destination)
                    onUpdateTabDestination(destination)
                },
                navHost = { hostModifier ->
                    AppNavHost(
                        appGraph = appGraph,
                        startDestination = startDestination,
                        modifier = hostModifier,
                        navController = controller
                    )
                }
            )
        }
    }

    fun renderPreview(currentTabDestination: PrimaryTabDestination) {
        setContent {
            MainActivityContentUiPreview(
                currentTabDestination = currentTabDestination
            )
        }
    }
}
