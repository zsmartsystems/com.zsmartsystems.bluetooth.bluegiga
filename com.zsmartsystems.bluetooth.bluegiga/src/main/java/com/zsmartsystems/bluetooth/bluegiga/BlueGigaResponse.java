package com.zsmartsystems.bluetooth.bluegiga;

import com.zsmartsystems.bluetooth.bluegiga.enumeration.BgApiResponse;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BluetoothAddressType;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.ConnectionStatusFlags;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.ScanResponseType;

public class BlueGigaResponse extends BlueGigaPacket {
    private int[] buffer = new int[131];
    private int position = 0;

    protected BlueGigaResponse(int[] inputBuffer) {
        // TODO Auto-generated constructor stub
        buffer = inputBuffer;
        position = 4;
    }

    /**
     * Reads a int8 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeInt8() {
        return buffer[position++];
    }

    /**
     * Reads a uint8 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeUInt8() {
        return buffer[position++];
    }

    /**
     * Reads a uint16 from the output stream
     *
     * @return value read from input
     */
    protected int deserializeUInt16() {
        return buffer[position++] + (buffer[position++] << 8);
    }

    protected BgApiResponse deserializeBgApiResponse() {
        return BgApiResponse.getBgApiResponse(deserializeUInt16());
    }

    protected ConnectionStatusFlags deserializeConnectionStatusFlags() {
        return ConnectionStatusFlags.getConnectionStatusFlags(deserializeUInt8());
    }

    protected BluetoothAddressType deserializeBluetoothAddressType() {
        return BluetoothAddressType.getBluetoothAddressType(deserializeUInt8());
    }

    protected ScanResponseType deserializeScanResponseType() {
        return ScanResponseType.getScanResponseType(deserializeUInt8());
    }

    protected long deserializeUInt32() {
        return buffer[position++] + (buffer[position++] << 8) + (buffer[position++] << 16) + (buffer[position++] << 24);
    }

    protected int[] deserializeUInt8Array() {
        int length = buffer[position++];
        int[] val = new int[length];

        for (int cnt = 0; cnt < length; cnt++) {
            val[cnt] = deserializeUInt8();
        }

        return val;
    }

    protected String deserializeAddress() {
        StringBuilder builder = new StringBuilder();

        for (int cnt = 5; cnt >= 0; cnt--) {
            if (cnt < 5) {
                builder.append(":");
            }
            builder.append(String.format("%02x", buffer[position + cnt]));
        }
        position += 6;

        return builder.toString();
    }
}
