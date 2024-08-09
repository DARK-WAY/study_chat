package ru.otus.study.chat;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;
    private AuthenticationProvider authenticationProvider;

    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    public Server(int port) throws SQLException {
        this.port = port;
        this.clients = new ArrayList<>();
        //this.authenticationProvider = new InMemoryAuthenticationProvider(this);
        this.authenticationProvider = new JdbcAuthenticationProvider (this);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            authenticationProvider.initialize();
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(this, socket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        broadcastMessage("В чат зашел: " + clientHandler.getUsername());
        clients.add(clientHandler);
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastMessage("Из чата вышел: " + clientHandler.getUsername());
    }

    public synchronized void broadcastMessage(String message) {
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void disableUser(ClientHandler user, String userToDelete) {
        boolean isAdmin = false;
        for(RolesUsers role: user.getRolesUsers()){
            if (role.equals(RolesUsers.ADMIN)){
                isAdmin = true;
                break;
            }
        }
        if(!isAdmin) {
            user.sendMessage("Отключать клиента может только администратор.");
            return;
        }
        for (ClientHandler c : clients) {
            if (c.getUsername().equals(userToDelete)) {
                broadcastMessage("Администратор отключил : " + c.getUsername());
                System.out.println("Сервер отключает клиента " + userToDelete);
                c.disconnect();
                return;
            }
        }
        user.sendMessage("Указанный пользователь не найден.");
    }
}
