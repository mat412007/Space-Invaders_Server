package com.space_invaders.red;

import com.space_invaders.screens.GameScreen;
import com.space_invaders.screens.MenuScreen;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class HiloServidor extends Thread{

    private DatagramSocket conexion;
    private boolean fin = false;
    private DireccionRed[] clientes = new DireccionRed[2]; //
    private int cantClientes = 0;
    private GameScreen gameScreen;

    public boolean empezar = false;

    //Constructor
    public HiloServidor(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
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

    public void enviarMensajeATodos(String msg) {
        for (int i = 0; i < cantClientes; i++) {
            enviarMensaje(msg, clientes[i].getIp(), clientes[i].getPuerto());
        }
    }
    public void enviarMensaje(String msg, InetAddress ip, int puerto) {
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
        System.out.println("Esperando mensaje...");
        do {
            byte[] data = new byte[1024];
            DatagramPacket dp = new DatagramPacket(data, data.length);
            try {
                conexion.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            procesarMensaje(dp);
        } while(!fin);
    }

    private void procesarMensaje(DatagramPacket dp) {
        String msg = new String(dp.getData()).trim();

        // --- PRIMER CASO: MENSAJE "Conexion" ---
        if (msg.equals("Conexion")) {
            System.out.println("Conexion recibida de " + dp.getAddress() + ":" + dp.getPort());

            if (cantClientes < 2) {
                clientes[cantClientes] = new DireccionRed(dp.getAddress(), dp.getPort());
                enviarMensaje("OK:"+(cantClientes+1), clientes[cantClientes].getIp(), clientes[cantClientes].getPuerto());
                cantClientes++;

                if (cantClientes == 2 && !empezar) {
                    empezar = true;
                    enviarMensajeATodos("Empieza"); // Envio mensaje a ambos clientes de empezar
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
        // En caso de cliente desconocido
        if (idCliente == -1) {
            System.out.println("Mensaje de cliente desconocido");
            return;
        }

        // Reenviar al otro cliente
        int otro = (idCliente == 0) ? 1 : 0;
        if (cantClientes == 2) {
            enviarMensaje(msg, clientes[otro].getIp(), clientes[otro].getPuerto());
        }

        System.out.println("Cliente " + (idCliente+1) + ": " + msg);
        String[] partes = msg.split(":");
        if(msg.startsWith("mover_izquierda")){
            this.gameScreen.moverIzquierda(Integer.parseInt(partes[1]));
        }
        if(msg.startsWith("mover_derecha")){
            this.gameScreen.moverDerecha(Integer.parseInt(partes[1]));
        }
        if(msg.startsWith("disparar")){
            this.gameScreen.disparar(Integer.parseInt(partes[1]));
        }
        if (msg.startsWith("actualizarDisparo")) {
            enviarMensaje(msg, clientes[otro].getIp(), clientes[otro].getPuerto());
        }

    }


}
