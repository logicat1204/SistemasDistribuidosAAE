package com.sis258.practica2udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Nodo3 {

    public static void main(String[] args) {
        int puertoNodo3 = 6805;
        int puertoNodo1 = 6801;
        String ip = "192.168.137.40";

        try {
            DatagramSocket socketUDP = new DatagramSocket(puertoNodo3);
            System.out.println("Nodo 3 iniciado en el puerto " + puertoNodo3);

            while (true) {
                byte[] bufer = new byte[2000];
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);
                socketUDP.receive(peticion);

                String datos = new String(
                        peticion.getData(), 0, peticion.getLength()
                );

                
                String[] partes = datos.split("\n", 4);
                int cantidadCaracteres = Integer.parseInt(partes[0]);
                int cantidadPalabras = Integer.parseInt(partes[1]);
                String paridad = partes[2];
                String textoOriginal = partes[3];

                String textoMayusculas = textoOriginal.toUpperCase();
                int cantidadVocales = contarVocales(textoOriginal);

                String resumen = "Texto original: " + textoOriginal + "\n"
                        + "Texto en mayusculas: " + textoMayusculas + "\n"
                        + "Cantidad de caracteres: " + cantidadCaracteres + "\n"
                        + "Cantidad de palabras: " + cantidadPalabras + "\n"
                        + "La cantidad de caracteres es: " + paridad + "\n"
                        + "Cantidad de vocales: " + cantidadVocales;

                byte[] mensaje = resumen.getBytes();
                InetAddress host = InetAddress.getByName(ip);
                DatagramPacket respuesta = new DatagramPacket(
                        mensaje, mensaje.length, host, puertoNodo1
                );
                socketUDP.send(respuesta);

                System.out.println("Resumen enviado nuevamente al Nodo 1.");
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static int contarVocales(String texto) {
        int cantidad = 0;
        String vocales = "aeiouAEIOU";

        for (int i = 0; i < texto.length(); i++) {
            if (vocales.indexOf(texto.charAt(i)) != -1) {
                cantidad++;
            }
        }

        return cantidad;
    }
}
