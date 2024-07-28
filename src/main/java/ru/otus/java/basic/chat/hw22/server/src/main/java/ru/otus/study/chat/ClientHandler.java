package ru.otus.java.basic.chat.hw22.server.src.main.java.ru.otus.study.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String userName;

    private static int usersCount = 0;

    public String getUsername() {
        return userName;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());

        this.userName = "user" + usersCount;
        usersCount++;
        new Thread(() -> {
            try {
                System.out.println("Подключился новый клиент");
                while (true) {
                    String message = in.readUTF();

                    if (message.startsWith("/")) {
                        if (message.equals("/exit")) {
                            sendMessage("/exitok");
                            break;
                        }
                        // обращение к конкретному пользователю.
                        if (message.startsWith("/w")) {
                            String[] outMessage = message.split(" ", 3);
                            System.out.println("Клиент " + this.userName + " прислал сообщение:" + outMessage[2]);
                            server.personalMessage(outMessage[2], outMessage[1],this);
                        }
                        // пришло новое имя
                        if (message.startsWith("/u")) {
                            this.userName = message.substring(2, message.length()).trim();
                            System.out.println("В чат зашел: " + this.userName);
                            message = "В чат зашел: " + this.userName;
                            server.broadcastMessage(userName + ": " + message);
                        }
                        continue;

                    }
                    server.broadcastMessage(userName + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }).start();
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
