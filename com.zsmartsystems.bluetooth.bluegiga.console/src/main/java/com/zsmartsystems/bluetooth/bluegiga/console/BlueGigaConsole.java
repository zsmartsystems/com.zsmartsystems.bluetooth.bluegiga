package com.zsmartsystems.bluetooth.bluegiga.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.zsmartsystems.bluetooth.bluegiga.BlueGigaCommand;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaEventListener;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaResponse;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaSerialHandler;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaAttributeWriteCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaFindInformationCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.attributeclient.BlueGigaReadByGroupTypeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.connection.BlueGigaDisconnectCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectDirectCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaConnectDirectResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaDiscoverCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaEndProcedureCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaScanResponseEvent;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetModeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetScanParametersCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaEncryptStartCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaGetBondsCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.security.BlueGigaSetBondableModeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetResponse;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetConnectionsCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetCountersCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetInfoCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaResetCommand;
import com.zsmartsystems.bluetooth.bluegiga.eir.EirDataType;
import com.zsmartsystems.bluetooth.bluegiga.eir.EirPacket;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BgApiResponse;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.BluetoothAddressType;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapConnectableMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverableMode;

/**
 *
 * @author Chris Jackson
 */
public final class BlueGigaConsole implements BlueGigaEventListener {
    /**
     * The main thread.
     */
    private Thread mainThread = null;

    private BlueGigaSerialHandler bleHandler;

    private Map<String, Map<EirDataType, Object>> deviceMap = new TreeMap<String, Map<EirDataType, Object>>();

    /**
     * The flag reflecting that shutdown is in process.
     */
    private boolean shutdown = false;

    /**
     * Map of registered commands and their implementations.
     */
    private Map<String, ConsoleCommand> commands = new TreeMap<String, ConsoleCommand>();

    /**
     * Constructor which configures ZigBee API and constructs commands.
     * 
     * @param handler
     *
     * @param dongle the dongle
     */
    public BlueGigaConsole(BlueGigaSerialHandler handler) {
        this.bleHandler = handler;

        handler.addEventListener(this);

        commands.put("bonds", new BondsCommand());
        commands.put("connect", new ConnectCommand());
        commands.put("connections", new GetConnectionsCommand());
        commands.put("counters", new GetCountersCommand());
        commands.put("devices", new DevicesCommand());
        commands.put("disconnect", new DisconnectCommand());
        commands.put("discover", new DiscoverCommand());
        commands.put("find", new FindCommand());
        commands.put("groups", new GroupsCommand());
        commands.put("info", new GetInfoCommand());
        commands.put("read", new ReadCommand());
        commands.put("readgroup", new ReadGroupCommand());
        commands.put("reset", new ResetCommand());
        commands.put("stop", new StopCommand());
        commands.put("write", new WriteCommand());

        commands.put("quit", new QuitCommand());
        commands.put("help", new HelpCommand());

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                shutdown = true;
                try {
                    System.in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mainThread.interrupt();
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    /**
     * Starts this console application
     */
    public void start() {
        mainThread = Thread.currentThread();

        print("BlueGiga console ready.", System.out);

        String inputLine;
        while (!shutdown && (inputLine = readLine()) != null) {
            processInputLine(inputLine, System.out);
        }
    }

    /**
     * Processes text input line.
     *
     * @param inputLine the input line
     * @param out the output stream
     */
    public void processInputLine(final String inputLine, final PrintStream out) {
        if (inputLine.length() == 0) {
            return;
        }
        final String[] args = inputLine.split(" ");
        processArgs(args, out);
    }

    /**
     * Processes input arguments.
     *
     * @param args the input arguments
     * @param out the output stream
     */
    public void processArgs(final String[] args, final PrintStream out) {
        try {
            if (commands.containsKey(args[0])) {
                executeCommand(args[0], args, out);
            } else {
                print("Uknown command. Use 'help' command to list available commands.", out);
            }
        } catch (final Exception e) {
            print("Exception in command execution: ", out);
            e.printStackTrace(out);
        }
    }

    /**
     * Executes command.
     *
     * @param zigbeeApi the ZigBee API
     * @param command the command
     * @param args the arguments including the command
     * @param out the output stream
     */
    private void executeCommand(final String command, final String[] args, final PrintStream out) {
        final ConsoleCommand consoleCommand = commands.get(command);
        try {
            if (!consoleCommand.process(args, out)) {
                print(consoleCommand.getSyntax(), out);
            }
        } catch (Exception e) {
            out.println("Error executing command: " + e);
            e.printStackTrace(out);
        }
    }

    /**
     * Prints line to console.
     *
     * @param line the line
     */
    private static void print(final String line, final PrintStream out) {
        out.println("\r" + line);
        // if (out == System.out) {
        // System.out.print("\r> ");
        // }
    }

    /**
     * Reads line from console.
     *
     * @return line readLine from console or null if exception occurred.
     */
    private String readLine() {
        System.out.print("\r> ");
        try {
            final BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            return bufferRead.readLine();
        } catch (final IOException e) {
            return null;
        }
    }

    /**
     * Interface for console commands.
     */
    private interface ConsoleCommand {
        /**
         * Get command description.
         *
         * @return the command description
         */
        String getDescription();

        /**
         * Get command syntax.
         *
         * @return the command syntax
         */
        String getSyntax();

        /**
         * Processes console command.
         *
         * @param zigbeeApi the ZigBee API
         * @param args the command arguments
         * @param out the output PrintStream
         * @return true if command syntax was correct.
         */
        boolean process(final String[] args, PrintStream out) throws Exception;
    }

    /**
     * Quits console.
     */
    private class QuitCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Quits console.";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "quit";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) {
            shutdown = true;
            return true;
        }
    }

