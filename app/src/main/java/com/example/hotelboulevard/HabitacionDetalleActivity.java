package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.os.Bundle;
import android.util.Log;
import android.widget.Button; // Importa Button
import android.widget.GridLayout; // Importa GridLayout
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Importa Toolbar

import com.google.android.material.appbar.CollapsingToolbarLayout; // Importa CollapsingToolbarLayout
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore; // Importa Firestore

import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide; // Asegúrate de tener este import
import java.util.HashMap;       // Importa HashMap
import java.util.Locale;
import java.util.Map;           // Importa Map
import java.util.concurrent.TimeUnit;

import java.util.Date; // Importa Date
import java.util.concurrent.TimeUnit; // Importa TimeUnit

public class HabitacionDetalleActivity extends AppCompatActivity {

    private static final String TAG = "HabitacionDetalle";

    // Vistas
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView ivImagen;
    private TextView tvDescripcion, tvPrecioTotal;
    private GridLayout gridLayoutServicios;
    private Button btnReservarAhora; // O MaterialButton si usas ese

    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Datos recibidos
    private String habitacionId;
    private long fechaLlegadaMillis;
    private long fechaSalidaMillis;
    // (Podríamos necesitar el número de huéspedes también)

    // Datos de la habitación (se cargarán desde Firestore)
    private Habitacion habitacionActual;
    private double precioTotalCalculado = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habitacion_detalle);

        // --- Enlazar Vistas ---
        toolbar = findViewById(R.id.toolbar_detalle);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar_detalle);
        ivImagen = findViewById(R.id.iv_detalle_imagen);
        tvDescripcion = findViewById(R.id.tv_detalle_descripcion);
        tvPrecioTotal = findViewById(R.id.tv_detalle_precio_total);
        gridLayoutServicios = findViewById(R.id.grid_layout_servicios);
        btnReservarAhora = findViewById(R.id.btn_reservar_ahora);

        // Configurar Toolbar como ActionBar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Mostrar botón atrás
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Listener para el botón atrás de la toolbar
        toolbar.setNavigationOnClickListener(v -> finish());


        // --- Recibir datos del Intent ---
        if (getIntent().getExtras() != null) {
            habitacionId = getIntent().getStringExtra("HABITACION_ID");
            fechaLlegadaMillis = getIntent().getLongExtra("FECHA_LLEGADA", 0);
            fechaSalidaMillis = getIntent().getLongExtra("FECHA_SALIDA", 0);

            if (habitacionId == null || fechaLlegadaMillis == 0 || fechaSalidaMillis == 0) {
                Log.e(TAG, "Error: Faltan datos necesarios en el Intent.");
                Toast.makeText(this, "Error al cargar los detalles.", Toast.LENGTH_SHORT).show();
                finish(); // Salir si faltan datos
                return;
            }

            // (Aquí llamaremos a la función para cargar datos de Firestore)
            cargarDatosHabitacion();

        } else {
            Log.e(TAG, "Error: No se recibieron datos en el Intent.");
            Toast.makeText(this, "Error: No se recibieron datos.", Toast.LENGTH_SHORT).show();
            finish(); // Salir si no hay datos
        }

        // (PENDIENTE) Configurar botón "Reservar Ahora"
        // btnReservarAhora.setOnClickListener(v -> { ... });
    }

    private void cargarDatosHabitacion() {
        if (habitacionId == null) return;

        Log.d(TAG, "Cargando datos para habitación ID: " + habitacionId);
        // (Mostrar ProgressBar si tienes uno)

        DocumentReference docRef = db.collection("habitaciones").document(habitacionId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                habitacionActual = documentSnapshot.toObject(Habitacion.class);

                if (habitacionActual != null) {
                    Log.d(TAG, "Datos cargados: " + habitacionActual.getNombre());
                    // Una vez cargados los datos, actualizamos la UI
                    actualizarUI();
                    // (Ocultar ProgressBar)
                } else {
                    Log.e(TAG, "Error al convertir DocumentSnapshot a objeto Habitacion.");
                    Toast.makeText(this, "Error al procesar datos.", Toast.LENGTH_SHORT).show();
                    // (Ocultar ProgressBar)
                }
            } else {
                Log.e(TAG, "Error: No se encontró el documento con ID: " + habitacionId);
                Toast.makeText(this, "Error: Habitación no encontrada.", Toast.LENGTH_SHORT).show();
                // (Ocultar ProgressBar)
                finish(); // Salir si la habitación no existe
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al cargar datos de Firestore", e);
            Toast.makeText(this, "Error al cargar detalles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // (Ocultar ProgressBar)
            finish(); // Salir si hay error
        });
    }

    private void actualizarUI() {
        if (habitacionActual == null) return;

        // 1. Poner el Título en la barra colapsable
        collapsingToolbar.setTitle(habitacionActual.getNombre());

        // 2. Cargar la Imagen Principal con Glide
        if (habitacionActual.getImagenUrl() != null && !habitacionActual.getImagenUrl().isEmpty()) {
            Glide.with(this)
                    .load(habitacionActual.getImagenUrl())
                    .placeholder(R.drawable.logoboulevard) // Asegúrate de tener este placeholder
                    .into(ivImagen);
        } else {
            ivImagen.setImageResource(R.drawable.logoboulevard);
        }

        // 3. Poner la Descripción
        tvDescripcion.setText(habitacionActual.getDescripcion());

        // 4. Mostrar los Servicios (Llamamos al método que hicimos antes)
        mostrarServicios();

        // 5. Calcular y mostrar el Precio Total
        precioTotalCalculado = calcularPrecioTotal();
        String precioTotalStr = String.format(Locale.getDefault(), "%.2f€", precioTotalCalculado); // Ajusta la moneda
        tvPrecioTotal.setText(precioTotalStr);
    }

    // Dentro de la clase HabitacionDetalleActivity

    /**
     * Añade dinámicamente los iconos y textos de los servicios al GridLayout.
     */
    private void mostrarServicios() {
        if (habitacionActual == null || habitacionActual.getServicios() == null) return;

        gridLayoutServicios.removeAllViews(); // Limpiar vistas anteriores

        Map<String, Boolean> servicios = habitacionActual.getServicios();

        // Creamos una lista de servicios disponibles para iterar
        Map<String, Integer> iconosServicios = new HashMap<>();
        iconosServicios.put("wifi", R.drawable.ic_home);
        iconosServicios.put("aireAcondicionado", R.drawable.ic_home);
        iconosServicios.put("smartTV", R.drawable.ic_home); // Asegúrate que el ID del icono sea correcto
        iconosServicios.put("minibar", R.drawable.ic_home); // Necesitas este icono
        iconosServicios.put("cajaSeguridad", R.drawable.ic_home); // Necesitas este icono (lock)
        iconosServicios.put("servicioHabitacion", R.drawable.ic_home); // Usamos el de concierge como ejemplo
        iconosServicios.put("banoPrivado", R.drawable.ic_home); // Necesitas este icono (shower)
        iconosServicios.put("balconPrivado", R.drawable.ic_home); // Necesitas este icono (balcony)
        // ... añade más si es necesario

        Map<String, String> textosServicios = new HashMap<>();
        textosServicios.put("wifi", "Wi-Fi de alta velocidad");
        textosServicios.put("aireAcondicionado", "Aire acondicionado");
        textosServicios.put("smartTV", "Smart TV con streaming");
        textosServicios.put("minibar", "Minibar");
        textosServicios.put("cajaSeguridad", "Caja de seguridad");
        textosServicios.put("servicioHabitacion", "Servicio a la habitación 24h");
        textosServicios.put("banoPrivado", "Baño privado de lujo");
        textosServicios.put("balconPrivado", "Balcón privado");
        // ...

        for (Map.Entry<String, Boolean> entry : servicios.entrySet()) {
            if (entry.getValue()) { // Si el servicio está disponible (true)
                String key = entry.getKey();
                if (iconosServicios.containsKey(key) && textosServicios.containsKey(key)) {
                    // Inflar el mini-layout para el servicio
                    View servicioView = LayoutInflater.from(this).inflate(R.layout.item_servicio_detalle, gridLayoutServicios, false);

                    ImageView icono = servicioView.findViewById(R.id.iv_servicio_icono);
                    TextView texto = servicioView.findViewById(R.id.tv_servicio_texto);

                    icono.setImageResource(iconosServicios.get(key));
                    texto.setText(textosServicios.get(key));

                    // Añadir al GridLayout
                    gridLayoutServicios.addView(servicioView);
                }
            }
        }
    }

    private double calcularPrecioTotal() {
        if (habitacionActual == null || fechaLlegadaMillis == 0 || fechaSalidaMillis == 0 || fechaSalidaMillis <= fechaLlegadaMillis) {
            return 0.0;
        }

        // Calcular la diferencia en días
        long diffMillis = fechaSalidaMillis - fechaLlegadaMillis;
        long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);

        // Si la diferencia es 0 (misma fecha de entrada y salida), contamos como 1 noche
        if (diffDays == 0) {
            diffDays = 1;
        }

        return habitacionActual.getPrecioPorNoche() * diffDays;
    }

}