package com.jb.nettverk.prog;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) {
        //Checks if any argument parameter is help, and providing info.
        if(args.length > 0 && (args[0] == "--help" || args[0] == "-h")) {
            System.out.println("You must provide a port as a command line parameter");
            System.out.println("java . com.jb.nettverk.prog.Server [PORT]");
            return;
        }

        //Checks if the given port is correct.
        int port;
        try {
            port = Integer.parseInt(args[0]);
            if(port > 9999 || port < 1) throw new Exception();
        }
        catch (Exception e) {
            System.out.println("Must provide correct port");
            return;
        }
        //Sets up the server socket and thread, and starts it.
        List<ServerThread> threadList = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(port)){
            while (true) {
                Socket socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket, threadList);
                threadList.add(serverThread);
                serverThread.start();
            }
        }
        catch (Exception e) {
            System.out.println("Could not create server socket, try again");
        }
    }

}

class ServerThread extends Thread {
    private final Socket socket;
    private final List<ServerThread> threadList;
    private PrintWriter output;
    private String clientName;

    public ServerThread(Socket socket, List<ServerThread> threadList) {
        this.socket = socket;
        this.threadList = threadList;
    }

    @Override
    public void run() {
        try {
            //Sets up the input and output stream from the socket.
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                //If the connected client doesn't have a name yet, set it.
                if(clientName == null) {
                    clientName = input.readLine();
                    continue;
                }
                //Input from a client.
                String outputString = input.readLine();
                //If the input is "/exit" the client will disconnect.
                if(outputString.equals("/exit")) {
                    printToAllClients(clientName, "Has disconnected");
                    break;
                }
                printToAllClients(clientName, outputString);
                System.out.printf("Server recieved message from %s: %s\n",clientName, outputString);
            }
        }
        catch (Exception e) {
            System.out.println("Server exited");
        }
    }

    private void printToAllClients(String clientName, String outputString) {
        //Runs through all the connected threads, to send the message to each client,
        //except the client who sent the message.
        for(ServerThread thread : threadList) {
            if(thread.socket.equals(this.socket)) continue;
            thread.output.printf("%s: %s\n", clientName, outputString);
        }
    }
}