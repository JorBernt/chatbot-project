package com.jb.nettverk.prog;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

    public static List<String> helpStrings = List.of(
            "You can kick clients with '/kick [CLIENT_NAME]' command, or '/kick -p [IP]'." +
                    "You can supply multiple clients.",
            "You can list clients with '/list' command.",
            "You can exit the server with /exit command");

    public static void main(String[] args) {
        //Checks if any argument parameter is help, and providing info.
        if (args.length > 0 && (args[0].equals("--help") || args[0].equals("-h"))) {
            System.out.println("You must provide a port as a command line parameter");
            System.out.println("java . com.jb.nettverk.prog.Server [PORT]");
            for (String s : helpStrings) System.out.println(s);
            return;
        }

        //Checks if the given port is correct.
        int port;
        try {
            port = Integer.parseInt(args[0]);
            if (port > 65535 || port < 0) throw new Exception();
        } catch (Exception e) {
            System.out.println("Must provide correct port");
            return;
        }
        List<ServerThread> threadList = new ArrayList<>();

        //Sets up thread for server terminal
        ServerThread st = new ServerThread(threadList);
        st.setName("Server terminal");
        st.start();

        //Sets up the server socket and thread, and starts it.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running on port " + port);
            System.out.println("Type /help to see all commands.");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.printf("Client connected from %s\n", socket.getLocalAddress().getHostAddress());
                //Sets up threads for incoming connections
                ServerThread serverThread = new ServerThread(socket, threadList);
                threadList.add(serverThread);
                serverThread.start();

            }
        } catch (Exception e) {
            System.out.println("Could not create server socket, try again");
        }
    }
}

class ServerThread extends Thread {
    private final Socket socket;
    private final List<ServerThread> threadList;
    private PrintWriter output;
    private BufferedReader input;
    private String clientName;

    public ServerThread(Socket socket, List<ServerThread> threadList) {
        this.socket = socket;
        this.threadList = threadList;
    }

    public ServerThread(List<ServerThread> threadList) {
        this(null, threadList);
    }

    public ServerThread(BufferedReader input, PrintWriter output) {
        this(new ArrayList<>());
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        if (socket == null) {
            startInputReader();
            return;
        }
        try {
            //Sets up the input and output stream from the socket.
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);


            while (true) {
                //If the connected client doesn't have a name yet, set it.
                if (clientName == null) {
                    clientName = input.readLine();
                    System.out.printf("The client (%s) set its name to %s.\n",
                            socket.getLocalAddress().getHostAddress(),
                            clientName);
                    printToAllClients("[SERVER]", clientName + " has connected to the server.");
                    continue;
                }

                //Input from a client.
                String outputString;
                try {
                    outputString = input.readLine();
                    if (outputString.equals("[PING] response")) continue;
                } catch (Exception e) {
                    disconnectClient();
                    break;
                }
                //If the input is "/exit" the client will disconnect.
                if (outputString.equals("/exit")) {
                    disconnectClient();
                    break;
                }

                printToAllClients(clientName, outputString);
                System.out.printf("Server recieved message from %s: %s\n", clientName, outputString);
            }
        } catch (Exception e) {
            close();
            System.out.println(
                    (clientName == null ? socket.getLocalAddress().getHostAddress() : clientName) + " exited.");
        }
    }

    private void ping() {
        output.println();
    }

    private void disconnectClient() {
        ServerThread thread = getThreadByName(clientName);
        if (thread == null) return;
        thread.close();
        System.out.println(clientName + " has disconnected.");
        printToAllClients("[SERVER]", clientName + " has disconnected.");
    }

    private void printToAllClients(String clientName, String outputString) {
        //Runs through all the connected threads, to send the message to each client,
        //except the client who sent the message.
        for (ServerThread thread : threadList) {
            if (thread.socket.equals(this.socket)) continue;
            thread.output.printf("%s: %s\n", clientName, outputString);
        }
    }

    //Handles input on the server terminal
    public void startInputReader() {
        Scanner in = new Scanner(System.in);
        while (true) {
            String input = in.nextLine();

            if (input.startsWith("/kick")) {
                String[] split = input.split(" ");
                if (split.length > 1) {
                    if (split[1].equals("all")) {
                        while (!threadList.isEmpty()) {
                            kick(threadList.get(0));
                        }
                        System.out.println("All clients kicked.");
                    } else {
                        boolean ip = split[1].equals("-p");
                        for (int i = ip ? 2 : 1; i < split.length; i++) {
                            ServerThread thread = ip ? getThreadByIp(split[i]) : getThreadByName(split[i]);
                            if (thread == null) {
                                System.out.println("No client named " + split[i]);
                                continue;
                            }
                            kick(thread);
                        }
                    }
                } else {
                    System.out.println("You must provide name(s).");
                }
            } else if (input.equals("/list")) {
                if (threadList.isEmpty()) {
                    System.out.println("No connected clients");
                } else {
                    System.out.println("All connected clients: ");
                    for (ServerThread thread : threadList) {
                        System.out.printf("%s (%s).\n", thread.clientName,
                                thread.socket.getLocalAddress().getHostAddress());
                    }
                }
            } else if (input.equals("/help")) {
                for (String s : Server.helpStrings) System.out.println(s);
            } else if (input.equals("/exit")) {
                System.out.println("Quitting server");
                System.exit(0);
            } else System.out.println("Unknown command. Type /help to see all commands.");
        }
    }

    private ServerThread getThreadByName(String name) {
        for (ServerThread thread : threadList) {
            if (thread.clientName.equals(name)) return thread;
        }
        return null;
    }

    private ServerThread getThreadByIp(String ip) {
        for (ServerThread thread : threadList) {
            if (thread.socket.getLocalAddress().getHostAddress().equals(ip)) return thread;
        }
        return null;
    }

    //Kicks a client from the server
    public void kick(ServerThread thread) {
        String name = thread.clientName;
        thread.output.println("You are kicked from the server.");
        thread.close();
        printToAllClients("[SERVER]", name + " is kicked from the server.");
        System.out.println(name + " is kicked from the server.");
    }

    //Close thread
    private void close() {
        threadList.remove(this);
        output.close();
    }
}