package com.zsmartsystems.bluetooth.bluegiga;

/**
 * A generic BlueGiga exception.
 */
public class BlueGigaException extends RuntimeException {

    public BlueGigaException() {
        super();
    }

    public BlueGigaException(String message) {
        super(message);
    }

    public BlueGigaException(String message, Throwable cause) {
        super(message, cause);
    }
}
