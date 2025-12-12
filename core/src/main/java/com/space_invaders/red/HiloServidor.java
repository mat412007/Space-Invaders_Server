package com.space_invaders.red;

import com.space_invaders.screens.MenuScreen;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class HiloServidor extends Thread{

    private DatagramSocket conexion;
    private boolean fin = false;
    private DireccionRed[] clientes = new DireccionRed[2]; //
    private int cantClientes = 0;

    public boolean empezar = false;

    //Constructor
    public HiloServidor() {
        try {
            conexion = new DatagramSocket(9998);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void cerrar() {
        fin = true;
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }

    private void enviarMensaje(String msg, InetAddress ip, int puerto) {
        byte[] data = msg.getBytes();
        DatagramPacket dp = new DatagramPacket(data, data.length, ip, puerto);
        try {
            conexion.send(dp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        do {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                System.out.println("Esperando mensaje...");
                conexion.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            procesarMensaje(dp);
        } while(!fin);
    }

    private void procesarMensaje(DatagramPacket dp) {
        String msg = new String(dp.getData()).trim();
        System.out.println("Mensaje recibido: " + msg);

        // --- PRIMER CASO: MENSAJE "Conexion" ---
        if (msg.equals("Conexion")) {
            System.out.println("Conexion recibida de " + dp.getAddress() + ":" + dp.getPort());

            if (cantClientes < 2) {
                clientes[cantClientes] = new DireccionRed(dp.getAddress(), dp.getPort());
                enviarMensaje("OK", clientes[cantClientes].getIp(), clientes[cantClientes].getPuerto());
                cantClientes++;

                if (cantClientes == 2 && !empezar) {
                    empezar = true;
                    for (DireccionRed c : clientes) {
                        enviarMensaje("Empieza", c.getIp(), c.getPuerto());
                    }
                }
            }
            return;
        }

        // --- SEGUNDO CASO: MENSAJES DE ACCIÓN DEL CLIENTE ---
        // Identificamos qué cliente envió el mensaje
        int idCliente = -1;
        for (int i = 0; i < cantClientes; i++) {
            if (clientes[i].getIp().equals(dp.getAddress()) &&
                clientes[i].getPuerto() == dp.getPort()) {
                idCliente = i;
                break;
            }
        }

        if (idCliente == -1) {
            System.out.println("Mensaje de cliente desconocido");
            return;
        }

        System.out.println("Mensaje del cliente " + (idCliente+1) + ": " + msg);

        // Reenviar al otro cliente
        int otro = (idCliente == 0) ? 1 : 0;

        if (cantClientes == 2) {
            enviarMensaje(msg, clientes[otro].getIp(), clientes[otro].getPuerto());
        }
    }


}
