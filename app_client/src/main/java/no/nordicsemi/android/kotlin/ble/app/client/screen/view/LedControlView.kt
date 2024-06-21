package no.nordicsemi.android.kotlin.ble.app.client.screen.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.android.kotlin.ble.app.client.R

@Composable
internal fun LedControlView(
    isLedOn: Boolean,
    onStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .clickable { onStateChanged(!isLedOn) }
                .padding(16.dp)
        ) {
            val color = if (isLedOn) {
                colorResource(id = no.nordicsemi.android.common.theme.R.color.nordicSun)
            } else {
                colorResource(id = no.nordicsemi.android.common.theme.R.color.nordicDarkGray)
            }

            SectionTitle(
                icon = Icons.Default.Lightbulb,
                title = stringResource(id = R.string.led)
            )

            Spacer(modifier = Modifier.size(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "",
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.led_state, isLedOn.toDisplayString()),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.led_description),
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = isLedOn, onCheckedChange = onStateChanged)
            }
        }
    }
}

@Composable
fun Boolean.toDisplayString(): String {
    return when (this) {
        true -> stringResource(id = R.string.blinky_button_on)
        false -> stringResource(id = R.string.blinky_button_off)
    }
}
