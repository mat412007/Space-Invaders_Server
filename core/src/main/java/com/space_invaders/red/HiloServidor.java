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

    //Constructor
    public HiloServidor() {
        try {
            conexion = new DatagramSocket(9998);
        } catch (SocketException e) {
            throw new RuntimeException(e);
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
        if(msg.equals("Conexion")) {
            System.out.println("Conexion recibida de " + dp.getAddress() + ":" + dp.getPort());
            if(cantClientes < 2) {
                clientes[cantClientes] = new DireccionRed(dp.getAddress(), dp.getPort());
                enviarMensaje("OK", clientes[cantClientes].getIp(), clientes[cantClientes++].getPuerto());
                if(cantClientes == 2) {
                    for(int i = 0; i < clientes.length; i++) {
                        enviarMensaje("Empieza", clientes[i].getIp(), clientes[i].getPuerto());
                    }
                }
            }
        }
    }

}
