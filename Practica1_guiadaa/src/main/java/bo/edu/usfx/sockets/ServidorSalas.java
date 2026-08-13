package bo.edu.usfx.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorSalas {
    private static final int PUERTO_DEFAULT = 5003;
    private static final int HILOS_DEFAULT = 4;

    private static final AtomicInteger contadorHistorico = new AtomicInteger(0);
    private static final Map<String, Sala> salas = new ConcurrentHashMap<String, Sala>();
    private static final Map<String, Usuario> usuariosPorNick = new ConcurrentHashMap<String, Usuario>();

    public static void main(String[] args) throws IOException {
        int puerto = PUERTO_DEFAULT;
        int hilos = HILOS_DEFAULT;

        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        if (args.length > 1) {
            hilos = Integer.parseInt(args[1]);
        }

        salas.put("general", new Sala("general"));

        ServerSocket servidor = new ServerSocket(puerto);
        ExecutorService pool = Executors.newFixedThreadPool(hilos);

        System.out.println("Servidor de chat con salas en el puerto " + puerto);
        System.out.println("Cantidad de hilos: " + hilos);

        while (true) {
            Socket cliente = servidor.accept();
            int id = contadorHistorico.incrementAndGet();

            System.out.println("Conexion #" + id + " desde "
                    + cliente.getInetAddress().getHostAddress());

            pool.execute(new ManejadorChat(cliente, id));
        }
    }

    public static void registrarUsuario(Usuario u) {
        usuariosPorNick.put(u.getNickname(), u);

        Sala general = salas.get("general");
        general.agregarUsuario(u);
        general.difundir("[SISTEMA] " + u.getNickname() + " ha entrado a la sala.", u);
    }

    public static void desconectarUsuario(Usuario u) {
        usuariosPorNick.remove(u.getNickname());

        if (u.getSalaActual() != null) {
            u.getSalaActual().removerUsuario(u);
            u.getSalaActual().difundir("[SISTEMA] " + u.getNickname()
                    + " ha salido de la sala.", null);
        }
    }

    public static synchronized void cambiarNick(Usuario u, String nuevoNick) {
        if (nuevoNick == null || nuevoNick.trim().length() == 0) {
            u.enviarMensaje("[SISTEMA] El apodo no puede estar vacio.");
            return;
        }

        nuevoNick = nuevoNick.trim();

        if (usuariosPorNick.containsKey(nuevoNick)) {
            u.enviarMensaje("[SISTEMA] El apodo '" + nuevoNick + "' ya esta en uso.");
            return;
        }

        String viejoNick = u.getNickname();
        usuariosPorNick.remove(viejoNick);
        u.setNickname(nuevoNick);
        usuariosPorNick.put(nuevoNick, u);

        u.enviarMensaje("[SISTEMA] Tu apodo ahora es: " + nuevoNick);
        u.getSalaActual().difundir("[SISTEMA] " + viejoNick
                + " ahora se llama " + nuevoNick, u);
    }

    public static String listarSalas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SALAS DISPONIBLES ===\n");

        for (String nombre : salas.keySet()) {
            Sala sala = salas.get(nombre);
            sb.append(" - ");
            sb.append(nombre);
            sb.append(" (");
            sb.append(sala.getCantidadUsuarios());
            sb.append(" usuarios)\n");
        }

        return sb.toString();
    }

    public static synchronized void crearSala(Usuario u, String nombreSala) {
        nombreSala = nombreSala.toLowerCase();

        if (salas.containsKey(nombreSala)) {
            u.enviarMensaje("[SISTEMA] Error: La sala '" + nombreSala + "' ya existe.");
            return;
        }

        salas.put(nombreSala, new Sala(nombreSala));
        u.enviarMensaje("[SISTEMA] Sala '" + nombreSala + "' creada con exito.");
        unirASala(u, nombreSala);
    }

    public static void unirASala(Usuario u, String nombreSala) {
        nombreSala = nombreSala.toLowerCase();
        Sala salaDestino = salas.get(nombreSala);

        if (salaDestino == null) {
            u.enviarMensaje("[SISTEMA] Error: La sala '" + nombreSala
                    + "' no existe. Usa /crear " + nombreSala);
            return;
        }

        Sala salaActual = u.getSalaActual();

        if (salaActual != null) {
            if (salaActual.getNombre().equalsIgnoreCase(nombreSala)) {
                u.enviarMensaje("[SISTEMA] Ya estas en la sala '" + nombreSala + "'.");
                return;
            }

            salaActual.removerUsuario(u);
            salaActual.difundir("[SISTEMA] " + u.getNickname()
                    + " ha dejado la sala.", null);
        }

        salaDestino.agregarUsuario(u);
        u.enviarMensaje("[SISTEMA] Te has unido a la sala: " + nombreSala);
        salaDestino.difundir("[SISTEMA] " + u.getNickname()
                + " se ha unido a la sala.", u);
    }

    public static String listarUsuariosSala(Sala sala) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USUARIOS EN SALA '");
        sb.append(sala.getNombre());
        sb.append("' ===\n");

        for (Usuario u : sala.getMiembros()) {
            sb.append(" - ");
            sb.append(u.getNickname());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void enviarMensajePrivado(Usuario emisor, String nickDestino, String mensaje) {
        Usuario receptor = usuariosPorNick.get(nickDestino);

        if (receptor == null) {
            emisor.enviarMensaje("[SISTEMA] El usuario '" + nickDestino
                    + "' no esta conectado.");
            return;
        }

        receptor.enviarMensaje("[PRIVADO de " + emisor.getNickname() + "]: " + mensaje);
        emisor.enviarMensaje("[PRIVADO para " + nickDestino + "]: " + mensaje);
    }

    public static String obtenerEstadoSistema() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADO DEL SISTEMA ===\n");
        sb.append("- Usuarios conectados actualmente: ");
        sb.append(usuariosPorNick.size());
        sb.append("\n");
        sb.append("- Total historico de conexiones: ");
        sb.append(contadorHistorico.get());
        sb.append("\n");
        sb.append("- Cantidad de salas creadas: ");
        sb.append(salas.size());
        sb.append("\n");

        return sb.toString();
    }
}
