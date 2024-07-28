package ru.otus.java.basic.chat.hw25.server_v2;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8189).start();
    }
}
