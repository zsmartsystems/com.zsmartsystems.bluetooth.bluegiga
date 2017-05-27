package com.zsmartsystems.bluetooth.bluegiga.console;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zsmartsystems.bluetooth.bluegiga.BlueGigaSerialHandler;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * Simple console used as an example and test application.
 *
 * @author Chris Jackson
 */
public class BlueGigaConsoleMain {
    /**
     * The {@link Logger}.
     */
    private final static Logger logger = LoggerFactory.getLogger(BlueGigaConsoleMain.class);
    /**
     * The usage.
     */
    public static final String USAGE = "Syntax: java -jar bluegiga-console.jar SERIALPORT SERIALBAUD";

    /**
     * The portName portName.
     */
    private static SerialPort serialPort;

    /**
     * The portName portName input stream.
     */
    private static InputStream inputStream;

    /**
     * The portName portName output stream.
     */
    private static OutputStream outputStream;

    /**
     * Private constructor to disable constructing main class.
     */
    private BlueGigaConsoleMain() {
    }

    /**
     * The main method.
     *
     * @param args
     *            the command arguments
     */
    public static void main(final String[] args) {
        DOMConfigurator.configure("./log4j.xml");

        final String serialPortName;
        final int serialBaud;

        serialPortName = args[0];
        serialBaud = Integer.parseInt(args[1]);

        openSerialPort(serialPortName, serialBaud);

        // final ZigBeePort serialPort = new SerialPortImpl(serialPortName,
        // serialBaud);

        System.out.println("Initialising console...");

        BlueGigaSerialHandler handler = new BlueGigaSerialHandler(inputStream, outputStream);

        final BlueGigaConsole console = new BlueGigaConsole(handler);

        console.start();

        closeSerialPort();
    }

    /**
     * Opens serial port.
     * 
     * @param portName the port name
     * @param baudRate the baud rate
     */
    private static void openSerialPort(String portName, int baudRate) {
        if (serialPort != null) {
            throw new RuntimeException("Serial port already open.");
        }

        serialPort = new SerialPort(portName);
        try {
            serialPort.openPort();
            serialPort.setParams(baudRate, 8, 1, 0);
            serialPort.setFlowControlMode(jssc.SerialPort.FLOWCONTROL_RTSCTS_OUT); // FLOWCONTROL_NONE);
        } catch (SerialPortException e) {
            logger.error("Error opening serial port.", e);
            throw new RuntimeException("Failed to open serial port: " + portName, e);
        }

        inputStream = new UnsignedByteSerialInputStream(serialPort);
        outputStream = new SerialOutputStream(serialPort);
    }

    private static void closeSerialPort() {
        try {
            if (serialPort != null) {
                try {
                    while (inputStream.available() > 0) {
                        try {
                            Thread.sleep(100);
                        } catch (final InterruptedException e) {
                            logger.warn("Interrupted while waiting input stream to flush.");
                        }
                    }
                } catch (Exception e) {
                    logger.trace("Exception in reading from serial port.", e);
                }
                inputStream.close();
                outputStream.flush();
                outputStream.close();
                serialPort.closePort();
                logger.info("Serial portName '" + serialPort.getPortName() + "' closed.");
                serialPort = null;
                inputStream = null;
                outputStream = null;
            }
        } catch (Exception e) {
            logger.warn("Error closing portName portName: '" + serialPort.getPortName() + "'", e);
        }
    }
}
