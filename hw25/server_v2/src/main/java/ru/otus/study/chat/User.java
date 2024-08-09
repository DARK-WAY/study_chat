package ru.otus.java.basic.chat;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final int id;
    private final String login;
    private final String password;
    private final String username;
    private List<RolesUsers> rolesUsers = new ArrayList<>();

    public User(int id, String login, String password, String username) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setRolesUsers(List<RolesUsers> rolesUsers) {
        this.rolesUsers = rolesUsers;
    }
}
