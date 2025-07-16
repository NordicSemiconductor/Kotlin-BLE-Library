package no.nordicsemi.kotlin.ble.android.sample

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.kotlin.ble.android.sample.advertiser.AdvertiserScreen
import no.nordicsemi.kotlin.ble.android.sample.menu.MenuScreen
import no.nordicsemi.kotlin.ble.android.sample.scanner.ScannerScreen
import no.nordicsemi.kotlin.ble.android.sample.theme.AppTheme

const val NAV_MENU = "Menu"
const val NAV_ADVERTISER = "Advertiser"
const val NAV_SCANNER = "Scanner"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            AppTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    topBar = {
                        MediumTopAppBar(
                            // TODO: Titles should go from strings.xml
                            title = { Text(text = currentRoute ?: NAV_MENU) },
                            navigationIcon = {
                                // Show the home icon only on the main menu
                                if (currentRoute != NAV_MENU) {
                                    IconButton(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                            contentDescription = "Navigate back",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            },
                        )
                    }
                ) { paddings ->
                    NavHost(
                        modifier = Modifier.padding(paddings),
                        navController = navController,
                        startDestination = NAV_MENU
                    ) {
                        composable(NAV_MENU) {
                            MenuScreen(
                                onMenuClicked = { navController.navigate(it) }
                            )
                        }
                        composable(NAV_ADVERTISER) {
                            AdvertiserScreen()
                        }
                        composable(NAV_SCANNER) {
                            ScannerScreen()
                        }
                    }
                }
            }
        }
    }
}