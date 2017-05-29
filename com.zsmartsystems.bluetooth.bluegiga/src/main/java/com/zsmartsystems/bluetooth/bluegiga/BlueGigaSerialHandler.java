package com.zsmartsystems.bluetooth.bluegiga;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Chris Jackson
 *
 */
public class BlueGigaSerialHandler {
    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(BlueGigaSerialHandler.class);

    private final int BLE_MAX_LENGTH = 64;

    /**
     * The portName portName input stream.
     */
    private InputStream inputStream;

    /**
     * The portName portName output stream.
     */
    private OutputStream outputStream;

    private final Queue<BlueGigaCommand> sendQueue = new LinkedList<BlueGigaCommand>();

    private final Timer timer = new Timer();
    private TimerTask timerTask = null;

    private final int TRANSACTION_TIMEOUT_PERIOD = 50;

    /**
     * The parser parserThread.
     */
    private Thread parserThread = null;

    private ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Transaction listeners are used internally to correlate the commands and responses
     */
    private final List<BleListener> transactionListeners = new ArrayList<BleListener>();

    /**
     * The event listeners will be notified of any asynchronous events
     */
    private final List<BlueGigaEventListener> eventListeners = new ArrayList<BlueGigaEventListener>();

    /**
     * Flag reflecting that parser has been closed and parser parserThread
     * should exit.
     */
    private boolean close = false;

    public BlueGigaSerialHandler(final InputStream inputStream, final OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;

        parserThread = new Thread("BlueGigaBLEHandler") {
            @Override
            public void run() {
                int exceptionCnt = 0;
                logger.trace("BlueGiga BLE thread started");
                int[] inputBuffer = new int[BLE_MAX_LENGTH];
                int inputCount = 0;
                int inputLength = 0;

                while (!close) {
                    try {
                        int val = inputStream.read();
                        // logger.debug("BLE RX: " + String.format("%02X", val));

                        if (inputCount == inputBuffer.length) {
                            // Buffer overrun - shouldn't ever happen and probably means we've lost packet sync!
                        }

                        inputBuffer[inputCount++] = val;
                        if (inputCount == 4) {
                            // Process the header to get the length
                            inputLength = inputBuffer[1] + (inputBuffer[0] & 0x02 << 8) + 4;
                            if (inputLength > 64) {
                                logger.error("BLE length larger than 64 bytes ({})", inputLength);
                            }
                        }
                        if (inputCount == inputLength) {
                            // End of packet reached - process
                            BlueGigaResponse responsePacket = BlueGigaResponsePackets.getPacket(inputBuffer);

                            logger.debug("BLE RX: {}", printHex(inputBuffer, inputLength));
                            logger.debug("BLE RX: {}", responsePacket);
                            if (responsePacket != null) {
                                if (responsePacket.isEvent()) {
                                    notifyEventListeners(responsePacket);
                                } else {
                                    notifyTransactionComplete(responsePacket);
                                }
                            }

                            inputCount = 0;
                        }

                    } catch (final IOException e) {
                        logger.error("BlueGiga BLE IOException: ", e);

                        if (exceptionCnt++ > 10) {
                            logger.error("BlueGiga BLE exception count exceeded");
                            // if (!close) {
                            // frameHandler.error(e);
                            close = true;
                        }
                    }
                }
                logger.debug("BlueGiga BLE exited.");
            }
        };

        parserThread.setDaemon(true);
        parserThread.start();
    }

    /**
     * Requests parser thread to shutdown.
     */
    public void close() {
        this.close = true;
        try {
            parserThread.interrupt();
            parserThread.join();
        } catch (InterruptedException e) {
            logger.warn("Interrupted in packet parser thread shutdown join.");
        }
    }

    /**
     * Checks if parser thread is alive.
     *
     * @return true if parser thread is alive.
     */
    public boolean isAlive() {
        return parserThread != null && parserThread.isAlive();
    }

    // Synchronize this method to ensure a packet gets sent as a block
    private synchronized void sendFrame(BlueGigaCommand bleFrame) {
        // Send the data
        try {
            int[] payload = bleFrame.serialize();
            logger.debug("TX BLE frame: {}", printHex(payload, payload.length));

            // outputStream.write(payload.length);
            for (int b : payload) {
                // result.append(" ");
                // result.append(String.format("%02X", b));
                // logger.debug("BLE TX: " + String.format("%02X", b));
                outputStream.write(b);
            }
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }

        // logger.debug(result.toString());
        logger.debug("--> TX BLE frame: {}", bleFrame);
    }

    // Synchronize this method so we can do the window check without interruption.
    // Otherwise this method could be called twice from different threads that could end up with
    // more than the TX_WINDOW number of frames sent.
    private synchronized void sendNextFrame() {
        // We're not allowed to send if we're not connected
        // if (!stateConnected) {
        // logger.warn("Trying to send when not connected.");
        // return;
        // }

        // Check how many frames are outstanding
        // if (sentQueue.size() >= TX_WINDOW) {
        // logger.debug("Sent queue larger than window [{} > {}].",
        // sentQueue.size(), TX_WINDOW);
        // return;
        // }

        BlueGigaCommand nextFrame = sendQueue.poll();
        if (nextFrame == null) {
            // Nothing to send
            return;
        }

        sendFrame(nextFrame);
    }

