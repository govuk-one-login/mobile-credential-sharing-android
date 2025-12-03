package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.security.cbor.decodeDeviceEngagement
import uk.gov.onelogin.sharing.verifier.R

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun ConnectWithHolderDeviceScreen(base64EncodedEngagement: String, modifier: Modifier = Modifier) {
    val engagementData = remember { decodeDeviceEngagement(base64EncodedEngagement) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacingDouble)
    ) {
        item {
            Text(stringResource(R.string.connect_with_holder_heading))
        }
        item {
            Text(base64EncodedEngagement)
        }
        item {
            Text(
                stringResource(
                    R.string.connect_with_holder_permission_state,
                    stringResource(coreR.string.permission_state_denied)
                )
            )
        }

        if (engagementData == null) {
            item {
                Text(stringResource(R.string.connect_with_holder_error_decode))
            }
        } else {
            item {
                Text(stringResource(R.string.connect_with_holder_decoded_data))
            }
            item {
                Text(engagementData.toString())
            }
        }
    }
}

@Composable
@Preview
internal fun ConnectWithHolderDevicePreview(
    @PreviewParameter(ConnectWithHolderDevicePreviewParameters::class)
    base64EncodedEngagement: String,
    modifier: Modifier = Modifier
) {
    GdsTheme {
        ConnectWithHolderDeviceScreen(
            base64EncodedEngagement = base64EncodedEngagement,
            modifier = modifier.background(Color.White)
        )
    }
}
