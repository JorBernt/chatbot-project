package com.jb.nettverk.prog;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static void main(String[] args) {
        int port;
        try {
            port = Integer.parseInt(args[0]);
        }
        catch (Exception e) {
            System.out.println("Must provide correct port");
            return;
        }
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
            e.printStackTrace();
        }
    }

}

class ServerThread extends Thread {
    private Socket socket;
    private List<ServerThread> threadList;
    private PrintWriter output;
    private String clientName;

    public ServerThread(Socket socket, List<ServerThread> threadList) {
        this.socket = socket;
        this.threadList = threadList;
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            while (true) {
                if(clientName == null) {
                    clientName = input.readLine();
                    continue;
                }
                String outputString = input.readLine();
                if(outputString.equals("exit")) {
                    printToAllClients(clientName, "Has disconnected");
                    break;
                }
                printToAllClients(clientName, outputString);
                System.out.printf("Server recieved message from %s: %s\n",clientName, outputString);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printToAllClients(String clientName, String outputString) {
        for(ServerThread thread : threadList) {
            if(thread.socket.equals(this.socket)) continue;
            thread.output.printf("%s: %s\n", clientName, outputString);
        }
    }
}