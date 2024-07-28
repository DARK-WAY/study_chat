package ru.otus.java.basic.chat.hw23.server_v1.src.main.java.ru.otus.study.chat;

public class ServerApplication {
    public static void main(String[] args) {
        new Server(8189).start();
    }
}
