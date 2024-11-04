package ChatBidireccional;

import java.io.*;
import java.net.*;
import java.util.*;

public class servidor {

    //private static Set<ClientHandler> clientHandlers = new HashSet<>();
    private static Map<String, ClientHandler> clients = new HashMap<>();

    public static void main(String[] args) {
        int port = 4001;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado y a la espera " );

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Solicito nombre del cliente
                out.println("Ingrese su nombre de usuario:");
                username = in.readLine();

                // Sincroniza a los usuarios que ingresan
                synchronized (clients) {
                    clients.put(username, this);
                    System.out.println("Usuario \"" + username + "\" se ha conectado");
                    broadcastMessage("Usuario \"" + username + "\" se ha conectado");
                    broadcastUserList();
                }

                String message;
                while ((message = in.readLine()) != null) {
                    String[] parts = message.split(":", 2);
                    if (parts.length == 2) {
                        String recipient = parts[0].trim();
                        String msg = parts[1].trim();
                        sendMessage(recipient, msg);
                    } else {
                        if (message.equalsIgnoreCase("chao")) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        // Mensaje al cliente destino
        private void sendMessage(String recipient, String message) {
            ClientHandler client = clients.get(recipient);
            if (client != null) {
                client.out.println("@" + username + ": " + message);
                if (message.equalsIgnoreCase("chao")) {
                    client.out.println("@" + username + " ha cerrado el chat contigo");
                }
            } else {
                out.println("Usuario: " + recipient + ", no está en línea en este momento.");
            }
        }

        // Para visualizar lista de usuarios
        private void broadcastUserList() {
            StringBuilder userList = new StringBuilder("Usuarios conectados actualmente:\n");
            for (String user : clients.keySet()) {
                userList.append("- ").append(user).append("\n");
            }
            for (ClientHandler client : clients.values()) {
                client.out.println(userList.toString());
            }
            System.out.println(userList);
        }

        // Esto me sirve para emitir un mensaje a los clientes
        private void broadcastMessage(String message) {
            for (ClientHandler client : clients.values()) {
                client.out.println(message);
            }
        }

        private void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar la conexión");
            }
            synchronized (clients) {
                clients.remove(username);
                broadcastUserList();
                String disconnectMessage = "El usuario \"" + username + "\" abandonó la sesión.";
                System.out.println(disconnectMessage);
                broadcastMessage(disconnectMessage);
            }
        }
    }
}

