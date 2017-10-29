package com.zsmartsystems.bluetooth.bluegiga;


/**
 * A lisener to track {@link BlueGigaSerialHandler} life cycle events.
 *
 * @author Chris Jackson
 * @author Vlad Kolotov
 */
public interface BlueGigaHandlerListener {

    /**
     * Notifies when the handler gets closed because of the reason specified as an argument.
     * @param reason a reason caused to be closed
     */
    void bluegigaClosed(Exception reason);

}
