package com.sis258.practica2udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class Nodo1 {

    public static void main(String[] args) {
        int puertoNodo1 = 6801;
        int puertoNodo2 = 6804;
        String ip = "192.168.137.40";
        Scanner teclado = new Scanner(System.in);

        try {
            
            DatagramSocket socketUDP = new DatagramSocket(puertoNodo1);

            System.out.print("Introduzca una palabra o frase: ");
            String texto = teclado.nextLine();
            int cantidadCaracteres = texto.length();

            
            String datos = cantidadCaracteres + "\n" + texto;
            byte[] mensaje = datos.getBytes();
            InetAddress host = InetAddress.getByName(ip);

            DatagramPacket peticion = new DatagramPacket(
                    mensaje, mensaje.length, host, puertoNodo2
            );
            socketUDP.send(peticion);

            System.out.println("Informacion enviada al Nodo 2.");
            System.out.println("Esperando el resultado del Nodo 3...");

            byte[] bufer = new byte[2000];
            DatagramPacket respuesta = new DatagramPacket(bufer, bufer.length);
            socketUDP.receive(respuesta);

            String resultado = new String(
                    respuesta.getData(), 0, respuesta.getLength()
            );

            System.out.println("\nRESULTADO FINAL");
            System.out.println(resultado);

            socketUDP.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }
}
