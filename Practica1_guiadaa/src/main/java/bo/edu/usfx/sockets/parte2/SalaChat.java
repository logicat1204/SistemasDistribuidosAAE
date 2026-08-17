package bo.edu.usfx.sockets.parte2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class SalaChat {
    private final String nombre;
    private final Set<UsuarioChat> usuarios;
    private final List<String> historial;

    public SalaChat(String nombre) {
        this.nombre = nombre;
        this.usuarios = new CopyOnWriteArraySet<UsuarioChat>();
        this.historial = new ArrayList<String>();
    }

    public String getNombre() {
        return nombre;
    }

    public void agregarUsuario(UsuarioChat usuario) {
        usuarios.add(usuario);
        usuario.setSalaActual(this);
    }

    public void quitarUsuario(UsuarioChat usuario) {
        usuarios.remove(usuario);
    }

    public int cantidadUsuarios() {
        return usuarios.size();
    }

    public Set<UsuarioChat> getUsuarios() {
        return usuarios;
    }

    public void difundir(String mensaje, UsuarioChat emisor) {
        for (UsuarioChat usuario : usuarios) {
            if (emisor == null || usuario != emisor) {
                usuario.enviar(mensaje);
            }
        }
    }

    public synchronized void guardarMensaje(String mensaje) {
        historial.add(mensaje);

        if (historial.size() > 10) {
            historial.remove(0);
        }
    }

    public synchronized String obtenerHistorial() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HISTORIAL DE ");
        sb.append(nombre);
        sb.append(" ===\n");

        if (historial.isEmpty()) {
            sb.append("No hay mensajes todavia.\n");
        } else {
            for (String mensaje : historial) {
                sb.append(mensaje);
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
