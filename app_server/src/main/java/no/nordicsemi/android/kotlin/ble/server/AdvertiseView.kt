package no.nordicsemi.android.kotlin.ble.server

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.android.kotlin.ble.app.server.R

@Composable
fun AdvertiseView(state: ServerState, viewModel: ServerViewModel) {
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                painter = painterResource(id = R.drawable.ic_broadcast),
                title = stringResource(id = R.string.characteristics)
            )

            Spacer(modifier = Modifier.size(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (state.isAdvertising) {
                    painterResource(id = R.drawable.ic_advertisements)
                } else {
                    painterResource(id = R.drawable.ic_advertisements_off)
                }

                val color = if (state.isAdvertising) {
                    colorResource(id = R.color.green)
                } else {
                    colorResource(id = R.color.gray)
                }

                Icon(
                    painter = icon,
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.advertisement_state, state.isAdvertising.toDisplayString()),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.size(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(1f))

                if (state.isAdvertising) {
                    Button(
                        onClick = { viewModel.stopAdvertise() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.stop))
                    }
                } else {
                    Button(
                        onClick = { viewModel.advertise() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(stringResource(id = R.string.advertise))
                    }
                }
            }
        }
    }
}
