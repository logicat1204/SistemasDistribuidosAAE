package bo.edu.usfx.sockets.parte2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteChatSalas {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int puerto = 5000;

        if (args.length > 0) {
            host = args[0];
        }

        if (args.length > 1) {
            puerto = Integer.parseInt(args[1]);
        }

        Socket socket = new Socket(host, puerto);
        System.out.println("Conectado. Puerto local: " + socket.getLocalPort());

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        BufferedReader teclado = new BufferedReader(
                new InputStreamReader(System.in));

        Thread receptor = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String s;
                    while ((s = in.readLine()) != null) {
                        System.out.println("  " + s);
                    }
                } catch (IOException e) {
                    System.out.println("Conexion terminada");
                }
            }
        }, "hilo-receptor");

        receptor.setDaemon(true);
        receptor.start();

        String texto;
        while ((texto = teclado.readLine()) != null) {
            out.println(texto);

            if (texto.equalsIgnoreCase("/salir")) {
                break;
            }
        }

        socket.close();
    }
}
