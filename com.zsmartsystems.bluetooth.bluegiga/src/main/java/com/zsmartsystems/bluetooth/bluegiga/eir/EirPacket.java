/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.bluetooth.bluegiga.eir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class processes the Extended Inquiry Response data
 * 
 * @author Chris Jackson
 *
 */
public class EirPacket {
    private List<EirRecord> records = new ArrayList<EirRecord>();

    public EirPacket(int[] data) {
        if (data == null || data.length == 0) {
            return;
        }

        for (int cnt = 0; cnt < data.length;) {
            int[] rawRecord = Arrays.copyOfRange(data, cnt + 1, cnt + data[cnt] + 1);
            EirRecord record = new EirRecord(rawRecord);

            cnt += data[cnt] + 1;

            records.add(record);
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("EirPacket [records=");
        builder.append(records);
        builder.append(']');
        return builder.toString();
    }

}
