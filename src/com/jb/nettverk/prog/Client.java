package com.jb.nettverk.prog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        if(args.length > 0 && (args[0].equals("--help") || args[0].equals("-h")))  {
            System.out.println("You need to run 'java .\\Main.java [IP] [PORT] ({OPTIONAL}[BOT])");
            return;
        }
        boolean bot = args.length == 3;
        BotClient botClient = new BotClient();
        try {
            botClient.startConnection(args[0], Integer.parseInt(args[1]));
            System.out.println("Connected");
        }
        catch (Exception e) {
            System.out.println("Could not connect bot, try again.");
            return;
        }
        if(!bot) {
            hostConnection(botClient);
        }
        else botConnection(botClient, args[2]);
    }

    private static void hostConnection(BotClient botClient) {
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.println("Whats your name?");
            String input = in.nextLine();
            if(!input.isEmpty()) {
               try {
                   System.out.printf("Hello %s\n", input);
                   botClient.sendMessage(input);
               }
               catch (Exception e) {
                   System.out.println("Bot client could not send message.");
               }
                break;
            }
        }
        while (true) {
            String input = in.nextLine();
            try {
                if(input.equals("/exit")) {
                    System.out.println("Exiting client.");
                    botClient.sendMessage(input);
                    botClient.stopConnection();
                    return;
                }
                botClient.sendMessage(input);
            }
            catch (IOException e) {
                System.out.println("Could not send message, no connection");
                break;
            }
        }
    }
    private static void botConnection(BotClient botClient, String botName) {
        botClient.connectBot(Bot.getBot(botName));
    }
}

class ClientThread extends Thread {
    private final BufferedReader input;
    private final BotClient client;

    public ClientThread(Socket socket, BotClient client) throws IOException {
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String respons = input.readLine();
                System.out.println(respons);
                client.recieveRespons(respons);
            }
        }
        catch (Exception e) {
            System.out.println("Could not recieve respons from server.");
        } finally {
            try {
                input.close();
            }
            catch (Exception e) {
                System.out.println("Could not close input connection from server.");
            }
        }
    }
}

class BotClient {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Bot bot;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ClientThread clientThread = new ClientThread(clientSocket, this);
        clientThread.start();
    }

    public void recieveRespons(String respons) {
        if(respons.split(": ").length < 2) return;
        if(bot != null && !respons.startsWith("[BOT]")) {
            try {
                sendMessage(bot.getResponse(respons.split(": ")[1]));
            }
            catch (Exception e) {
                System.out.println("Could not send message to server.");
            }
        }
    }

    public void connectBot(Bot bot) {
        this.bot = bot;
        out.printf("[BOT]%s\n",bot.getName());
        System.out.println("Hello " + bot.getName());
    }

    public void sendMessage(String message) throws IOException {
        if(message == null) return;
        out.println(message);
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
}
