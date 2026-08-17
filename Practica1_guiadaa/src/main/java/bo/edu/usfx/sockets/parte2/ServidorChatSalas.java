package bo.edu.usfx.sockets.parte2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServidorChatSalas {
    private static final int PUERTO_DEFAULT = 5000;
    private static final int HILOS_DEFAULT = 4;

    private static final AtomicInteger totalHistorico = new AtomicInteger(0);
    private static final Map<String, SalaChat> salas = new ConcurrentHashMap<String, SalaChat>();
    private static final Map<String, UsuarioChat> usuarios = new ConcurrentHashMap<String, UsuarioChat>();

    public static void main(String[] args) throws IOException {
        int puerto = PUERTO_DEFAULT;
        int hilos = HILOS_DEFAULT;

        if (args.length > 0) {
            puerto = Integer.parseInt(args[0]);
        }

        if (args.length > 1) {
            hilos = Integer.parseInt(args[1]);
        }

        salas.put("general", new SalaChat("general"));

        ServerSocket servidor = new ServerSocket(puerto);
        ExecutorService pool = Executors.newFixedThreadPool(hilos);

        System.out.println("Servidor de chat con salas en el puerto " + puerto);
        System.out.println("Pool de hilos: " + hilos);

        while (true) {
            Socket cliente = servidor.accept();
            int id = totalHistorico.incrementAndGet();

            System.out.println("Conexion #" + id + " desde "
                    + cliente.getInetAddress().getHostAddress());

            pool.execute(new ManejadorChatSalas(cliente, id));
        }
    }

    public static void registrar(UsuarioChat usuario) {
        usuarios.put(usuario.getNick(), usuario);

        SalaChat general = salas.get("general");
        general.agregarUsuario(usuario);
        general.difundir("[SISTEMA] " + usuario.getNick()
                + " ha entrado a la sala.", usuario);
    }

    public static void desconectar(UsuarioChat usuario) {
        usuarios.remove(usuario.getNick());

        SalaChat sala = usuario.getSalaActual();
        if (sala != null) {
            sala.quitarUsuario(usuario);
            sala.difundir("[SISTEMA] " + usuario.getNick()
                    + " ha salido de la sala.", null);
        }
    }

    public static synchronized void cambiarNick(UsuarioChat usuario, String nuevoNick) {
        if (nuevoNick == null || nuevoNick.trim().length() == 0) {
            usuario.enviar("[SISTEMA] Uso: /nick <apodo>");
            return;
        }

        nuevoNick = nuevoNick.trim();

        if (usuarios.containsKey(nuevoNick)) {
            usuario.enviar("[SISTEMA] El apodo '" + nuevoNick + "' ya esta en uso.");
            return;
        }

        String nickAnterior = usuario.getNick();
        usuarios.remove(nickAnterior);
        usuario.setNick(nuevoNick);
        usuarios.put(nuevoNick, usuario);

        usuario.enviar("[SISTEMA] Tu apodo ahora es: " + nuevoNick);

        if (usuario.getSalaActual() != null) {
            usuario.getSalaActual().difundir("[SISTEMA] " + nickAnterior
                    + " ahora se llama " + nuevoNick, usuario);
        }
    }

    public static String listarSalas() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SALAS DISPONIBLES ===\n");

        for (String nombre : salas.keySet()) {
            SalaChat sala = salas.get(nombre);
            sb.append(" - ");
            sb.append(nombre);
            sb.append(" (");
            sb.append(sala.cantidadUsuarios());
            sb.append(" usuarios)\n");
        }

        return sb.toString();
    }

    public static synchronized void crearSala(UsuarioChat usuario, String nombreSala) {
        if (nombreSala == null || nombreSala.trim().length() == 0) {
            usuario.enviar("[SISTEMA] Uso: /crear <sala>");
            return;
        }

        nombreSala = nombreSala.trim().toLowerCase();

        if (salas.containsKey(nombreSala)) {
            usuario.enviar("[SISTEMA] La sala '" + nombreSala + "' ya existe.");
            return;
        }

        salas.put(nombreSala, new SalaChat(nombreSala));
        usuario.enviar("[SISTEMA] Sala '" + nombreSala + "' creada.");
        unirASala(usuario, nombreSala);
    }

    public static void unirASala(UsuarioChat usuario, String nombreSala) {
        if (nombreSala == null || nombreSala.trim().length() == 0) {
            usuario.enviar("[SISTEMA] Uso: /unirse <sala>");
            return;
        }

        nombreSala = nombreSala.trim().toLowerCase();
        SalaChat salaDestino = salas.get(nombreSala);

        if (salaDestino == null) {
            usuario.enviar("[SISTEMA] La sala '" + nombreSala
                    + "' no existe. Usa /crear " + nombreSala);
            return;
        }

        SalaChat salaActual = usuario.getSalaActual();

        if (salaActual != null) {
            if (salaActual.getNombre().equalsIgnoreCase(nombreSala)) {
                usuario.enviar("[SISTEMA] Ya estas en la sala '" + nombreSala + "'.");
                return;
            }

            salaActual.quitarUsuario(usuario);
            salaActual.difundir("[SISTEMA] " + usuario.getNick()
                    + " ha dejado la sala.", null);
        }

        salaDestino.agregarUsuario(usuario);
        usuario.enviar("[SISTEMA] Te has unido a la sala: " + nombreSala);
        usuario.enviar(salaDestino.obtenerHistorial());
        salaDestino.difundir("[SISTEMA] " + usuario.getNick()
                + " se ha unido a la sala.", usuario);
    }

    public static String listarUsuarios(SalaChat sala) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USUARIOS EN ");
        sb.append(sala.getNombre());
        sb.append(" ===\n");

        for (UsuarioChat usuario : sala.getUsuarios()) {
            sb.append(" - ");
            sb.append(usuario.getNick());
            sb.append("\n");
        }

        return sb.toString();
    }

    public static void enviarPrivado(UsuarioChat emisor, String nickDestino, String mensaje) {
        if (nickDestino == null || mensaje == null || mensaje.trim().length() == 0) {
            emisor.enviar("[SISTEMA] Uso: /privado <apodo> <mensaje>");
            return;
        }

        UsuarioChat receptor = usuarios.get(nickDestino);

        if (receptor == null) {
            emisor.enviar("[SISTEMA] El usuario '" + nickDestino + "' no esta conectado.");
            return;
        }

        receptor.enviar("[PRIVADO de " + emisor.getNick() + "]: " + mensaje);
        emisor.enviar("[PRIVADO para " + nickDestino + "]: " + mensaje);
    }

    public static String estado() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADO DEL SISTEMA ===\n");
        sb.append("- Usuarios conectados actualmente: ");
        sb.append(usuarios.size());
        sb.append("\n");
        sb.append("- Total historico de conexiones: ");
        sb.append(totalHistorico.get());
        sb.append("\n");
        sb.append("- Cantidad de salas creadas: ");
        sb.append(salas.size());
        sb.append("\n");

        return sb.toString();
    }
}