    /**
     * Prints help on console.
     */
    private class HelpCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "View command help.";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "help [command]";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) {

            if (args.length == 2) {
                if (commands.containsKey(args[1])) {
                    final ConsoleCommand command = commands.get(args[1]);
                    print(command.getDescription(), out);
                    print("", out);
                    print("Syntax: " + command.getSyntax(), out);
                } else {
                    return false;
                }
            } else if (args.length == 1) {
                final List<String> commandList = new ArrayList<String>(commands.keySet());
                Collections.sort(commandList);
                print("Commands:", out);
                for (final String command : commands.keySet()) {
                    print(command + " - " + commands.get(command).getDescription(), out);
                }
            } else {
                return false;
            }

            return true;
        }
    }

    private class GetInfoCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Get version info about the dongle";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "info";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaCommand command;

            command = new BlueGigaGetInfoCommand();
            bleHandler.sendTransaction(command);

            command = new BlueGigaAddressGetCommand();
            BlueGigaAddressGetResponse response = (BlueGigaAddressGetResponse) bleHandler.sendTransaction(command);

            print("Local address is " + response.getAddress(), System.out);

            return true;
        }
    }

    private class GetCountersCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Get counters from the dongle";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "counters";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaCommand command;

            command = new BlueGigaGetCountersCommand();
            bleHandler.sendTransaction(command);
            return true;
        }
    }

    private class DiscoverCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Discovery";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "discover [ACTIVE | PASSIVE]";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            boolean active = true;

            if (args.length == 2) {
                active = !args[1].toLowerCase().equals("passive");
            }

            print("Starting " + (active ? "ACTIVE" : "PASSIVE") + " scan.", out);

            BlueGigaSetModeCommand modeCommand = new BlueGigaSetModeCommand();
            modeCommand.setConnect(GapConnectableMode.GAP_DIRECTED_CONNECTABLE);
            modeCommand.setDiscover(GapDiscoverableMode.GAP_GENERAL_DISCOVERABLE);
            bleHandler.sendTransaction(modeCommand);

            BlueGigaSetScanParametersCommand scanCommand = new BlueGigaSetScanParametersCommand();
            scanCommand.setActiveScanning(active);
            scanCommand.setScanInterval(0x40);
            scanCommand.setScanWindow(0x8);
            bleHandler.sendTransaction(scanCommand);

            BlueGigaDiscoverCommand discoverCommand = new BlueGigaDiscoverCommand();
            discoverCommand.setMode(GapDiscoverMode.GAP_DISCOVER_OBSERVATION);
            bleHandler.sendTransaction(discoverCommand);

            return true;
        }
    }

    private class StopCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "Stop";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "stop";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaEndProcedureCommand command = new BlueGigaEndProcedureCommand();
            bleHandler.sendTransaction(command);

            return true;
        }
    }

    private class ConnectCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "connect to a device";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "connect address [PUBLIC|RANDOM]";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            // BlueGigaSetParametersCommand secParameters = new BlueGigaSetParametersCommand();
            // secParameters.setMinKeySize(7);
            // secParameters.setRequireMitm(true);
            // secParameters.setIoCapabilities(SmpIoCapabilities.SM_IO_CAPABILITY_NOINPUTNOOUTPUT);
            // bleHandler.sendTransaction(secParameters);

            String address = args[1];
            int connIntervalMin = 60;
            int connIntervalMax = 100;
            int latency = 0;
            int timeout = 100;

            BluetoothAddressType addressType = BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC;
            if (args.length > 2) {
                if (args[2].toLowerCase().equals("public")) {
                    addressType = BluetoothAddressType.GAP_ADDRESS_TYPE_PUBLIC;
                } else if (args[2].toLowerCase().equals("random")) {
                    addressType = BluetoothAddressType.GAP_ADDRESS_TYPE_RANDOM;
                }
            }

            BlueGigaSetModeCommand modeCommand = new BlueGigaSetModeCommand();
            modeCommand.setConnect(GapConnectableMode.GAP_NON_CONNECTABLE);
            modeCommand.setDiscover(GapDiscoverableMode.GAP_NON_DISCOVERABLE);
            bleHandler.sendTransaction(modeCommand);

            BlueGigaConnectDirectCommand connect = new BlueGigaConnectDirectCommand();
            connect.setAddress(address);
            connect.setAddrType(addressType);
            connect.setConnIntervalMin(connIntervalMin);
            connect.setConnIntervalMax(connIntervalMax);
            connect.setLatency(latency);
            connect.setTimeout(timeout);

            BlueGigaConnectDirectResponse connectResponse = (BlueGigaConnectDirectResponse) bleHandler
                    .sendTransaction(connect);
            if (connectResponse.getResult() != BgApiResponse.SUCCESS) {
                return false;
            }

            if (false) {
                BlueGigaSetBondableModeCommand bondMode = new BlueGigaSetBondableModeCommand();
                bondMode.setBondable(true);
                bleHandler.sendTransaction(bondMode);

                // sm_set_parameters(0, 7, sm_io_capability_noinputnooutput)
                // ble_cmd_sm_encrypt_start(connectHandle, 1)

                BlueGigaEncryptStartCommand encryptStart = new BlueGigaEncryptStartCommand();
                encryptStart.setHandle(connectResponse.getConnectionHandle());
                encryptStart.setBonding(true);
                bleHandler.sendTransaction(encryptStart);
            }
            return true;
        }
    }

    private class FindCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "find device info";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "find connection";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            int handle = Integer.parseInt(args[1]);

            BlueGigaFindInformationCommand info = new BlueGigaFindInformationCommand();
            info.setConnection(handle);
            info.setStart(1);
            info.setEnd(65535);
            bleHandler.sendTransaction(info);

            return true;
        }
    }

    private class GroupsCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "get group info";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "groups connection";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            String groupUUID = "00002800-0000-0000-0000-000000000000";
            if (args.length > 2) {
                if (args[2].toLowerCase().startsWith("pri")) {
                    groupUUID = "00002800-0000-0000-0000-000000000000";
                } else if (args[2].toLowerCase().startsWith("sec")) {
                    groupUUID = "00002801-0000-0000-0000-000000000000";
                }
            }

            int connection = Integer.parseInt(args[1]);
            int start = 1;
            int end = 0xffff;

            BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand();
            command.setConnection(connection);
            command.setStart(start);
            command.setEnd(end);
            // command.setUuid(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
            command.setUuid(UUID.fromString(groupUUID));
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class GetConnectionsCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "connections";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "connections";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaCommand command = new BlueGigaGetConnectionsCommand();
            BlueGigaResponse response = bleHandler.sendTransaction(command);

            return true;
        }
    }

    private class ResetCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "reset";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "reset";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaCommand command = new BlueGigaResetCommand();
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class BondsCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "list all bonded devices";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "bonds";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            BlueGigaGetBondsCommand command = new BlueGigaGetBondsCommand();
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class DisconnectCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "disconnect a connection";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "disconnect connection";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            int connection = Integer.parseInt(args[1]);

            BlueGigaDisconnectCommand command = new BlueGigaDisconnectCommand();
            command.setConnection(connection);
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class ReadCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "read";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "read connection start stop";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            int connection = Integer.parseInt(args[1]);
            int start = 1;
            int end = 0xffff;

            BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand();
            command.setConnection(connection);
            command.setStart(start);
            command.setEnd(end);
            // command.setUuid(UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb"));
            command.setUuid(UUID.fromString("00002800-0000-0000-0000-000000000000"));
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class ReadGroupCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "readgroup";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "read connection start stop uuid";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            int connection = Integer.parseInt(args[1]);
            int start = 1;
            int end = 0xffff;

            BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand();
            command.setConnection(connection);
            command.setStart(start);
            command.setEnd(end);
            command.setUuid(UUID.fromString("0000fff1-0000-0000-0000-000000000000"));
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class WriteCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "write an attribute";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "write ";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length < 2) {
                return false;
            }

            int connection = Integer.parseInt(args[1]);

            BlueGigaAttributeWriteCommand command = new BlueGigaAttributeWriteCommand();
            command.setConnection(connection);
            command.setAttHandle(18);
            command.setData(new int[] { '2', '5', '5', ',', '0', ',', '0', ',', '1', '0', '0', ',', ',', ',', ',', ',',
                    ',', ',' });
            bleHandler.queueFrame(command);

            return true;
        }
    }

    private class DevicesCommand implements ConsoleCommand {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDescription() {
            return "List devices heard";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSyntax() {
            return "devices";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            for (String addr : deviceMap.keySet()) {
                print(addr + "  " + deviceMap.get(addr), System.out);
            }
            return true;
        }
    }

    @Override
    public void bluegigaEventReceived(BlueGigaResponse event) {
        print("Event received: " + event, System.out);

        if (event instanceof BlueGigaScanResponseEvent) {
            EirPacket eir = new EirPacket(((BlueGigaScanResponseEvent) event).getData());
            print("              : " + eir, System.out);

            BlueGigaScanResponseEvent scanResponse = (BlueGigaScanResponseEvent) event;

            if (deviceMap.get(scanResponse.getSender()) != null) {
                deviceMap.get(scanResponse.getSender()).putAll(eir.getRecords());
            } else {
                deviceMap.put(scanResponse.getSender(), eir.getRecords());
            }
        }
    }

}
