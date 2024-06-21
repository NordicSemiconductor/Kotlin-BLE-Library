package no.nordicsemi.android.kotlin.ble.app.client.screen.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.SectionTitle
import no.nordicsemi.android.kotlin.ble.app.client.R

@Composable
internal fun ButtonControlView(
    state: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {

            SectionTitle(
                icon = Icons.Default.RadioButtonChecked,
                title = stringResource(id = R.string.blinky_button)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.blinky_button_description),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (state) {
                        stringResource(id = R.string.blinky_button_on)
                    } else {
                        stringResource(id = R.string.blinky_button_off)
                    },
                )
            }
        }
    }
}
