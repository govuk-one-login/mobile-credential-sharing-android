package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import uk.gov.android.ui.componentsv2.topappbar.GdsTopAppBarDefaults.scrollBehaviour
import uk.gov.android.ui.patterns.leftalignedscreen.v3.LeftAlignedScreen
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingTriple
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.putScreenState
import uk.gov.onelogin.sharing.core.performance.JankStatsHelper.rememberMetricsStateHolder
import uk.gov.onelogin.sharing.core.presentation.preview.PreviewContent
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.consent.HolderConsentScreenPreviewParameters
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState
import uk.gov.onelogin.sharing.holder.consent.moreaboutdvs.BodyHelper.moreAboutDvsBodyContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MoreAboutDvsScreen(
    state: HolderConsentUiState.Loaded,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    ),
    onScreenLoad: (String) -> Unit = {},
    onCancelScreen: () -> Unit = {},
    onPrivacyPolicyLinkClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val metrics = rememberMetricsStateHolder()
    val latestOnScreenLoad by rememberUpdatedState(onScreenLoad)

    if (state.shouldShowMoreAboutDvs) {
        LaunchedEffect(Unit) {
            metrics.putScreenState("MoreAboutDvsScreen")
            context.getEnglishString(
                R.string.more_about_dvs_title,
                "DVS"
            ).let(latestOnScreenLoad)
        }

        MoreAboutDvsContent(
            modifier = modifier,
            onCancelScreen = onCancelScreen,
            onPrivacyPolicyLinkClick = onPrivacyPolicyLinkClick,
            sheetState = sheetState,
            state = state
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MoreAboutDvsContent(
    state: HolderConsentUiState.Loaded,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    ),
    onCancelScreen: () -> Unit = {},
    onPrivacyPolicyLinkClick: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val latestOnPrivacyPolicyLinkClick by rememberUpdatedState(onPrivacyPolicyLinkClick)

    ModalBottomSheet(
        onDismissRequest = onCancelScreen,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        shape = RoundedCornerShape(
            topEnd = spacingTriple,
            topStart = spacingTriple
        ),
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = null,
        contentWindowInsets = { WindowInsets.safeDrawing }
    ) {
        TopSheetBar(modifier = Modifier.fillMaxWidth()) {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                onCancelScreen()
            }
        }

        LeftAlignedScreen(
            modifier = Modifier
                .fillMaxSize()
                .testTag("MoreAboutDvsContent"),
            title = stringResource(
                R.string.more_about_dvs_title,
                state.organizationName
            ),
            body = state.organizationName.moreAboutDvsBodyContent(
                onPrivacyPolicyLinkClick = {
                    latestOnPrivacyPolicyLinkClick(state.privacyPolicyUrl)
                }
            )
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopSheetBar(modifier: Modifier = Modifier, handler: () -> Unit = {}) {
    Box(modifier = modifier) {
        DismissScreenButton(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("MoreAboutDvsDismiss"),
            onCancelScreen = handler
        )
        BottomSheetDefaults.DragHandle(
            modifier = Modifier
                .align(Alignment.Center)
                .minimumInteractiveComponentSize()
                .size(minimumTouchTarget)
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DismissScreenButton(modifier: Modifier = Modifier, onCancelScreen: () -> Unit = {}) {
    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(
                onClick = onCancelScreen,
                modifier = Modifier
                    .testTag("closeButton")
                    .minimumInteractiveComponentSize()
                    .width(minimumTouchTarget)
                    .height(minimumTouchTarget)
            ) {
                Icon(
                    painter = painterResource(coreR.drawable.filled_close),
                    contentDescription = "Close",
                    modifier = Modifier.minimumInteractiveComponentSize(),
                    tint = GdsLocalColorScheme.current.topBarIcon
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = GdsLocalColorScheme.current.topBarScrolledBackground
        ),
        scrollBehavior = scrollBehaviour()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewContent
@Composable
internal fun MoreAboutDvsScreenPreview() {
    GdsTheme {
        Surface(modifier = Modifier.fillMaxSize()) {}
        Surface {
            MoreAboutDvsContent(
                sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true
                ),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = spacingDouble)
                    .fillMaxSize(),
                state = HolderConsentScreenPreviewParameters.loadedPreviewState.copy(
                    shouldShowMoreAboutDvs = true
                )
            )
        }
    }
}
