package uk.gov.onelogin.sharing.screenshottesting

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * A [TestRule] that applies an underlying Compose test rule only when [enabled] returns `true`.
 *
 * Installing a Compose test rule changes the host the content is laid out in, which affects the
 * captured image size even for previews that never touch the rule. Gating it keeps the default
 * (non-manual-clock) capture path — and therefore its baselines — identical to a world with no
 * Compose rule at all; the rule's host is only created for opt-in manual-clock previews.
 *
 * @param enabled Evaluated at apply time; when `false` the wrapped Compose rule is never applied
 * and the base statement runs unchanged.
 */
class ConditionalComposeRule(
    private val enabled: () -> Boolean
) : TestRule {

    private val composeRule: ComposeContentTestRule = createComposeRule()

    /**
     * The underlying Compose rule, for use by manual-clock captures. Only valid when [enabled] is
     * `true` (i.e. when the rule has actually been applied).
     */
    val delegate: ComposeContentTestRule get() = composeRule

    override fun apply(base: Statement, description: Description): Statement =
        if (enabled()) composeRule.apply(base, description) else base
}
