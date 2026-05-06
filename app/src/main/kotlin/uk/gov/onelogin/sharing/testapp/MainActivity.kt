package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.metrics.performance.JankStats
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.BuildConfig
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.testapp.MainActivityRoutes.configureTestAppRoutes
import uk.gov.onelogin.sharing.testapp.credential.MockCredentials
import uk.gov.onelogin.sharing.testapp.home.HomeRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var presentCredentialSdk: PresentCredentialSdk

    @Inject
    lateinit var verifyCredentialSdk: VerifyCredentialSdk

    @Inject
    lateinit var logger: Logger

    private lateinit var jankStats: JankStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mockCredentials = MockCredentials.getMockCredentialStates()

        if (BuildConfig.DEBUG) {
            Log.d(
                "Mock Credential",
                mockCredentials.toString()
            )
        }

        setContent {
            val navController = rememberNavController()

            GdsTheme {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.safeContentPadding()
                    ) {
                        configureTestAppRoutes(
                            mockCredentials = mockCredentials,
                            navController = navController,
                            presentCredentialSdk = presentCredentialSdk,
                            verifyCredentialSdk = verifyCredentialSdk
                        )
                    }
                }
            }
        }

        jankStats = JankStats.createAndTrack(window) { frameData ->
            if (frameData.isJank) {
                logger.debug(
                    "JankStats",
                    "Received possible jank: ${frameData.states}"
                )
            }
        }.also {
            logger.debug(
                logTag,
                "Initialised JankStats"
            )
        }
    }

    override fun onPause() {
        super.onPause()
        jankStats.isTrackingEnabled = false
    }

    override fun onResume() {
        super.onResume()
        jankStats.isTrackingEnabled = true
    }
}
