package ChatBidireccional;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class clientes {
    private static final String SERVER_ADDRESS = "127.0.0.1"; // mi IP local
    private static final int SERVER_PORT = 4001; // puerto que voy a usar para la conexión

    public static void main(String[] args) {
        // Establezco la conexión con el servidor
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {
            
            String username;
            String message;  // Variable para guardar los mensajes
            String recipient = ""; // Variable para guardar el destinatario del mensaje

            // Recibo el mensaje inicial desde el servidor y envío el nombre de usuario
            System.out.println(in.readLine());
            username = scanner.nextLine();
            out.println(username);

            // Inicio un hilo para la escucha del servidor
            new Thread(new ServerListener(in)).start();

            // Elegir cliente destino y mensajes a enviar
            while (true) {
                System.out.println(in.readLine()); // Leer los usuarios conectados
                System.out.println("Elija un cliente para chatear:");
                recipient = scanner.nextLine();

                System.out.println("Chateando con " + recipient + ". Escriba la palabra 'chao' si quiere salir del chat.");

                // Enviar mensajes
                while (true) {
                    message = scanner.nextLine();
                    if (message.equalsIgnoreCase("chao")) {
                        out.println("chao");
                        break;
                    }
                    // Enviar el mensaje
                    out.println(recipient + ":" + message);
                }

                if (message.equalsIgnoreCase("chao")) {
                    System.out.println("Has salido del chat.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error en la conexión: " + e.getMessage());
        }
    }

    // Clase interna para escuchar mensajes del servidor en hilo separado
    private static class ServerListener implements Runnable {
        private BufferedReader in;

        public ServerListener(BufferedReader in) {
            this.in = in;
        }

        public void run() {
            String message;
            try {
                // Leer y mostrar los mensajes
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
