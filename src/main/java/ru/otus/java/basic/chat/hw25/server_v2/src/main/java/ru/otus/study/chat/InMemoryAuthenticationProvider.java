package ru.otus.java.basic.chat.hw25.server_v2.src.main.java.ru.otus.study.chat;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {
    private class User {
        private String login;
        private String password;
        private String username;
        private RolesUsers rolesUsers;

        public User(String login, String password, String username, RolesUsers rolesUsers ) {
            this.login = login;
            this.password = password;
            this.username = username;
            this.rolesUsers = rolesUsers;
        }
    }

    private Server server;
    private List<User> users;

    public InMemoryAuthenticationProvider(Server server) {
        this.server = server;
        this.users = new ArrayList<>();
        this.users.add(new User("login1", "pass1", "bob", RolesUsers.ADMIN));
        this.users.add(new User("login2", "pass2", "user2", RolesUsers.USER));
        this.users.add(new User("login3", "pass3", "user3", RolesUsers.USER));
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: In-Memory режим");
    }

    private RolesUsers getRoleByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.rolesUsers;
            }
        }
        return null;
    }
    private String getUsernameByLoginAndPassword(String login, String password) {
        for (User u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.username;
            }
        }
        return null;
    }
    private boolean isLoginAlreadyExist(String login) {
        for (User u : users) {
            if (u.login.equals(login)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameAlreadyExist(String username) {
        for (User u : users) {
            if (u.username.equals(username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        String authUsername = getUsernameByLoginAndPassword(login, password);
        if (authUsername == null) {
            clientHandler.sendMessage("Некорретный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(authUsername)) {
            clientHandler.sendMessage("Указанная учетная запись уже занята");
            return false;
        }
        RolesUsers  roleUser = getRoleByLoginAndPassword(login, password);
        clientHandler.setUsername(authUsername);
        clientHandler.setRolesUsers(roleUser);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/authok " + authUsername);
        return true;
    }

    @Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().length() < 1) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        if (isLoginAlreadyExist(login)) {
            clientHandler.sendMessage("Указанный логин уже занят");
            return false;
        }
        if (isUsernameAlreadyExist(username)) {
            clientHandler.sendMessage("Указанное имя пользователя уже занято");
            return false;
        }
        users.add(new User(login, password, username, RolesUsers.USER));
        clientHandler.setUsername(username);
        clientHandler.setRolesUsers(RolesUsers.USER);
        server.subscribe(clientHandler);
        clientHandler.sendMessage("/regok " + username);
        return true;
    }
}