    /**
     * Add a {@link BlueGigaCommand} frame to the send queue. The sendQueue is a
     * FIFO queue. This method queues a {@link BlueGigaCommand} frame without
     * waiting for a response.
     *
     * @param transaction
     *            {@link BlueGigaCommand}
     */
    public void queueFrame(BlueGigaCommand request) {
        logger.debug("TX BLE frame: {}", request);

        sendQueue.add(request);

        logger.debug("TX BLE queue: {}", sendQueue.size());

        sendNextFrame();
    }

    /**
     * Notify any transaction listeners when we receive a response.
     *
     * @param response
     *            the response data received
     * @return true if the response was processed
     */
    private boolean notifyTransactionComplete(final BlueGigaResponse response) {
        boolean processed = false;

        // logger.debug("NODE {}: notifyTransactionResponse {}",
        // transaction.getNodeId(), transaction.getTransactionId());
        synchronized (transactionListeners) {
            for (BleListener listener : transactionListeners) {
                if (listener.transactionEvent(response)) {
                    processed = true;
                }
            }
        }

        return processed;
    }

    private void addTransactionListener(BleListener listener) {
        synchronized (transactionListeners) {
            if (transactionListeners.contains(listener)) {
                return;
            }

            transactionListeners.add(listener);
        }
    }

    private void removeTransactionListener(BleListener listener) {
        synchronized (transactionListeners) {
            transactionListeners.remove(listener);
        }
    }

    /**
     * Sends an BlueGiga request without waiting for the response.
     *
     * @param bleCommand
     *            Request {@link BlueGigaCommand}
     * @return response {@link Future} {@link BlueGigaResponse}
     */
    public Future<BlueGigaResponse> sendBleRequestAsync(final BlueGigaCommand bleCommand) {
        class TransactionWaiter implements Callable<BlueGigaResponse>, BleListener {
            private boolean complete = false;
            private BlueGigaResponse response = null;

            @Override
            public BlueGigaResponse call() {
                // Register a listener
                addTransactionListener(this);

                // Send the transaction
                queueFrame(bleCommand);

                // Wait for the transaction to complete
                synchronized (this) {
                    while (!complete) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            complete = true;
                        }
                    }
                }

                // Remove the listener
                removeTransactionListener(this);

                return response;
            }

            @Override
            public boolean transactionEvent(BlueGigaResponse bleResponse) {
                // Check if this response completes our transaction
                if (bleCommand.hashCode() == bleResponse.hashCode()) {
                    return false;
                }

                response = bleResponse;
                complete = true;
                synchronized (this) {
                    notify();
                }

                return true;
            }
        }

        Callable<BlueGigaResponse> worker = new TransactionWaiter();
        return executor.submit(worker);
    }

    /**
     * Sends a {@link BlueGigaCommand} request to the NCP and waits for the response. The response is correlated with
     * the request and the returned {@link BlueGigaResponse} contains the request and response data.
     *
     * @param bleRequest
     *            Request {@link BlueGigaCommand}
     * @return response {@link BlueGigaResponse}
     */
    public BlueGigaResponse sendTransaction(BlueGigaCommand bleCommand) {
        Future<BlueGigaResponse> futureResponse = sendBleRequestAsync(bleCommand);
        if (futureResponse == null) {
            logger.debug("Error sending BLE transaction: Future is null");
            return null;
        }

        try {
            return futureResponse.get();
            // return bleCommand;
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("Error sending BLE transaction to listeners: ", e);
        }

        return null;
    }

    private synchronized void startTransactionTimer() {
        // Stop any existing timer
        resetTransactionTimer();

        // Create the timer task
        timerTask = new TransactionTimer();
        timer.schedule(timerTask, TRANSACTION_TIMEOUT_PERIOD);
    }

    private synchronized void resetTransactionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private class TransactionTimer extends TimerTask {
        // private final Logger logger =
        // LoggerFactory.getLogger(ZWaveTransactionTimer.class);

        @Override
        public void run() {

        }
    }

    /**
     * Notify any transaction listeners when we receive a response.
     *
     * @param response
     *            the response data received
     * @return true if the response was processed
     */
    private void notifyEventListeners(final BlueGigaResponse response) {
        synchronized (eventListeners) {
            for (BlueGigaEventListener listener : eventListeners) {
                listener.bluegigaEventReceived(response);
            }
        }
    }

    public void addEventListener(BlueGigaEventListener listener) {
        synchronized (eventListeners) {
            if (eventListeners.contains(listener)) {
                return;
            }

            eventListeners.add(listener);
        }
    }

    public void removeEventListener(BlueGigaEventListener listener) {
        synchronized (eventListeners) {
            eventListeners.remove(listener);
        }
    }

    private String printHex(int[] data, int len) {
        StringBuilder builder = new StringBuilder();

        for (int cnt = 0; cnt < len; cnt++) {
            builder.append(String.format("%02X ", data[cnt]));
        }

        return builder.toString();
    }

    interface BleListener {
        boolean transactionEvent(BlueGigaResponse bleResponse);
    }
}
