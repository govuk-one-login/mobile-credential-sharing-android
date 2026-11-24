package uk.gov.onelogin.sharing.holder.error

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
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
    val latestOnUpdateNavigationEvent by rememberUpdatedState(onUpdateNavigationEvent)
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

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is NavigationEvent.ExitJourney -> {
                R.string.holder_generic_error_button
                    .let(context::getEnglishString)
                    .let(latestOnUpdateNavigationEvent)
                onExitJourney()
            }

            else -> {
                // do nothing with null events
            }
        }
    }

    ErrorScreen(
        modifier = modifier,
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = titleText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = body,
        supportingText = null,
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.holder_generic_error_button),
                buttonType = ButtonTypeV2.Primary(),
                onClick = buttonBehaviour,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
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
