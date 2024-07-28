package ru.otus.java.basic.chat.hw25.server_v2.src.main.java.ru.otus.study.chat;

public interface AuthenticationProvider {
    void initialize();
    boolean authenticate(ClientHandler clientHandler, String login, String password);
    boolean registration(ClientHandler clientHandler, String login, String password, String username);
}
