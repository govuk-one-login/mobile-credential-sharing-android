package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.holder.HolderWelcomeText
import uk.gov.onelogin.sharing.holder.QrCodeImage
import uk.gov.onelogin.sharing.holder.engagement.EngagementGenerator
import uk.gov.onelogin.sharing.testapp.ui.theme.TestAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        HolderWelcomeText(modifier = Modifier.padding(innerPadding))
                        val engagement = EngagementGenerator()
                        QrCodeImage(
                            modifier = Modifier,
                            data = "mdoc://${engagement.generateEncodedBase64QrEngagement()}",
                            size = QR_SIZE
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val QR_SIZE = 800
    }
}
