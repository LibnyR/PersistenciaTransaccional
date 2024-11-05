package ChatBidireccional;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class clientes {
    private static final String SERVER_ADDRESS = "127.0.0.1";//  mi IP local
    private static final int SERVER_PORT = 4001;// puerto que voy a usar para la conexion 

    public static void main(String[] args) {
    	// aca establezco las conexion con el servidor 
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);
            String username;
            String message;  // mi variable para guardar los mensajes 
            String recipient = ""; // aca voy a guardar el destinatario del mensaje 

            // con esto recibo el  mensaje inicial  desde el servidor y envio el nombre de usuario
            System.out.println(in.readLine());
            username = scanner.nextLine();
            out.println(username);

            // con esto inicio  un hilo y la escucha del servidor
            new Thread(new ServerListener(in)).start();

            // Elegir cliente destino y mensajes a enviar 
            while (true) {
                System.out.println(in.readLine()); // con esto leo los usuarios conectados
                System.out.println("Elija un cliente para chatear:");
                recipient = scanner.nextLine();

                System.out.println("Chateando con " + recipient + ". Escriba la palabra 'chao' si quiere salir del chat.");
                
                // con este ciclo envio los mensajes 
                while (true) {
                    message = scanner.nextLine();
                    if (message.equalsIgnoreCase("chao")) {
                        out.println("chao");
                        break;
                    }
                    scanner.close();
                    
                    // con este print envio los mensajes 
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
            	// con este ciclo leo y muestro los mensajes 
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                
            }
        }
    }
}
