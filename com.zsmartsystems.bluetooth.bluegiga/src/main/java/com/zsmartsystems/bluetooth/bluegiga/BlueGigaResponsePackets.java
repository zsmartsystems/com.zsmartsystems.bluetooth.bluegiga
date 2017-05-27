/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.zsmartsystems.bluetooth.bluegiga;

import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeValueEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeWriteResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaExecuteWriteResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindByTypeValueResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationFoundEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaGroupFoundEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaIndicateConfirmResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaIndicatedEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaPrepareWriteResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaProcedureCompletedEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByGroupTypeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByHandleResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByTypeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadLongResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadMultipleResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadMultipleResponseEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaWriteCommandResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaAttributeStatusEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaReadResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaReadTypeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaSendAttributesResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaUserReadRequestEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaUserReadResponseResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaUserWriteResponseResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaValueEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.attributedb.BlueGigaWriteResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaChannelMapGetResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaConnectionStatusEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectedEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaFeatureIndEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaGetRssiResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaGetStatusResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaUpdateResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaVersionIndEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectDirectResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectSelectiveResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaDiscoverResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaEndProcedureResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaScanResponseEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetAdvDataResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetAdvParametersResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetModeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetScanParametersResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaBondStatusEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaBondingFailEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaDeleteBondingResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaEncryptStartResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaGetBondsResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaPassKeyResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaPasskeyDisplayEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaPasskeyRequestEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaSetBondableModeResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaSetParametersResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaWhitelistBondsResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaBootEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetConnectionsResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetCountersResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetInfoResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaHelloResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaResetResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to create BlueGiga BLE Response and Event packets (ie packets that we will receive).
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
class BlueGigaResponsePackets {

    private static Logger logger = LoggerFactory.getLogger(BlueGigaResponsePackets.class);

    final private static Map<Integer, Class<?>> packetMap = new HashMap<Integer, Class<?>>();

