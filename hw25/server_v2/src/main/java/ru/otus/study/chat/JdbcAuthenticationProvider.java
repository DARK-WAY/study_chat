package ru.otus.study.chat;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.sql.DriverManager.*;

public class JdbcAuthenticationProvider implements AuthenticationProvider {
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/homework";

    private static final String USERS_QUERY = """
            SELECT u.id, u.login, u.password, u.name
            FROM users u
            """;
    private static final String USER_ROLES_QUERY = """
            select r.name from roles r
            join users_to_roles ur ON r.id = ur.role_id
            WHERE user_id = ?
            ORDER BY id
            """;
    private static final String AUTHENTICATION_QUERY = """
               select u.id, u.login, u.password, u.name
               from users u
               where u.login  = ? 
               and u."password"  = ?
            """;


    private final Server server;
    private List<User> users = new ArrayList<>();

    private final Connection connection;


    public JdbcAuthenticationProvider(Server server) throws SQLException {
        this.server = server;
        connection = getConnection(DATABASE_URL, "postgres", "1");
       // getAll();
    }

    @Override
    public void initialize() {
        System.out.println("Сервис аутентификации запущен: Jdbc режим");
    }

    private String getUsernameByLoginAndPassword(String login, String password) {
        try (PreparedStatement ps = connection.prepareStatement(AUTHENTICATION_QUERY)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    return resultSet.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public synchronized boolean addUser (ClientHandler clientHandler, int userId, String login, String password, String username) {
        User user = new User(userId, login, password, username);
        users.add(user);
        List<RolesUsers> roles = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(USER_ROLES_QUERY)) {
                ps.setInt(1, userId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        roles.add(RolesUsers.valueOf(name));
                    }
                    user.setRolesUsers(roles);
                    clientHandler.setUsername(username);
                    clientHandler.setRolesUsers(roles);
                    server.subscribe(clientHandler);
                    return true;
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
    @Override
    public synchronized boolean authenticate(ClientHandler clientHandler, String login, String password) {
        //String authUsername = getUsernameByLoginAndPassword(login, password);
        String username = null;
        int userId = 0;
        try (PreparedStatement ps = connection.prepareStatement(AUTHENTICATION_QUERY)) {
            ps.setString(1, login);
            ps.setString(2, password);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    username=  resultSet.getString("name");
                    userId = resultSet.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (username == null) {
            clientHandler.sendMessage("Некорректный логин/пароль");
            return false;
        }
        if (server.isUsernameBusy(username)) {
            clientHandler.sendMessage("Указанная учетная запись уже занята");
            return false;
        }
        if( addUser(clientHandler, userId, login, password, username)){
            clientHandler.sendMessage("/regok " + username);
            return true;
        }
        clientHandler.sendMessage("Ошибка подключения.");
        return false;
    }

    //@Override
    public boolean registration(ClientHandler clientHandler, String login, String password, String username) {
        if (login.trim().length() < 3 || password.trim().length() < 6 || username.trim().length() < 1) {
            clientHandler.sendMessage("Логин 3+ символа, Пароль 6+ символов, Имя пользователя 1+ символ");
            return false;
        }
        int userId = 0;
        String runFunction = "{ ? = call registration_user(?, ?, ?)}";

        try (CallableStatement callableStatement = connection.prepareCall(runFunction)) {
            callableStatement.registerOutParameter(1, Types.INTEGER); // output
            callableStatement.setString(2, login); // input
            callableStatement.setString(3, password); // input
            callableStatement.setString(4, username); // input
            callableStatement.executeUpdate(); // run function

            userId = callableStatement.getInt(1);// Get result
            if (userId == -100) {
                clientHandler.sendMessage("Указанный логин уже занят");
                return false;
            }
            if (userId == -200) {
                clientHandler.sendMessage("Указанное имя пользователя уже занято");
                return false;
            }
            if (userId == -1) {
                clientHandler.sendMessage("Ошибка регистрации.");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (userId > 0) {
            if( addUser(clientHandler, userId, login, password, username)){
                clientHandler.sendMessage("/regok " + username);
            }
            clientHandler.sendMessage("Ошибка подключения.");
            return false;
        }
        return userId > 0;
    }

   /* public void getAll() {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery(USERS_QUERY)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    String login = resultSet.getString(2);
                    String password = resultSet.getString(3);
                    String name = resultSet.getString(4);
                    User user = new User(id, login, password, name);
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try (PreparedStatement ps = connection.prepareStatement(USER_ROLES_QUERY)) {
            for (User user : users) {
                ps.setInt(1, user.getId());
                List<RolesUsers> roles = new ArrayList<>();
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        roles.add(RolesUsers.valueOf(name));
                    }
                    user.setRolesUsers(roles);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}

