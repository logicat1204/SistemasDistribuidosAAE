package bo.edu.usfx.sockets;

import java.io.PrintWriter;
import java.net.Socket;

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

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Sala getSalaActual() {
        return salaActual;
    }

    public void setSalaActual(Sala salaActual) {
        this.salaActual = salaActual;
    }

    public synchronized void enviarMensaje(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }
}
