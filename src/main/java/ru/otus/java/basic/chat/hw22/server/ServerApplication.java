package ru.otus.java.basic.chat.hw22.server;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8189).start();
    }
}
