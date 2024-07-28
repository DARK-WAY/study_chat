package ru.otus.java.basic.chat.hw23.server_v1.src.main.java.ru.otus.study.chat;

public interface AuthenticationProvider {
    void initialize();
    boolean authenticate(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String username);
}