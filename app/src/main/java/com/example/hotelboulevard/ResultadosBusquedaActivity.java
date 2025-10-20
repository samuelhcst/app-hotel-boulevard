package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // Importa View
import android.widget.ProgressBar; // Importa ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Importa NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager; // Importa LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener; // Importa OnCompleteListener
import com.google.android.gms.tasks.Task; // Importa Task
import com.google.android.gms.tasks.Tasks; // Importa Tasks
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp; // Importa Timestamp
import com.google.firebase.firestore.CollectionReference; // Importa CollectionReference
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query; // Importa Query
import com.google.firebase.firestore.QueryDocumentSnapshot; // Importa QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot; // Importa QuerySnapshot

import java.text.SimpleDateFormat;
import java.util.ArrayList; // Importa ArrayList
import java.util.Date;
import java.util.List; // Importa List
import java.util.Locale;

public class ResultadosBusquedaActivity extends AppCompatActivity
        implements HabitacionResultadoAdapter.OnItemClickListener {

    private static final String TAG = "ResultadosBusqueda"; // Para logs

    // --- Vistas ---
    private MaterialToolbar toolbar;
    private TextView tvResumenBusqueda;
    private MaterialButton btnCambiarBusqueda;
    private RecyclerView recyclerViewResultados;
    private ProgressBar progressBarResultados; // <-- ProgressBar para indicar carga

    // --- Firebase ---
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference habitacionesRef = db.collection("habitaciones");
    private CollectionReference reservasRef = db.collection("reservas");

    // --- Datos recibidos ---
    private long fechaLlegadaMillis;
    private long fechaSalidaMillis;
    private int numHuespedes;

    // --- Adaptador y Lista ---
    private HabitacionResultadoAdapter adapter;
    private List<Habitacion> habitacionesDisponibles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_busqueda);

        // --- Enlazar Vistas ---
        toolbar = findViewById(R.id.toolbar_resultados);
        tvResumenBusqueda = findViewById(R.id.tv_resumen_busqueda);
        btnCambiarBusqueda = findViewById(R.id.btn_cambiar_busqueda);
        recyclerViewResultados = findViewById(R.id.recycler_view_resultados);
        // (Añade un ProgressBar a tu XML o comenta la línea)
        // progressBarResultados = findViewById(R.id.progress_bar_resultados);

        // --- Configurar UI Inicial ---
        setupToolbar();
        setupRecyclerView();

        // --- Recibir datos y Mostrar Resumen ---
        if (!recibirYMostrarResumen()) {
            // Si hay error al recibir datos, no continuamos
            return;
        }

        // --- Iniciar la Búsqueda ---
        buscarHabitacionesDisponibles();
    }

    @Override
    public void onItemClick(String habitacionId) {
        // Abre la pantalla de detalles, pasando los datos necesarios
        Intent intent = new Intent(this, HabitacionDetalleActivity.class);
        intent.putExtra("HABITACION_ID", habitacionId);
        intent.putExtra("FECHA_LLEGADA", fechaLlegadaMillis);
        intent.putExtra("FECHA_SALIDA", fechaSalidaMillis);
        startActivity(intent);
    }

    // --- Configuración Inicial ---

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
        btnCambiarBusqueda.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        // Creamos el adaptador (aún no le pasamos datos)
        adapter = new HabitacionResultadoAdapter(habitacionesDisponibles, this); // Pasamos 'this' como contexto
        recyclerViewResultados.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResultados.setAdapter(adapter);

        adapter.setOnItemClickListener(this);
    }

    private boolean recibirYMostrarResumen() {
        if (getIntent().getExtras() != null) {
            fechaLlegadaMillis = getIntent().getLongExtra("FECHA_LLEGADA", 0);
            fechaSalidaMillis = getIntent().getLongExtra("FECHA_SALIDA", 0);
            numHuespedes = getIntent().getIntExtra("NUM_HUESPEDES", 1);

            // Validar timestamps recibidos
            if (fechaLlegadaMillis == 0 || fechaSalidaMillis == 0) {
                Toast.makeText(this, "Error: Fechas inválidas recibidas.", Toast.LENGTH_LONG).show();
                finish();
                return false;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String llegadaStr = sdf.format(new Date(fechaLlegadaMillis));
            String salidaStr = sdf.format(new Date(fechaSalidaMillis));
            String huespedesStr = numHuespedes + (numHuespedes == 1 ? " Huésped" : " Huéspedes");
            tvResumenBusqueda.setText(llegadaStr + " - " + salidaStr + ", " + huespedesStr);
            return true;
        } else {
            Toast.makeText(this, "Error: No se recibieron datos de búsqueda.", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
    }

    // --- Lógica Principal de Búsqueda ---

    private void buscarHabitacionesDisponibles() {
        Log.d(TAG, "Iniciando búsqueda...");
        // (Mostrar ProgressBar)
        // progressBarResultados.setVisibility(View.VISIBLE);
        habitacionesDisponibles.clear(); // Limpiar resultados anteriores

        // Convertir millis a Timestamps de Firebase para las consultas
        Timestamp fechaLlegadaTS = new Timestamp(new Date(fechaLlegadaMillis));
        Timestamp fechaSalidaTS = new Timestamp(new Date(fechaSalidaMillis));

        // 1. Obtener TODOS los tipos de habitación
        habitacionesRef.get().addOnCompleteListener(taskHabitaciones -> {
            if (taskHabitaciones.isSuccessful()) {
                List<Task<QuerySnapshot>> tasksReservas = new ArrayList<>();
                List<Habitacion> todosLosTipos = new ArrayList<>(); // Guardamos los tipos aquí temporalmente
                List<String> todosLosTiposIDs = new ArrayList<>(); // Y sus IDs

                // Guardamos todos los tipos y preparamos las tareas para buscar sus reservas
                for (QueryDocumentSnapshot docHabitacion : taskHabitaciones.getResult()) {
                    Habitacion tipoHabitacion = docHabitacion.toObject(Habitacion.class);
                    String habitacionId = docHabitacion.getId();

                    todosLosTipos.add(tipoHabitacion);
                    todosLosTiposIDs.add(habitacionId);

                    // 2. Por CADA tipo, creamos una TAREA para buscar sus reservas que se cruzan
                    Query consultaReservas = reservasRef
                            .whereEqualTo("idTipoHabitacion", habitacionId) // Reservas de ESTE tipo
                            .whereLessThan("fechaEntrada", fechaSalidaTS) // Que empiecen ANTES de que yo me vaya
                            .whereGreaterThan("fechaSalida", fechaLlegadaTS); // Y terminen DESPUÉS de que yo llegue

                    tasksReservas.add(consultaReservas.get()); // Añadimos la tarea a la lista
                }

                // 3. Ejecutamos TODAS las tareas de búsqueda de reservas EN PARALELO
                Tasks.whenAllSuccess(tasksReservas.toArray(new Task[0])).addOnSuccessListener(listaDeResultados -> {
                    // Cuando TODAS las búsquedas de reservas terminan...

                    for (int i = 0; i < listaDeResultados.size(); i++) {
                        QuerySnapshot resultadoReservas = (QuerySnapshot) listaDeResultados.get(i);
                        int cantidadReservada = resultadoReservas.size(); // Contamos cuántas reservas se encontraron

                        Habitacion tipoActual = todosLosTipos.get(i);
                        long cantidadTotal = tipoActual.getCantidadTotal();

                        Log.d(TAG, "Habitación: " + tipoActual.getNombre() +
                                " - Total: " + cantidadTotal +
                                " - Reservadas: " + cantidadReservada);

                        // 4. Comparamos: Si el total es MAYOR que las reservadas, ¡está disponible!
                        if (cantidadTotal > cantidadReservada) {
                            // 🔹 INICIO DE LA CORRECCIÓN: La variable 'tipoActual' ya existe, solo la usamos.
                            String idDelDocumento = todosLosTiposIDs.get(i); // <-- Obtenemos el ID que guardamos antes

                            tipoActual.setId(idDelDocumento); // <-- Guardamos el ID en el objeto
                            habitacionesDisponibles.add(tipoActual); // Añadimos a la lista final
                            // 🔹 FIN DE LA CORRECCIÓN
                        }
                    }

                    // 5. Actualizamos el RecyclerView con los resultados
                    Log.d(TAG, "Búsqueda completa. Habitaciones disponibles: " + habitacionesDisponibles.size());
                    adapter.notifyDataSetChanged(); // Notificamos al adaptador que hay nuevos datos
                    // (Ocultar ProgressBar)
                    // progressBarResultados.setVisibility(View.GONE);

                    if (habitacionesDisponibles.isEmpty()) {
                        Toast.makeText(this, "No se encontraron habitaciones disponibles para esas fechas.", Toast.LENGTH_LONG).show();
                    }

                }).addOnFailureListener(e -> {
                    // Error al buscar reservas
                    Log.e(TAG, "Error al buscar reservas", e);
                    Toast.makeText(this, "Error al verificar disponibilidad.", Toast.LENGTH_SHORT).show();
                    // (Ocultar ProgressBar)
                    // progressBarResultados.setVisibility(View.GONE);
                });

            } else {
                // Error al obtener los tipos de habitación
                Log.e(TAG, "Error al obtener tipos de habitación", taskHabitaciones.getException());
                Toast.makeText(this, "Error al cargar tipos de habitación.", Toast.LENGTH_SHORT).show();
                // (Ocultar ProgressBar)
                // progressBarResultados.setVisibility(View.GONE);
            }
        });
    }
}