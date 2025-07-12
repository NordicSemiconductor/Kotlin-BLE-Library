/*
 * Copyright (c) 2024, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.android.sample.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewRemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewRemoteDescriptor
import no.nordicsemi.kotlin.ble.client.android.preview.PreviewRemoteService
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.util.toShortString
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun DeviceServices(services: List<RemoteService>?) {
    Column {
        Text("Services", style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        services?.forEach { service ->
            Service(service)
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun Service(service: RemoteService) {
    Column(
        modifier = Modifier.indent(12.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = service.uuid.toShortString(),
            style = MaterialTheme.typography.bodySmall
        )
        if (service.characteristics.isNotEmpty()) {
            service.characteristics.forEach { characteristic ->
                Characteristic(characteristic)

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun Characteristic(characteristic: RemoteCharacteristic) {
    Column(
        modifier = Modifier.indent(12.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
    ) {
        Text(
            text = characteristic.uuid.toShortString(),
            style = MaterialTheme.typography.bodySmall
        )

        if (characteristic.descriptors.isNotEmpty()) {
            characteristic.descriptors.forEach { descriptor ->
                Descriptor(descriptor)

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun Descriptor(descriptor: RemoteDescriptor) {
    Text(
        text = descriptor.uuid.toShortString(),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.indent(12.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    )
}

/**
 * A modifier that draws a vertical line on the left side of the content.
 */
@Composable
private fun Modifier.indent(strokeWidth: Dp = 12.dp, color: Color): Modifier {
    val density = LocalDensity.current
    val strokeWidthPx = density.run { strokeWidth.toPx() }

    return this then Modifier
        .drawBehind {
            val margin = strokeWidthPx / 8
            val offset = strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = margin + offset, y = offset),
                end = Offset(x = margin + offset , y = size.height - offset),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round,
            )
        }
        .padding(start = strokeWidth * 1.25f)
}

@Preview(showBackground = true)
@Composable
private fun PreviewDeviceServices() {
    DeviceServices(
        listOf(
            PreviewRemoteService(0x1800) {
                Characteristic(0x2A00, CharacteristicProperty.NOTIFY) {
                    CharacteristicUserDescriptionDescriptor("Example")
                }
                Characteristic(0x2A01)
            },
            PreviewRemoteService(0x1801),
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCharacteristics() {
    Characteristic(
        characteristic = PreviewRemoteCharacteristic(0x2A00) {
            Descriptor(0x2901)
            Descriptor(0x2902)
        }
    )
}

@OptIn(ExperimentalUuidApi::class)
@Preview(showBackground = true)
@Composable
private fun PreviewDescriptor() {
    Descriptor(
        descriptor = PreviewRemoteDescriptor(Uuid.random())
    )
}