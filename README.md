

# Overview

This project aims to provide a Library to implement the Blue Giga API written in Java. This provides access to the BlueGiga modules via a serial interface 

The main handler class is ```BlueGigaSerialHandler```. This implements the serial interface and transaction management. It should be instantiated with the constructor ```BlueGigaSerialHandler(final InputStream inputStream, final OutputStream outputStream)```.

Users can send a BlueGiga command with the ```BlueGigaSerialHandler.sendTransaction``` method. This method will return the response frame linked to the command. Alternatively, the ```BlueGigaSerialHandler.sendBleRequestAsync``` method can be used to return a ```Future<BlueGigaResponse>```, or ```BlueGigaSerialHandler.queueFrame``` can be called to simply queue a frame with no transaction management.

Users can subscribe to event notifications by implementing the ```BlueGigaEventListener``` interface and registering for notifications with the ```BlueGigaSerialHandler.addEventListener``` method.

An example console application is provided in the ```com.zsmartsystems.bluetooth.bluegiga.console``` project. This is a little hacky at the moment as it is used as a test application but provides a good reference on the libraries use.


# Contributing

Codacy static testing should pass.
Contributions must be supported with tests.
Contributions must be your own.


# License

The code is licensed under [Eclipse Public License](https://www.eclipse.org/legal/epl-v10.html).
