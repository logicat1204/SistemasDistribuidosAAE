/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author gabriel
 */

public class Usuario {
    private final int id;
    private final Socket socket;
    private final PrintWriter salida;
    private String nickname;
    private Sala salaActual;

    public Usuario(int id, Socket socket, PrintWriter salida) {
        this.id = id;
        this.socket = socket;
        this.salida = salida;
        this.nickname = "usuario_" + id;
    }

    public int getId() { return id; }
    public Socket getSocket() { return socket; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public Sala getSalaActual() { return salaActual; }
    public void setSalaActual(Sala salaActual) { this.salaActual = salaActual; }

    // Sincronizado para evitar interbloqueos/desorden si dos hilos envían un privado al mismo tiempo
    public synchronized void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }
}
