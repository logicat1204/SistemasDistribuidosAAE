package com.sis258.practica2udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Nodo2 {

    public static void main(String[] args) {
        int puertoNodo2 = 6804;
        int puertoNodo3 = 6805;
        String ip = "192.168.137.10";

        try {
            DatagramSocket socketUDP = new DatagramSocket(puertoNodo2);
            System.out.println("Nodo 2 iniciado en el puerto " + puertoNodo2);

            while (true) {
                byte[] bufer = new byte[2000];
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);
                socketUDP.receive(peticion);

                String datos = new String(
                        peticion.getData(), 0, peticion.getLength()
                );

                
                int posicion = datos.indexOf("\n");
                int cantidadCaracteres = Integer.parseInt(
                        datos.substring(0, posicion)
                );
                String texto = datos.substring(posicion + 1);

                int cantidadPalabras = contarPalabras(texto);
                String paridad;

                if (cantidadCaracteres % 2 == 0) {
                    paridad = "par";
                } else {
                    paridad = "impar";
                }

                String datosProcesados = cantidadCaracteres + "\n"
                        + cantidadPalabras + "\n"
                        + paridad + "\n"
                        + texto;

                byte[] mensaje = datosProcesados.getBytes();
                InetAddress host = InetAddress.getByName(ip);
                DatagramPacket respuesta = new DatagramPacket(
                        mensaje, mensaje.length, host, puertoNodo3
                );
                socketUDP.send(respuesta);

                System.out.println("Mensaje procesado y enviado al Nodo 3.");
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public static int contarPalabras(String texto) {
        texto = texto.trim();

        if (texto.isEmpty()) {
            return 0;
        }

        String[] palabras = texto.split("\\s+");
        return palabras.length;
    }
}
