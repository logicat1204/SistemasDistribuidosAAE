/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.*;
import java.net.Socket;
/**
 *
 * @author gabriel
 */


public class ManejadorChat implements Runnable {
    private final Socket socket;
    private final int idCliente;
    private Usuario usuario;

    public ManejadorChat(Socket socket, int idCliente) {
        this.socket = socket;
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        String nombreHilo = Thread.currentThread().getName();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            usuario = new Usuario(idCliente, socket, out);
            ServidorSalas.registrarUsuario(usuario);

            out.println("=== BIENVENIDO AL CHAT DE SALAS (Atendido por " + nombreHilo + ") ===");
            out.println("Tu apodo actual es: " + usuario.getNickname());
            out.println("Te encuentras en la sala: " + usuario.getSalaActual().getNombre());
            out.println("Escribe /ayuda para ver los comandos disponibles.\n");

            String linea;
            while ((linea = in.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                if (linea.startsWith("/")) {
                    procesarComando(linea);
                    if (linea.equalsIgnoreCase("/salir")) {
                        break;
                    }
                } else {
                    // Mensaje normal para la sala actual
                    String mensajeFormateado = usuario.getNickname() + "> " + linea;
                    usuario.getSalaActual().difundir(mensajeFormateado, usuario);
                }
            }
        } catch (IOException e) {
            System.err.println("Conexión perdida con cliente " + idCliente + ": " + e.getMessage());
        } finally {
            if (usuario != null) {
                ServidorSalas.desconectarUsuario(usuario);
            }
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Cliente " + idCliente + " desconectado y recursos liberados.");
        }
    }

    private void procesarComando(String cmd) {
        String[] partes = cmd.split(" ", 3);
        String comando = partes[0].toLowerCase();

        switch (comando) {
            case "/nick":
                if (partes.length < 2) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /nick <nuevo_apodo>");
                } else {
                    ServidorSalas.cambiarNick(usuario, partes[1].trim());
                }
                break;

            case "/salas":
                usuario.enviarMensaje(ServidorSalas.listarSalas());
                break;

            case "/crear":
                if (partes.length < 2) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /crear <nombre_sala>");
                } else {
                    ServidorSalas.crearSala(usuario, partes[1].trim());
                }
                break;

            case "/unirse":
                if (partes.length < 2) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /unirse <nombre_sala>");
                } else {
                    ServidorSalas.unirASala(usuario, partes[1].trim());
                }
                break;

            case "/quien":
                usuario.enviarMensaje(ServidorSalas.listarUsuariosSala(usuario.getSalaActual()));
                break;

            case "/privado":
                if (partes.length < 3) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /privado <apodo> <mensaje>");
                } else {
                    ServidorSalas.enviarMensajePrivado(usuario, partes[1].trim(), partes[2].trim());
                }
                break;

            case "/estado":
                usuario.enviarMensaje(ServidorSalas.obtenerEstadoSistema());
                break;

            case "/salir":
                usuario.enviarMensaje("[SISTEMA] Desconectando del servidor... ¡Hasta luego!");
                break;

            case "/ayuda":
                usuario.enviarMensaje("""
                    [COMANDOS DISPONIBLES]
                    /nick <apodo>         - Cambia tu apodo.
                    /salas                - Lista las salas disponibles.
                    /crear <sala>         - Crea una nueva sala y te traslada.
                    /unirse <sala>        - Se une a una sala existente.
                    /quien                - Muestra usuarios en la sala actual.
                    /privado <nick> <msg> - Envía mensaje privado.
                    /estado               - Estado global del servidor.
                    /salir                - Cierra la conexión.
                    """);
                break;

            default:
                usuario.enviarMensaje("[SISTEMA] Comando no reconocido. Usa /ayuda");
                break;
        }
    }
}
