package com.space_invaders.red;

import com.badlogic.gdx.ApplicationAdapter;
import com.space_invaders.red.HiloServidor;

public class Servidor extends ApplicationAdapter {

    private HiloServidor hs;

    @Override
    public void create() {
        // Inicializa el hilo del servidor
        hs = new HiloServidor();
        hs.start();
    }

    @Override
    public void render() {
    }

    @Override
    public void dispose() {
        // Liberar recursos del servidor
        if (hs != null) {
            hs.interrupt(); // detener el hilo
            hs = null;
        }
    }
}

