package bo.edu.usfx.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
        String hilo = Thread.currentThread().getName();

        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            usuario = new Usuario(idCliente, socket, out);
            ServidorSalas.registrarUsuario(usuario);

            out.println("Bienvenido. Le atiende el hilo: " + hilo);
            out.println("Tu apodo actual es: " + usuario.getNickname());
            out.println("Te encuentras en la sala: " + usuario.getSalaActual().getNombre());
            out.println("Escribe /ayuda para ver los comandos disponibles.");

            String linea;
            while ((linea = in.readLine()) != null) {
                linea = linea.trim();

                if (linea.length() == 0) {
                    continue;
                }

                if (linea.startsWith("/")) {
                    procesarComando(linea);

                    if (linea.equalsIgnoreCase("/salir")) {
                        break;
                    }
                } else {
                    System.out.println("[" + hilo + "] " + usuario.getNickname()
                            + ": " + linea);
                    usuario.getSalaActual().difundir(usuario.getNickname() + "> " + linea, usuario);
                }
            }
        } catch (IOException e) {
            System.err.println("Error con el cliente " + idCliente + ": " + e.getMessage());
        } finally {
            if (usuario != null) {
                ServidorSalas.desconectarUsuario(usuario);
            }

            try {
                socket.close();
            } catch (IOException e) {
            }

            System.out.println("Cliente " + idCliente + " desconectado");
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
                    ServidorSalas.cambiarNick(usuario, partes[1]);
                }
                break;

            case "/salas":
                usuario.enviarMensaje(ServidorSalas.listarSalas());
                break;

            case "/crear":
                if (partes.length < 2) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /crear <nombre_sala>");
                } else {
                    ServidorSalas.crearSala(usuario, partes[1]);
                }
                break;

            case "/unirse":
                if (partes.length < 2) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /unirse <nombre_sala>");
                } else {
                    ServidorSalas.unirASala(usuario, partes[1]);
                }
                break;

            case "/quien":
                usuario.enviarMensaje(ServidorSalas.listarUsuariosSala(usuario.getSalaActual()));
                break;

            case "/privado":
                if (partes.length < 3) {
                    usuario.enviarMensaje("[SISTEMA] Uso: /privado <apodo> <mensaje>");
                } else {
                    ServidorSalas.enviarMensajePrivado(usuario, partes[1], partes[2]);
                }
                break;

            case "/estado":
                usuario.enviarMensaje(ServidorSalas.obtenerEstadoSistema());
                break;

            case "/salir":
                usuario.enviarMensaje("[SISTEMA] Desconectando del servidor...");
                break;

            case "/ayuda":
                usuario.enviarMensaje(
                        "[COMANDOS DISPONIBLES]\n"
                        + "/nick <apodo>         - Cambia tu apodo.\n"
                        + "/salas                - Lista las salas disponibles.\n"
                        + "/crear <sala>         - Crea una nueva sala y te traslada.\n"
                        + "/unirse <sala>        - Se une a una sala existente.\n"
                        + "/quien                - Muestra usuarios en la sala actual.\n"
                        + "/privado <nick> <msg> - Envia mensaje privado.\n"
                        + "/estado               - Estado global del servidor.\n"
                        + "/salir                - Cierra la conexion.");
                break;

            default:
                usuario.enviarMensaje("[SISTEMA] Comando no reconocido. Usa /ayuda");
                break;
        }
    }
}
