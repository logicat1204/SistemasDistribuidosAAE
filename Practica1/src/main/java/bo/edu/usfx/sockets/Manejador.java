/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Manejador implements Runnable {

    private static final Set<Manejador> CLIENTES = new CopyOnWriteArraySet<>();
    private PrintWriter salida;

    private final Socket cliente;
    private final int id;

    public Manejador(Socket cliente, int id) {
        this.cliente = cliente;
        this.id = id;
    }

    @Override
    public void run() {
        String hilo = Thread.currentThread().getName();

        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(cliente.getInputStream()));
             PrintWriter out = new PrintWriter(cliente.getOutputStream(), true)) {

            this.salida = out;
            CLIENTES.add(this);

            out.println("Bienvenido. Le atiende el hilo: " + hilo);

            String linea;
            while ((linea = in.readLine()) != null) {
                System.out.println("[" + hilo + "] cliente " + id + ": " + linea);
                difundir("cliente-" + id + "> " + linea);
            }

        } catch (IOException e) {
            System.err.println("Error con el cliente " + id + ": " + e.getMessage());
        } finally {
            CLIENTES.remove(this);

            try {
                cliente.close();
            } catch (IOException e) {
            }

            System.out.println("Cliente " + id + " desconectado");
        }
    }

    private void difundir(String mensaje) {
        for (Manejador m : CLIENTES) {
            if (m != this && m.salida != null) {
                m.salida.println(mensaje);
            }
        }
    }
}