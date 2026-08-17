package bo.edu.usfx.sockets.parte2;

import java.io.PrintWriter;
import java.net.Socket;

public class UsuarioChat {
    private final int id;
    private final Socket socket;
    private final PrintWriter salida;
    private String nick;
    private SalaChat salaActual;

    public UsuarioChat(int id, Socket socket, PrintWriter salida) {
        this.id = id;
        this.socket = socket;
        this.salida = salida;
        this.nick = "usuario_" + id;
    }

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public SalaChat getSalaActual() {
        return salaActual;
    }

    public void setSalaActual(SalaChat salaActual) {
        this.salaActual = salaActual;
    }

    public synchronized void enviar(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
        }
    }
}
