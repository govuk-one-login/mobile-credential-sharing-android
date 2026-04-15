package uk.gov.onelogin.sharing.testapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun SharingDialog(onCloseFlow: () -> Unit, content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onCloseFlow,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag(SHARING_DIALOG_TAG),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                content()

                IconButton(
                    onClick = onCloseFlow,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .testTag(CLOSE_DIALOG_BUTTON_TAG)
                ) {
                    Icon(
                        painter = painterResource(
                            android.R.drawable.ic_menu_close_clear_cancel
                        ),
                        contentDescription = "Close"
                    )
                }
            }
        }
    }
}
