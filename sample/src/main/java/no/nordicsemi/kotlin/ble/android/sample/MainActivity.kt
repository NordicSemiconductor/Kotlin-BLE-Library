package no.nordicsemi.kotlin.ble.android.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.common.ui.view.NordicMediumAppBar
import no.nordicsemi.kotlin.ble.android.sample.advertiser.AdvertiserScreen
import no.nordicsemi.kotlin.ble.android.sample.menu.MenuScreen
import no.nordicsemi.kotlin.ble.android.sample.scanner.ScannerScreen

const val NAV_MENU = "Menu"
const val NAV_ADVERTISER = "Advertiser"
const val NAV_SCANNER = "Scanner"

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    topBar = {
                        NordicMediumAppBar(
                            // TODO: Titles should go from strings.xml
                            title = { Text(text = currentRoute ?: NAV_MENU) },
                            showBackButton = currentRoute != NAV_MENU,
                            onNavigationButtonClick = {
                                navController.popBackStack()
                            }
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