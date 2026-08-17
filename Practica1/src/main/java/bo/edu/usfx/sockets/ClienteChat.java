/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.*;
import java.net.*;

/**
 *
 * @author gabriel
 */

public class ClienteChat {
    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "localhost";
        int puerto = 5000;

        Socket socket = new Socket(host, puerto);
        System.out.println("Conectado al chat. Puerto local: " + socket.getLocalPort());

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        BufferedReader teclado = new BufferedReader(
                new InputStreamReader(System.in));

        // HILO RECEPTOR: Escucha al servidor en segundo plano
        Thread receptor = new Thread(() -> {
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    System.out.println(" " + s);
                }
            } catch (IOException e) {
                System.out.println("Conexion terminada");
            }
        }, "hilo-receptor");

        receptor.setDaemon(true); // Permite que la JVM termine si se cierra el hilo principal
        receptor.start();

        // HILO PRINCIPAL: Lee del teclado y envía al servidor
        String texto;
        while ((texto = teclado.readLine()) != null) {
            out.println(texto);
        }

        socket.close();
    }
}