    static {
        packetMap.put(Objects.hash(0x04, 0x05, false), BlueGigaAttributeWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x0A, false), BlueGigaExecuteWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x00, false), BlueGigaFindByTypeValueResponse.class);
        packetMap.put(Objects.hash(0x04, 0x03, false), BlueGigaFindInformationResponse.class);
        packetMap.put(Objects.hash(0x04, 0x07, false), BlueGigaIndicateConfirmResponse.class);
        packetMap.put(Objects.hash(0x04, 0x09, false), BlueGigaPrepareWriteResponse.class);
        packetMap.put(Objects.hash(0x04, 0x01, false), BlueGigaReadByGroupTypeResponse.class);
        packetMap.put(Objects.hash(0x04, 0x04, false), BlueGigaReadByHandleResponse.class);
        packetMap.put(Objects.hash(0x04, 0x02, false), BlueGigaReadByTypeResponse.class);
        packetMap.put(Objects.hash(0x04, 0x08, false), BlueGigaReadLongResponse.class);
        packetMap.put(Objects.hash(0x04, 0x0B, false), BlueGigaReadMultipleResponse.class);
        packetMap.put(Objects.hash(0x04, 0x06, false), BlueGigaWriteCommandResponse.class);
        packetMap.put(Objects.hash(0x04, 0x01, true), BlueGigaProcedureCompletedEvent.class);
        packetMap.put(Objects.hash(0x04, 0x05, true), BlueGigaAttributeValueEvent.class);
        packetMap.put(Objects.hash(0x04, 0x05, true), BlueGigaFindInformationFoundEvent.class);
        packetMap.put(Objects.hash(0x04, 0x02, true), BlueGigaGroupFoundEvent.class);
        packetMap.put(Objects.hash(0x04, 0x00, true), BlueGigaIndicatedEvent.class);
        packetMap.put(Objects.hash(0x04, 0x00, true), BlueGigaReadMultipleResponseEvent.class);
        packetMap.put(Objects.hash(0x02, 0x01, false), BlueGigaReadResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, false), BlueGigaReadTypeResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, false), BlueGigaSendAttributesResponse.class);
        packetMap.put(Objects.hash(0x02, 0x03, false), BlueGigaUserReadResponseResponse.class);
        packetMap.put(Objects.hash(0x02, 0x04, false), BlueGigaUserWriteResponseResponse.class);
        packetMap.put(Objects.hash(0x02, 0x00, false), BlueGigaWriteResponse.class);
        packetMap.put(Objects.hash(0x02, 0x02, true), BlueGigaAttributeStatusEvent.class);
        packetMap.put(Objects.hash(0x02, 0x01, true), BlueGigaUserReadRequestEvent.class);
        packetMap.put(Objects.hash(0x02, 0x00, true), BlueGigaValueEvent.class);
        packetMap.put(Objects.hash(0x03, 0x04, false), BlueGigaChannelMapGetResponse.class);
        packetMap.put(Objects.hash(0x03, 0x00, false), BlueGigaDisconnectResponse.class);
        packetMap.put(Objects.hash(0x03, 0x00, false), BlueGigaGetRssiResponse.class);
        packetMap.put(Objects.hash(0x03, 0x07, false), BlueGigaGetStatusResponse.class);
        packetMap.put(Objects.hash(0x03, 0x02, false), BlueGigaUpdateResponse.class);
        packetMap.put(Objects.hash(0x03, 0x04, true), BlueGigaDisconnectedEvent.class);
        packetMap.put(Objects.hash(0x03, 0x02, true), BlueGigaFeatureIndEvent.class);
        packetMap.put(Objects.hash(0x03, 0x00, true), BlueGigaConnectionStatusEvent.class);
        packetMap.put(Objects.hash(0x03, 0x01, true), BlueGigaVersionIndEvent.class);
        packetMap.put(Objects.hash(0x06, 0x07, false), BlueGigaSetScanParametersResponse.class);
        packetMap.put(Objects.hash(0x06, 0x03, false), BlueGigaConnectDirectResponse.class);
        packetMap.put(Objects.hash(0x06, 0x05, false), BlueGigaConnectSelectiveResponse.class);
        packetMap.put(Objects.hash(0x06, 0x02, false), BlueGigaDiscoverResponse.class);
        packetMap.put(Objects.hash(0x06, 0x08, false), BlueGigaSetAdvParametersResponse.class);
        packetMap.put(Objects.hash(0x06, 0x09, false), BlueGigaSetAdvDataResponse.class);
        packetMap.put(Objects.hash(0x06, 0x04, false), BlueGigaEndProcedureResponse.class);
        packetMap.put(Objects.hash(0x06, 0x01, false), BlueGigaSetModeResponse.class);
        packetMap.put(Objects.hash(0x06, 0x00, true), BlueGigaScanResponseEvent.class);
        packetMap.put(Objects.hash(0x05, 0x02, false), BlueGigaDeleteBondingResponse.class);
        packetMap.put(Objects.hash(0x05, 0x00, false), BlueGigaEncryptStartResponse.class);
        packetMap.put(Objects.hash(0x05, 0x05, false), BlueGigaGetBondsResponse.class);
        packetMap.put(Objects.hash(0x05, 0x04, false), BlueGigaPassKeyResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, false), BlueGigaSetBondableModeResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, false), BlueGigaSetParametersResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, false), BlueGigaWhitelistBondsResponse.class);
        packetMap.put(Objects.hash(0x05, 0x01, true), BlueGigaBondingFailEvent.class);
        packetMap.put(Objects.hash(0x05, 0x04, true), BlueGigaBondStatusEvent.class);
        packetMap.put(Objects.hash(0x05, 0x02, true), BlueGigaPasskeyDisplayEvent.class);
        packetMap.put(Objects.hash(0x05, 0x03, true), BlueGigaPasskeyRequestEvent.class);
        packetMap.put(Objects.hash(0x00, 0x02, false), BlueGigaAddressGetResponse.class);
        packetMap.put(Objects.hash(0x00, 0x01, false), BlueGigaHelloResponse.class);
        packetMap.put(Objects.hash(0x00, 0x00, false), BlueGigaResetResponse.class);
        packetMap.put(Objects.hash(0x00, 0x06, false), BlueGigaGetConnectionsResponse.class);
        packetMap.put(Objects.hash(0x00, 0x05, false), BlueGigaGetCountersResponse.class);
        packetMap.put(Objects.hash(0x00, 0x08, false), BlueGigaGetInfoResponse.class);
        packetMap.put(Objects.hash(0x00, 0x00, true), BlueGigaBootEvent.class);
    }

    public static BlueGigaResponse getPacket(int[] data) {
        int cmdClass = data[2];
        int cmdMethod = data[3];
        boolean isEvent = (data[0] & 0x80) != 0;

    	Class<?> bleClass = packetMap.get(Objects.hash(cmdClass, cmdMethod, isEvent));

    	if (bleClass == null) {
    		return null;
    	}

    	Constructor<?> ctor;

    	try {
    		ctor = bleClass.getConstructor(int[].class);
    		BlueGigaResponse bleFrame = (BlueGigaResponse) ctor.newInstance(data);
    		return bleFrame;
    	} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
    			| IllegalArgumentException | InvocationTargetException e) {
    		logger.error("Error instantiating BLE class", e);
    	}

        return null;
    }
}
