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

import com.zsmartsystems.bluetooth.bluegiga.BlueGigaCommand;
import com.zsmartsystems.bluetooth.bluegiga.BlueGigaSerialHandler;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaDiscoverCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaEndProcedureCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetModeCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.gap.BlueGigaSetScanParametersCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaAddressGetCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetConnectionsCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetCountersCommand;
import com.zsmartsystems.bluetooth.bluegiga.command.system.BlueGigaGetInfoCommand;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapConnectableMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverMode;
import com.zsmartsystems.bluetooth.bluegiga.enumeration.GapDiscoverableMode;

/**
 *
 * @author Chris Jackson
 */
public final class BlueGigaConsole {
    /**
     * The main thread.
     */
    private Thread mainThread = null;

    private BlueGigaSerialHandler bleHandler;

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

        commands.put("info", new GetInfoCommand());
        commands.put("counters", new GetCountersCommand());
        commands.put("connections", new GetConnectionsCommand());
        commands.put("discover", new DiscoverCommand());

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
            bleHandler.sendBleRequestAsync(command);

            command = new BlueGigaAddressGetCommand();
            bleHandler.sendBleRequestAsync(command);

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
            bleHandler.sendBleRequestAsync(command);
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
            return "discover [stop]";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean process(final String[] args, PrintStream out) throws Exception {
            if (args.length == 2 && args[1].toLowerCase().equals("stop")) {
                BlueGigaEndProcedureCommand command = new BlueGigaEndProcedureCommand();
                bleHandler.sendTransaction(command);

                return true;
            }

            BlueGigaSetModeCommand modeCommand = new BlueGigaSetModeCommand();
            modeCommand.setConnect(GapConnectableMode.GAP_DIRECTED_CONNECTABLE);
            modeCommand.setDiscover(GapDiscoverableMode.GAP_GENERAL_DISCOVERABLE);
            bleHandler.sendTransaction(modeCommand);

            BlueGigaSetScanParametersCommand scanCommand = new BlueGigaSetScanParametersCommand();
            scanCommand.setActive(1);
            scanCommand.setScanInterval(0x40);
            scanCommand.setScanWindow(0x30);
            bleHandler.sendTransaction(scanCommand);

            BlueGigaDiscoverCommand discoverCommand = new BlueGigaDiscoverCommand();
            discoverCommand.setMode(GapDiscoverMode.GAP_DISCOVER_GENERIC);
            bleHandler.sendTransaction(discoverCommand);

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
            bleHandler.sendTransaction(command);

            return true;
        }
    }

}
