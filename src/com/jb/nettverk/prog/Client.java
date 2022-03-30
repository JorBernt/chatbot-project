package com.jb.nettverk.prog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        //Checks if --help or -h argument is given.
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            System.out.println("You need to run 'java -cp . com.jb.nettverk.prog.Client [IP] [PORT] ({OPTIONAL}[BOT NAME])'");
            System.out.println("If you don't supply a bot name, you will connect to server with a interactive terminal.");
            System.out.println("The bot names are: ");
            System.out.println("Liam");
            System.out.println("Hannah");
            System.out.println("Sara");
            System.out.println("John");
            return;
        }
        //If its given 3 arguments, the last one must be the bot name.
        boolean bot = args.length == 3;
        ClientConnection clientConnection = new ClientConnection();
        //Tries to connect client to the server.
        try {
            String ip = args[0];
            int port = Integer.parseInt(args[1]);
            clientConnection.startConnection(ip, port);
            System.out.println("Connected");
            System.out.println("Type /exit to quit.");
        } catch (Exception e) {
            System.out.println("Could not connect to server, try again. Use the --help or -h argument to get help.");
            return;
        }

        //Sets up the connection interaction logic, bot or human.
        if (!bot) {
            hostConnection(clientConnection);
        } else botConnection(clientConnection, args[2]);
    }

    //Host interaction logic.
    private static void hostConnection(ClientConnection clientConnection) {
        Scanner in = new Scanner(System.in);
        //Sets the name of the client.
        while (true) {
            System.out.println("Whats your name?");
            String input = in.nextLine();
            if (!input.isEmpty()) {
                System.out.printf("Hello %s\n", input);
                System.out.println("Type your prompt:");
                clientConnection.sendMessage(input);
                break;
            }
        }
        //The messaging loop. Waits for input from user and sends it to the server.
        // If "/exit" is given, disconnects the client from the server.
        while (true) {
            try {
                String input = in.nextLine();
                if (input.equals("/exit")) {
                    System.out.println("Exiting client.");
                    clientConnection.sendMessage(input);
                    System.exit(0);
                    try {
                        clientConnection.stopConnection();
                    } catch (Exception e) {
                        System.out.println("Could not stop the connection, try again.");
                    }
                    return;
                }
                clientConnection.sendMessage(input);
            }
            //If the user quits the program with CTRL-C, tell the server to disconnect the client.
            catch (Exception e) {
                clientConnection.sendMessage("/exit");
            }
        }
    }

    private static void botConnection(ClientConnection clientConnection, String botName) {
        clientConnection.connectBot(Bot.getBot(botName));
    }
}

//The client thread class
class ClientThread extends Thread {
    private final BufferedReader input;
    private final ClientConnection client;

    public ClientThread(Socket socket, ClientConnection client) throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.client = client;
    }

    //This method overrides Java's Thread run method. While its running, its
    //responsible the socket sending and receiving messages, and broadcasting that to the client.
    @Override
    public void run() {
        while (true) {
            try {
                String respons = input.readLine();
                client.recieveResponse(respons);
            } catch (Exception e) {
                break;
            }
        }
        try {
            input.close();
        } catch (Exception e) {
            System.out.println("Could not close input connection from server.");
        }
    }
}

class ClientConnection {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Bot bot;


    //Sets up the client socket, and input and output streams. It also sets up the thread and starts it.
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ClientThread clientThread = new ClientThread(clientSocket, this);
        clientThread.start();
    }

    //Logic for the bots recieving a prompt from the user. If its a respons from another bot, it will not respond.
    public void recieveResponse(String response) {
        System.out.println(response);
        if (response.split(": ").length < 2) return;

        if (bot != null && !response.startsWith("[BOT]") && !response.startsWith("[SERVER]")) {
            sendMessage(bot.getResponse(response.split(": ")[1]));
        }
    }

    //Sends the bot name to the server.
    public void connectBot(Bot bot) {
        this.bot = bot;
        out.printf("[BOT]%s\n", bot.getName());
        System.out.println("Hello " + bot.getName());
    }

    //Sends message to the server.
    public void sendMessage(String message) {
        if (message == null) return;
        if (clientSocket.isClosed()) {
            System.out.println("Client is not connected to server.\nServer is most likely not running.");
            System.exit(0);
        }
        out.println(message);
    }

    //Closes the socket and streams, ends connection.
    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
