/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.bluetooth.bluegiga.enumeration;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to implement the BlueGiga Enumeration <b>GapDiscoverableMode</b>.
 * <p>
 * GAP discoverable modes
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public enum GapDiscoverableMode {
    /**
     * Default unknown value
     */
    UNKNOWN(-1),

    /**
     * Non-discoverable mode: the LE Limited Discoverable Mode and the LE General Discoverable
     * Mode bits are NOT set in the AD Flags type. A master can still connect to the advertising slave
     * in this mode.
     */
    GAP_NON_DISCOVERABLE(0x0000),

    /**
     * Discoverable using limited scanning mode: the advertisement packets will carry the LE
     * Limited Discoverable Mode bit set in the Flags AD type.
     */
    GAP_LIMITED_DISCOVERABLE(0x0001),

    /**
     * Discoverable using general scanning mode: the advertisement packets will carry the LE
     * General Discoverable Mode bit set in the Flags AD type.
     */
    GAP_GENERAL_DISCOVERABLE(0x0002),

    /**
     * Same as gap_non_discoverable.
     */
    GAP_BROADCAST(0x0003),

    /**
     * In this advertisement the advertisement and scan response data defined by user will be used.
     * The user is responsible of building the advertisement data so that it also contains the
     * appropriate desired Flags AD type.
     */
    GAP_USER_DATA(0x0004),

    /**
     * When turning the most highest bit on in GAP discoverable mode, the remote devices that send
     * scan request packets to the advertiser are reported back to the application through Scan
     * Response event. This is so called Enhanced Broadcasting mode.
     */
    GAP_ENHANCED_BROADCASTING(0x0080);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, GapDiscoverableMode> codeMapping;

    private int key;

    private GapDiscoverableMode(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<Integer, GapDiscoverableMode>();
        for (GapDiscoverableMode s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the type code. Returns null if the code does not exist.
     *
     * @param i
     *            the code to lookup
     * @return enumeration value.
     */
    public static GapDiscoverableMode getGapDiscoverableMode(int i) {
        if (codeMapping == null) {
            initMapping();
        }

        if (codeMapping.get(i) == null) {
            return UNKNOWN;
        }

        return codeMapping.get(i);
    }

    /**
     * Returns the BlueGiga protocol defined value for this enum
     *
     * @return the BGAPI enumeration key
     */
    public int getKey() {
        return key;
    }
}
