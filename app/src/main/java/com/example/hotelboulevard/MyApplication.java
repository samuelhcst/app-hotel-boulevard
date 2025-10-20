package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Esta clase se ejecuta UNA SOLA VEZ cuando se inicia la app.
 * Es el lugar perfecto para inicializar servicios como Cloudinary.
 */
public class MyApplication extends Application {

    // Pon tu Cloud Name aquí
    private final String CLOUD_NAME = "dp10u608a";

    @Override
    public void onCreate() {
        super.onCreate();

        // Configurar Cloudinary UNA SOLA VEZ para toda la app
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CLOUD_NAME);
        config.put("secure", "true");
        MediaManager.init(this, config);
    }
}