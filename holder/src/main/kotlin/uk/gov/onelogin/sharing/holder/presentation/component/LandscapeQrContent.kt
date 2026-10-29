package uk.gov.onelogin.sharing.holder.presentation.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.gov.android.ui.patterns.leftalignedscreen.LeftAlignedScreen
import uk.gov.android.ui.theme.spacingDouble

@Composable
internal fun LandscapeQrContent(qrData: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Row {
            // col 1
            LeftAlignedScreen(
                modifier = Modifier.weight(1f),
                title = { padding ->
                    HolderWelcomeHeading(Modifier.padding(horizontal = padding))
                },
                body = { padding ->
                    item {
                        HolderWelcomeBody(Modifier.padding(horizontal = padding))
                    }
                }
            )
            // col 2
            HolderWelcomeQrCode(
                qrData = qrData,
                modifier = Modifier
                    .padding(top = spacingDouble, end = spacingDouble)
                    .size(250.dp)
            )
        }
    }
}
