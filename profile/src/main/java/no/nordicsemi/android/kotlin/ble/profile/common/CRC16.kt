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

package no.nordicsemi.android.kotlin.ble.profile.common


/**
 * CRC-16 class is a helper that calculates different types of CRC.
 * Catalogue of CRC-16 algorithms:
 * [http://reveng.sourceforge.net/crc-catalogue/16.htm](http://reveng.sourceforge.net/crc-catalogue/16.htm)
 *
 * Testing is based on 'check' from the link above and
 * [https://www.lammertbies.nl/comm/info/crc-calculation.html](https://www.lammertbies.nl/comm/info/crc-calculation.html).
 */
object CRC16 {
    /**
     * Calculates CRC CCITT (Kermit) over given range of bytes from the block of data.
     * It is using the 0x1021 polynomial and 0x0000 initial value.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.kermit
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 CCITT (Kermit).
     */
    fun CCITT_Kermit(data: ByteArray, offset: Int, length: Int): Int {
        return CRC(0x1021, 0x0000, data, offset, length, true, true, 0x0000)
    }

    /**
     * Calculates CRC CCITT-FALSE over given range of bytes from the block of data.
     * It is using the 0x1021 polynomial and 0xFFFF initial value.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-ccitt-false
     * See: http://srecord.sourceforge.net/crc16-ccitt.html
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 CCITT-FALSE.
     */
    fun CCITT_FALSE(data: ByteArray, offset: Int, length: Int): Int {
//		Other implementation of the same algorithm:
//		int crc = 0xFFFF;
//
//		for (int i = offset; i < offset + length && i < data.length; ++i) {
//			crc = (((crc & 0xFFFF) >> 8) | (crc << 8));
//			crc ^= data[i];
//			crc ^= (crc & 0xFF) >> 4;
//			crc ^= (crc << 8) << 4;
//			crc ^= ((crc & 0xFF) << 4) << 1;
//		}
        return CRC(0x1021, 0xFFFF, data, offset, length, false, false, 0x0000)
    }

    /**
     * Calculates CRC MCRF4XX over given range of bytes from the block of data.
     * It is using the 0x1021 polynomial and 0xFFFF initial value.
     *
     *
     * This method is used in Bluetooth LE CGMS service E2E-CRC calculation.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-mcrf4xx<br></br>
     * See: http://ww1.microchip.com/downloads/en/AppNotes/00752a.pdf<br></br>
     * See: https://www.bluetooth.com/specifications/gatt -> CGMS (1.0.1)
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 MCRF4XX.
     */
    fun MCRF4XX(data: ByteArray, offset: Int, length: Int): Int {
        return CRC(0x1021, 0xFFFF, data, offset, length, true, true, 0x0000)
    }

    /**
     * Calculates CRC AUG-CCITT over given range of bytes from the block of data.
     * It is using the 0x1021 polynomial and 0x1D0F initial value.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-aug-ccitt
     * See: http://srecord.sourceforge.net/crc16-ccitt.html
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 AUG-CCITT.
     */
    fun AUG_CCITT(data: ByteArray, offset: Int, length: Int): Int {
        return CRC(0x1021, 0x1D0F, data, offset, length, false, false, 0x0000)
    }

    /**
     * Calculates CRC-16 ARC over given range of bytes from the block of data.
     * It is using the 0x8005 polynomial and 0x0000 initial value.
     *
     *
     * Input data and output CRC are reversed.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-arc
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16.
     */
    fun ARC(data: ByteArray, offset: Int, length: Int): Int {
        return CRC(0x8005, 0x0000, data, offset, length, true, true, 0x0000)
    }

    /**
     * Calculates CRC-16 MAXIM over given range of bytes from the block of data.
     * It is using the 0x8005 polynomial and 0x0000 initial value and XORs output with 0xFFFF.
     *
     *
     * Input data and output CRC are reversed.
     *
     *
     * See: http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-maxim
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 MAXIM.
     */
    fun MAXIM(data: ByteArray, offset: Int, length: Int): Int {
        return CRC(0x8005, 0x0000, data, offset, length, true, true, 0xFFFF)
    }

    /**
     * Calculates the CRC over given range of bytes from the block of data with given polynomial and initial value.
     * This method may also reverse input bytes and reverse output CRC.
     *
     * See: http://www.zorc.breitbandkatze.de/crc.html
     *
     * @param poly   Polynomial used to calculate the CRC16.
     * @param init   Initial value to feed the buffer.
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @param refin  True if the input data should be reversed.
     * @param refout True if the output data should be reversed.
     * @return CRC calculated with given parameters.
     */
    fun CRC(
        poly: Int,
        init: Int,
        data: ByteArray,
        offset: Int,
        length: Int,
        refin: Boolean,
        refout: Boolean,
        xorout: Int
    ): Int {
        var crc = init
        var i = offset
        while (i < offset + length && i < data.size) {
            val b = data[i]
            for (j in 0..7) {
                val k = if (refin) 7 - j else j
                val bit = (b.toInt() shr (7 - k) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor poly
            }
            ++i
        }
        return if (refout) {
            (Integer.reverse(crc) ushr 16) xor xorout
        } else {
            (crc xor xorout) and 0xFFFF
        }
    }
}
