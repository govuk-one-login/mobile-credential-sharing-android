package uk.gov.onelogin.sharing.holder.qrcode

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Dp
import androidx.test.core.app.ApplicationProvider
import kotlin.collections.forEach
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers
import uk.gov.onelogin.sharing.holder.R

class QrCodeScreenRule(
    composeTestRule: ComposeContentTestRule,
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    private val paragraphsIds = listOf(
        R.string.holder_qr_code_body_1,
        R.string.holder_qr_code_body_2,
        R.string.holder_qr_code_body_3
    )

    fun assertHeadingIsDisplayed() =
        onNodeWithText(resources.getString(R.string.holder_qr_code_heading))
            .assertIsDisplayed()
            .assert(isHeading())

    fun assertQrCodeIsDisplayed() = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    )
        .assertIsDisplayed()
        .assert(SemanticsMatchers.hasRole(Role.Image))

    fun assertQrCodeDoesNotExist() = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    )
        .assertDoesNotExist()

    fun assertQrCodeSizeIs(size: Dp) = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    )
        .assertIsDisplayed()
        .assert(SemanticsMatchers.hasRole(Role.Image))
        .assertWidthIsEqualTo(size)
        .assertHeightIsEqualTo(size)

    fun assertBodyIsDisplayed() = paragraphsIds.forEach {
        onNodeWithText(resources.getString(it))
            .assertIsDisplayed()
    }

    fun assertBodyExists() = paragraphsIds.forEach {
        onNodeWithText(resources.getString(it))
            .assertExists()
    }

    fun scrollToAndAssertBodyIsDisplayed() = paragraphsIds.forEach {
        onNodeWithText(resources.getString(it))
            .performScrollTo()
            .assertIsDisplayed()
    }

    fun assertPortraitLayout() {
        val heading = onNodeWithText(
            resources.getString(R.string.holder_qr_code_heading)
        ).getBoundsInRoot()
        val qrCode = onNodeWithContentDescription(
            resources.getString(R.string.holder_qr_code_alt_text)
        ).getBoundsInRoot()
        val body = onNodeWithText(resources.getString(paragraphsIds.first())).getBoundsInRoot()

        assert(qrCode.top >= heading.bottom) { "QR code should be below heading" }
        assert(body.top >= qrCode.bottom) { "Body should be below QR code" }
    }

    fun assertQrCodeScrollsWithContent() = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    ).assert(hasAnyAncestor(hasScrollAction()))

    fun assertLandscapeLayout() {
        val heading = onNodeWithText(
            resources.getString(R.string.holder_qr_code_heading)
        ).getBoundsInRoot()
        val qrCode = onNodeWithContentDescription(
            resources.getString(R.string.holder_qr_code_alt_text)
        ).getBoundsInRoot()
        val body = onNodeWithText(resources.getString(paragraphsIds.first())).getBoundsInRoot()

        assert(body.top >= heading.bottom) { "Body code should be below heading" }
        assert(qrCode.left >= heading.right) { "QR code should be right of heading" }
        assert(qrCode.left >= body.right) { "QR code should be right of body" }
    }

    fun assertQrCodeIsFixed() = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    ).assert(!hasAnyAncestor(hasScrollAction()))

    fun assertHeadingAndBodyAreScrollable() {
        onNodeWithText(resources.getString(R.string.holder_qr_code_heading))
            .assert(hasAnyAncestor(hasScrollAction()))
        paragraphsIds.forEach {
            onNodeWithText(resources.getString(it))
                .assert(hasAnyAncestor(hasScrollAction()))
        }
    }

    fun waitUntilQrCodeExists() = waitUntil {
        onAllNodesWithContentDescription(
            resources.getString(R.string.holder_qr_code_alt_text)
        ).fetchSemanticsNodes().isNotEmpty()
    }

    fun waitUntilQrCodeDoesNotExist() = waitUntil {
        onAllNodesWithContentDescription(
            resources.getString(R.string.holder_qr_code_alt_text)
        ).fetchSemanticsNodes().isEmpty()
    }

    fun assertQrCodeFitsOnScreen() {
        val screen = onRoot().getUnclippedBoundsInRoot()
        val qrCode = onNodeWithContentDescription(
            resources.getString(R.string.holder_qr_code_alt_text)
        ).getUnclippedBoundsInRoot()

        assert(qrCode.top >= screen.top && qrCode.bottom <= screen.bottom) {
            "QR code does not fit on screen"
        }
    }

    fun scrollToQrCode() = onNodeWithContentDescription(
        resources.getString(R.string.holder_qr_code_alt_text)
    ).performScrollTo()
}
