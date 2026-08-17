package bo.edu.usfx.sockets.parte2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorChatSalas implements Runnable {
    private final Socket socket;
    private final int id;
    private UsuarioChat usuario;

    public ManejadorChatSalas(Socket socket, int id) {
        this.socket = socket;
        this.id = id;
    }

    @Override
    public void run() {
        String hilo = Thread.currentThread().getName();

        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            usuario = new UsuarioChat(id, socket, out);

            out.println("Bienvenido. Le atiende el hilo: " + hilo);
            out.println("Tu apodo actual es: " + usuario.getNick());
            ServidorChatSalas.registrar(usuario);
            out.println("Sala actual: " + usuario.getSalaActual().getNombre());
            out.println(usuario.getSalaActual().obtenerHistorial());
            out.println("Escribe /ayuda para ver los comandos.");

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
                    enviarMensajeSala(linea, hilo);
                }
            }
        } catch (IOException e) {
            System.err.println("Error con el cliente " + id + ": " + e.getMessage());
        } finally {
            if (usuario != null) {
                ServidorChatSalas.desconectar(usuario);
            }

            try {
                socket.close();
            } catch (IOException e) {
            }

            System.out.println("Cliente " + id + " desconectado");
        }
    }

    private void enviarMensajeSala(String texto, String hilo) {
        SalaChat sala = usuario.getSalaActual();
        String mensaje = "[" + sala.getNombre() + "] " + usuario.getNick() + "> " + texto;

        System.out.println("[" + hilo + "] " + mensaje);
        sala.guardarMensaje(mensaje);
        sala.difundir(mensaje, usuario);
    }

    private void procesarComando(String linea) {
        String[] partes = linea.split(" ", 3);
        String comando = partes[0].toLowerCase();

        switch (comando) {
            case "/nick":
                if (partes.length < 2) {
                    usuario.enviar("[SISTEMA] Uso: /nick <apodo>");
                } else {
                    ServidorChatSalas.cambiarNick(usuario, partes[1]);
                }
                break;

            case "/salas":
                usuario.enviar(ServidorChatSalas.listarSalas());
                break;

            case "/crear":
                if (partes.length < 2) {
                    usuario.enviar("[SISTEMA] Uso: /crear <sala>");
                } else {
                    ServidorChatSalas.crearSala(usuario, partes[1]);
                }
                break;

            case "/unirse":
                if (partes.length < 2) {
                    usuario.enviar("[SISTEMA] Uso: /unirse <sala>");
                } else {
                    ServidorChatSalas.unirASala(usuario, partes[1]);
                }
                break;

            case "/quien":
                usuario.enviar(ServidorChatSalas.listarUsuarios(usuario.getSalaActual()));
                break;

            case "/privado":
                if (partes.length < 3) {
                    usuario.enviar("[SISTEMA] Uso: /privado <apodo> <mensaje>");
                } else {
                    ServidorChatSalas.enviarPrivado(usuario, partes[1], partes[2]);
                }
                break;

            case "/estado":
                usuario.enviar(ServidorChatSalas.estado());
                break;

            case "/historial":
                usuario.enviar(usuario.getSalaActual().obtenerHistorial());
                break;

            case "/salir":
                usuario.enviar("[SISTEMA] Cerrando conexion...");
                break;

            case "/ayuda":
                enviarAyuda();
                break;

            default:
                usuario.enviar("[SISTEMA] Comando no reconocido. Usa /ayuda");
                break;
        }
    }

    private void enviarAyuda() {
        usuario.enviar(
                "[COMANDOS DISPONIBLES]\n"
                + "/nick <apodo>         - Asigna o cambia tu apodo.\n"
                + "/salas                - Lista las salas.\n"
                + "/crear <sala>         - Crea una sala y te traslada.\n"
                + "/unirse <sala>        - Entra a una sala existente.\n"
                + "/quien                - Lista usuarios de tu sala.\n"
                + "/privado <nick> <msg> - Envia mensaje privado.\n"
                + "/estado               - Muestra el estado del servidor.\n"
                + "/historial            - Muestra los ultimos 10 mensajes.\n"
                + "/salir                - Cierra la conexion.");
    }
}
