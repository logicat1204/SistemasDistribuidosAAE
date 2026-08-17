/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 *
 * @author gabriel
 */

public class Sala {
    private final String nombre;
    private final Set<Usuario> miembros = new CopyOnWriteArraySet<>();

    public Sala(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public void agregarUsuario(Usuario u) {
        miembros.add(u);
        u.setSalaActual(this);
    }

    public void removerUsuario(Usuario u) {
        miembros.remove(u);
    }

    public int getCantidadUsuarios() {
        return miembros.size();
    }

    public Set<Usuario> getMiembros() {
        return miembros;
    }

    // Difunde un mensaje a los miembros de ESTA sala
    public void difundir(String mensaje, Usuario emisor) {
        for (Usuario u : miembros) {
            if (emisor == null || !u.equals(emisor)) {
                u.enviarMensaje(mensaje);
            }
        }
    }
}
