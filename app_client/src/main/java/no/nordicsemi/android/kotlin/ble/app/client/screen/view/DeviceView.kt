package no.nordicsemi.android.kotlin.ble.app.client.screen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.android.kotlin.ble.app.client.R
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

@Composable
fun DeviceView(device: ServerDevice) {
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                icon = Icons.Default.DeviceHub,
                title = stringResource(id = R.string.device)
            )

            Spacer(modifier = Modifier.size(8.dp))

            device.name?.let {
                Text(text = stringResource(id = R.string.device_name, it))

                Spacer(modifier = Modifier.size(4.dp))
            }


            Text(text = stringResource(id = R.string.address, device.address))
        }
    }
}
