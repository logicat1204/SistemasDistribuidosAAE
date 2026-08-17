/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bo.edu.usfx.sockets;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author gabriel
 */

public class ServidorSalas {
    private static final int PUERTO_DEFAULT = 5002;
    private static final int HILOS_DEFAULT = 4;

    // Contador atómico para el total histórico (Thread-Safe)
    private static final AtomicInteger contadorHistorico = new AtomicInteger(0);

    // Mapas concurrentes globales
    private static final Map<String, Sala> salas = new ConcurrentHashMap<>();
    private static final Map<String, Usuario> usuariosPorNick = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : PUERTO_DEFAULT;
        int maxHilos = args.length > 1 ? Integer.parseInt(args[1]) : HILOS_DEFAULT;

        // Crear sala por defecto
        salas.put("general", new Sala("general"));

        ExecutorService pool = Executors.newFixedThreadPool(maxHilos);
        ServerSocket servidor = new ServerSocket(puerto);

        System.out.println("==================================================");
        System.out.println("Servidor de Chat con Salas iniciado en el puerto " + puerto);
        System.out.println("Tamaño del Pool de Hilos: " + maxHilos);
        System.out.println("==================================================");

        while (true) {
            Socket socketCliente = servidor.accept(); // SOLO ACEPTA
            int id = contadorHistorico.incrementAndGet(); // Incremento atómico seguro entre hilos
            System.out.println("-> Conexión entrante #" + id + " desde " + socketCliente.getInetAddress().getHostAddress());
            
            pool.execute(new ManejadorChat(socketCliente, id)); // Y DELEGA
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
            u.getSalaActual().difundir("[SISTEMA] " + u.getNickname() + " ha salido de la sala.", null);
        }
    }

    public static synchronized void cambiarNick(Usuario u, String nuevoNick) {
        if (nuevoNick == null || nuevoNick.isBlank()) {
            u.enviarMensaje("[SISTEMA] El apodo no puede estar vacío.");
            return;
        }
        if (usuariosPorNick.containsKey(nuevoNick)) {
            u.enviarMensaje("[SISTEMA] Error: El apodo '" + nuevoNick + "' ya está en uso por otro usuario.");
            return;
        }
        String viejoNick = u.getNickname();
        usuariosPorNick.remove(viejoNick);
        u.setNickname(nuevoNick);
        usuariosPorNick.put(nuevoNick, u);

        u.enviarMensaje("[SISTEMA] Tu apodo ahora es: " + nuevoNick);
        u.getSalaActual().difundir("[SISTEMA] " + viejoNick + " ahora se llama " + nuevoNick, u);
    }

    public static String listarSalas() {
        StringBuilder sb = new StringBuilder("=== SALAS DISPONIBLES ===\n");
        salas.forEach((nombre, sala) -> {
            sb.append(" - ").append(nombre).append(" (").append(sala.getCantidadUsuarios()).append(" usuarios)\n");
        });
        return sb.toString();
    }

    public static void crearSala(Usuario u, String nombreSala) {
        nombreSala = nombreSala.toLowerCase();
        if (salas.putIfAbsent(nombreSala, new Sala(nombreSala)) != null) {
            u.enviarMensaje("[SISTEMA] Error: La sala '" + nombreSala + "' ya existe.");
            return;
        }
        u.enviarMensaje("[SISTEMA] Sala '" + nombreSala + "' creada con éxito.");
        unirASala(u, nombreSala);
    }

    public static void unirASala(Usuario u, String nombreSala) {
        nombreSala = nombreSala.toLowerCase();
        Sala salaDestino = salas.get(nombreSala);

        if (salaDestino == null) {
            u.enviarMensaje("[SISTEMA] Error: La sala '" + nombreSala + "' no existe. Usa /crear " + nombreSala);
            return;
        }

        Sala salaActual = u.getSalaActual();
        if (salaActual != null) {
            if (salaActual.getNombre().equalsIgnoreCase(nombreSala)) {
                u.enviarMensaje("[SISTEMA] Ya estás en la sala '" + nombreSala + "'.");
                return;
            }
            salaActual.removerUsuario(u);
            salaActual.difundir("[SISTEMA] " + u.getNickname() + " ha dejado la sala para ir a '" + nombreSala + "'.", null);
        }

        salaDestino.agregarUsuario(u);
        u.enviarMensaje("[SISTEMA] Te has unido a la sala: " + nombreSala);
        salaDestino.difundir("[SISTEMA] " + u.getNickname() + " se ha unido a la sala.", u);
    }

    public static String listarUsuariosSala(Sala sala) {
        StringBuilder sb = new StringBuilder("=== USUARIOS EN SALA '" + sala.getNombre() + "' ===\n");
        for (Usuario u : sala.getMiembros()) {
            sb.append(" - ").append(u.getNickname()).append("\n");
        }
        return sb.toString();
    }

    public static void enviarMensajePrivado(Usuario emisor, String nickDestino, String mensaje) {
        Usuario receptor = usuariosPorNick.get(nickDestino);
        if (receptor == null) {
            emisor.enviarMensaje("[SISTEMA] Error: El usuario '" + nickDestino + "' no está conectado.");
            return;
        }
        receptor.enviarMensaje("[PRIVADO de " + emisor.getNickname() + "]: " + mensaje);
        emisor.enviarMensaje("[PRIVADO para " + nickDestino + "]: " + mensaje);
    }

    public static String obtenerEstadoSistema() {
        return String.format("""
            === ESTADO DEL SISTEMA ===
            - Usuarios conectados actualmente: %d
            - Total histórico de conexiones: %d
            - Cantidad de salas creadas: %d
            """, usuariosPorNick.size(), contadorHistorico.get(), salas.size());
    }
}