/*
 * Copyright (c) 2023, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.kotlin.ble.ui.scanner.view.internal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.R

internal data class ScanFilterState(
    val title: String,
    val selected: Boolean,
    val predicate: (isFilterSelected: Boolean, result: BleScanResults) -> Boolean,
)

@Composable
internal fun FilterView(
    state: List<ScanFilterState>,
    onChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        state.forEachIndexed { index, scanFilter ->
            FilterChip(
                selected = state[index].selected,
                enabled = enabled,
                shape = shape,
                colors = FilterChipDefaults.filterChipColors().copy(
                    labelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = BorderStroke(if (state[index].selected) 0.dp else 1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)),
                onClick = { onChanged(index) },
                label = { Text(text = scanFilter.title) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterViewPreview() {
    MaterialTheme {
        Column {
            FilterView(
                state = listOf(
                    ScanFilterState(
                        title = stringResource(id = R.string.filter_uuid),
                        selected = true,
                        predicate = { selected,_ -> selected },
                    ),
                    ScanFilterState(
                        title = stringResource(id = R.string.filter_nearby),
                        selected = false,
                        predicate = { selected,_ -> selected },
                    ),
                    ScanFilterState(
                        title = stringResource(id = R.string.filter_name),
                        selected = true,
                        predicate = { selected,_ -> selected },
                    ),
                ),
                onChanged = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}