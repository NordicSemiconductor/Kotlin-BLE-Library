package no.nordicsemi.kotlin.ble.android.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.kotlin.ble.android.sample.scanner.ScannerScreen

@AndroidEntryPoint
class MainActivity : NordicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "menu"
                ) {
                    composable("menu") {
                        MenuScreen(navController)
                    }
                    composable("scanner") {
                        ScannerScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreen(
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(text = "Select a feature to test")

        // Add buttons here
        Button(
            onClick = { navController.navigate("scanner") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Scanner")
        }
    }
}

@Preview
@Composable
fun PreviewMenuScreen() {
    NordicTheme {
        MenuScreen(
            navController = rememberNavController(),
        )
    }
}