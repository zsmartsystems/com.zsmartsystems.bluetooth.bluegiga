package com.zsmartsystems.bluetooth.bluegiga;

/**
 * 
 * @author Chris Jackson
 *
 */
public interface BlueGigaEventListener {
    /**
     * Called when an event is received
     * 
     * @param event the {@link BlueGigaResponse} just received
     */
    void bluegigaEventReceived(BlueGigaResponse event);
}
