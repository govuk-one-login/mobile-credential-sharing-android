package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import uk.gov.android.ui.theme.m3.GdsTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel = MainActivityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            val startDestination = PrimaryTabDestination.Holder
            val currentTab by viewModel.currentTabDestination.collectAsStateWithLifecycle()

            GdsTheme {
                MainActivityContent(
                    currentTab = currentTab,
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                    onUpdateTabDestination = viewModel::update
                )
            }
        }
    }
}
