package uk.gov.onelogin.sharing.holder.error

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.holder.R

@Composable
fun ErrorScreenTemplate(
    titleText: String,
    navigationEvent: NavigationEvent?,
    modifier: Modifier = Modifier,
    body: (LazyListScope.(Dp) -> Unit) = { _ -> },
    onGoBackToDocumentsClick: () -> Unit = {},
    onUpdateNavigationEvent: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val latestOnGoBackToDocumentsClick by rememberUpdatedState(onGoBackToDocumentsClick)
    val mainScope = rememberCoroutineScope { Dispatchers.Main }
    val onBackPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
    val scope = rememberCoroutineScope()

    val buttonBehaviour: () -> Unit = {
        scope.launch {
            latestOnGoBackToDocumentsClick()
        }
    }
    val onExitJourney: () -> Unit = {
        mainScope.launch {
            onBackPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    BackHandler(
        navigationEvent == null,
        buttonBehaviour
    )

    HandleNavigationEvent(
        navigationEvent = navigationEvent,
        context = context,
        onUpdateNavigationEvent = onUpdateNavigationEvent,
        onExitJourney = onExitJourney
    )

    ErrorScreen(
        modifier = modifier,
        icon = errorIconContent,
        title = errorTemplateTitle(titleText),
        body = body,
        supportingText = null,
        primaryButton = errorTemplateButton(buttonBehaviour)
    )
}

private fun errorTemplateButton(buttonBehaviour: () -> Unit): @Composable () -> Unit = {
    GdsButton(
        text = stringResource(R.string.holder_generic_error_button),
        buttonType = ButtonTypeV2.Primary(),
        onClick = buttonBehaviour,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun errorTemplateTitle(titleText: String): @Composable (Dp) -> Unit = { horizontalPadding ->
    GdsHeading(
        text = titleText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        textAlign = GdsHeadingAlignment.CenterAligned
    )
}

private val errorIconContent: @Composable (Dp) -> Unit = { horizontalPadding ->
    GdsIcon(
        image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
        contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun HandleNavigationEvent(
    navigationEvent: NavigationEvent?,
    context: Context,
    onUpdateNavigationEvent: (String) -> Unit,
    onExitJourney: () -> Unit
) {
    val latestOnExitJourney by rememberUpdatedState(onExitJourney)
    val latestOnUpdateNavigationEvent by rememberUpdatedState(onUpdateNavigationEvent)

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.ExitJourney -> {
                R.string.holder_generic_error_button
                    .let(context::getEnglishString)
                    .let(latestOnUpdateNavigationEvent)
                latestOnExitJourney()
            }

            else -> {
                // do nothing with null events
            }
        }
    }
}

internal fun LazyListScope.createErrorBodyText(@StringRes resourceId: Int, padding: Dp) {
    item {
        Text(
            stringResource(resourceId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padding),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
