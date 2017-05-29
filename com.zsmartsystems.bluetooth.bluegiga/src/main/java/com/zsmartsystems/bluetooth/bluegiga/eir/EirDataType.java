package com.zsmartsystems.bluetooth.bluegiga.eir;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Chris Jackson
 *
 */
public enum EirDataType {
    /**
     * Default unknown value
     */
    UNKNOWN(-1),
    NONE(0),

    EIR_FLAGS(0x01),
    EIR_UUID16_INCOMPLETE(0x02),
    EIR_UUID16_COMPLETE(0x03),
    EIR_UUID32_INCOMPLETE(0x04),
    EIR_UUID32_COMPLETE(0x05),
    EIR_UUID128_INCOMPLETE(0x06),
    EIR_UUID128_COMPLETE(0x07),
    EIR_NAME_SHORT(0x08),
    EIR_NAME_LONG(0x09),
    EIR_TXPOWER(0x0A),
    EIR_DEVICEID(0x10),
    EIR_SLAVEINTERVALRANGE(0x12);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, EirDataType> codeMapping;

    private int key;

    private EirDataType(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<Integer, EirDataType>();
        for (EirDataType s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the type code. Returns null if the code does not exist.
     *
     * @param bluetoothAddressType
     *            the code to lookup
     * @return enumeration value.
     */
    public static EirDataType getEirPacketType(int eirDataType) {
        if (codeMapping == null) {
            initMapping();
        }

        if (codeMapping.get(eirDataType) == null) {
            return UNKNOWN;
        }

        return codeMapping.get(eirDataType);
    }

    /**
     * Returns the Bluetooth protocol defined value for this enum
     *
     * @return the EIR Data type key
     */
    public int getKey() {
        return key;
    }
}
