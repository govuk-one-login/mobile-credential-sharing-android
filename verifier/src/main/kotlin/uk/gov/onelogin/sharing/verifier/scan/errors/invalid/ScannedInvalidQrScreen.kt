package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingAlignment
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.verifier.R

/**
 * Error screen that's shown to the User when they scan a URL-based QR code that isn't considered
 * to be a digital credential.
 *
 * @param inputUri The URI obtained from the QR Scanner.
 * @param modifier The composable [Modifier] affecting the UI. Defaults to `Modifier`.
 * @param onTryAgainClick The behaviour when clicking the primary [GdsButton].
 */
@Composable
@ImplementationDetail(
    ticket = "DCMAW-16278",
    description = "Invalid QR error handling"
)
@RequiresImplementation(
    details = [
        ImplementationDetail(
            description = "Finalise UI for Invalid QR error screen"
        )
    ]
)
@OptIn(UnstableDesignSystemAPI::class)
fun ScannedInvalidQrScreen(
    inputUri: String,
    modifier: Modifier = Modifier,
    onTryAgainClick: () -> Unit = {}
) {
    ErrorScreen(
        modifier = modifier.fillMaxWidth(),
        icon = { horizontalPadding ->
            GdsIcon(
                image = ImageVector.vectorResource(ErrorScreenIcon.ErrorIcon.icon),
                contentDescription = stringResource(ErrorScreenIcon.ErrorIcon.description),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                color = colorScheme.onBackground
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(R.string.scanned_invalid_qr_title),
                modifier = Modifier
                    .padding(horizontal = horizontalPadding),
                textAlign = GdsHeadingAlignment.CenterAligned
            )
        },
        body = { horizontalPadding ->
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacingDouble),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
                ) {
                    Text(
                        stringResource(R.string.scanned_invalid_qr_body, inputUri),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.scanned_invalid_qr_try_again),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onTryAgainClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    )
}

@Composable
@Preview
internal fun ScannedInvalidQrScreenPreview() {
    GdsTheme {
        ScannedInvalidQrScreen(
            inputUri = "https://scanned.invalid.qr.code"
        )
    }
}
