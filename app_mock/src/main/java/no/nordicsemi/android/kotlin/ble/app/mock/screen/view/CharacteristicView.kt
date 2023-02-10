package no.nordicsemi.android.kotlin.ble.app.mock.screen.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.view.SectionTitle
import no.nordicsemi.android.kotlin.ble.app.mock.R

@Composable
fun CharacteristicView(state: BlinkyViewState, onButtonClick: () -> Unit) {
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(
                icon = Icons.Default.Bluetooth,
                title = stringResource(id = R.string.device)
            )

            Spacer(modifier = Modifier.size(8.dp))

            Text(text = stringResource(id = R.string.led, state.isLedOn))

            Spacer(modifier = Modifier.size(4.dp))

            Text(text = stringResource(id = R.string.button, state.isButtonPressed))

            Spacer(modifier = Modifier.size(4.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onButtonClick() },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(text = stringResource(id = R.string.turn_led))
                }
            }
        }
    }
}
