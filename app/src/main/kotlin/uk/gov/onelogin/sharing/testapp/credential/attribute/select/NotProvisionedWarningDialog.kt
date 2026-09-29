package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.android.ui.theme.spacingSingle
import uk.gov.onelogin.sharing.testapp.R
import uk.gov.onelogin.sharing.testapp.READER_AUTH_WARNING_TAG

/**
 * Warns the user that the selected reader-auth certificate is an unprovisioned
 * placeholder because the build did not go through the deployment pipeline.
 *
 * Uses GDS components so that it renders with the GOV.UK Design System palette
 * rather than the Material 3 baseline colours.
 */
@Composable
internal fun NotProvisionedWarningDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(spacingDouble),
            color = GdsLocalColorScheme.current.dialogBackground,
            modifier = Modifier.testTag(READER_AUTH_WARNING_TAG)
        ) {
            Column(
                modifier = Modifier.padding(spacingDouble),
                verticalArrangement = Arrangement.spacedBy(spacingSingle)
            ) {
                Text(
                    text = stringResource(R.string.reader_auth_not_provisioned_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(stringResource(R.string.reader_auth_not_provisioned_warning))
                GdsButton(
                    text = stringResource(android.R.string.ok),
                    buttonType = ButtonTypeV2.Primary(),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
