package ChatBidireccional;

import java.io.*;
import java.net.*;
import java.util.*;

public class servidor {

    // con la map puedo mantener la lista de los clientes conectados.
	// con el handler manejo la conexion de cada cliente.
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
    // con el implementacion de Runnable manejo las conexiones en hilos separados. 
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        
        // este es el constructor que toma el socket del cliente
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        // este es el metodo que se inicia cuando se crea un hilo nuevo  
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

        // Esto me sirve para emitir un mensaje a los clientes conectados
        private void broadcastMessage(String message) {
            for (ClientHandler client : clients.values()) {
                client.out.println(message);
            }
        }
        //  con esto cierro la conexion del cliente, y con el synchronized 
        // lo eliminino de la lista y nitifico a los demas clientes.
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

