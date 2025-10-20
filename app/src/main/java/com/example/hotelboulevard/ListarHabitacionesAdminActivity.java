package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.annotation.NonNull; // <-- ¡AÑADE ESTA LÍNEA!
import com.google.android.gms.tasks.OnFailureListener; // <-- ¡AÑADE ESTA LÍNEA!
import com.google.android.gms.tasks.OnSuccessListener; // <-- ¡AÑADE ESTA LÍNEA!
import android.util.Log;

// 1. Implementamos la interfaz de nuestro adaptador
public class ListarHabitacionesAdminActivity extends AppCompatActivity
        implements HabitacionAdminAdapter.OnItemClickListener {

    // --- Servicios de Firebase ---
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("habitaciones");

    // --- Vistas y Adaptador ---
    private HabitacionAdminAdapter adapter;
    private RecyclerView recyclerViewHabitaciones;
    private ImageButton btnBackToolbar;
    private ImageButton btnAddToolbar;
    private FloatingActionButton fabAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_habitaciones_admin);

        // --- Enlazar Vistas ---
        btnAddToolbar = findViewById(R.id.btn_add_toolbar);
        recyclerViewHabitaciones = findViewById(R.id.recycler_view_habitaciones);
        fabAgregar = findViewById(R.id.fab_agregar);
        btnBackToolbar = findViewById(R.id.btn_back_toolbar);

        // --- Configurar Listeners para Añadir ---
        View.OnClickListener listenerParaAnadir = v -> {
            Intent intent = new Intent(ListarHabitacionesAdminActivity.this, GestionarHabitacionActivity.class);
            startActivity(intent);
            // Toast.makeText(ListarHabitacionesAdminActivity.this, "Abriendo formulario...", Toast.LENGTH_SHORT).show();
        };

        btnAddToolbar.setOnClickListener(listenerParaAnadir);
        fabAgregar.setOnClickListener(listenerParaAnadir);

        btnBackToolbar.setOnClickListener(new View.OnClickListener() { // <-- AÑADE ESTE BLOQUE
            @Override
            public void onClick(View v) {
                finish(); // Cierra esta pantalla y vuelve al Dashboard
            }
        });

        // --- Configurar el RecyclerView ---
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        // 2. Creamos la consulta a Firestore
        //    (Queremos todos los documentos de "tiposDeHabitacion")
        Query query = notebookRef.orderBy("nombre", Query.Direction.ASCENDING);

        // 3. Configuramos las opciones para el FirestoreRecyclerAdapter
        FirestoreRecyclerOptions<Habitacion> options = new FirestoreRecyclerOptions.Builder<Habitacion>()
                .setQuery(query, Habitacion.class)
                .build();

        // 4. Inicializamos el Adaptador
        adapter = new HabitacionAdminAdapter(options);

        // 5. Configuramos el RecyclerView
        recyclerViewHabitaciones.setHasFixedSize(true);
        recyclerViewHabitaciones.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabitaciones.setAdapter(adapter);

        // 6. Asignamos el listener para los clics
        adapter.setOnItemClickListener(this);
    }

    // --- Implementación de los clics del Adaptador ---

    @Override
    public void onEditarClick(DocumentSnapshot documentSnapshot) {
        // Obtenemos el ID del documento presionado
        String id = documentSnapshot.getId();
        Toast.makeText(this, "Editar habitación con ID: " + id, Toast.LENGTH_SHORT).show();

        // Aquí es donde abriremos GestionarHabitacionActivity
        Intent intent = new Intent(this, GestionarHabitacionActivity.class);
        intent.putExtra("HABITACION_ID", id); // Le pasamos el ID para "modo edición"
        startActivity(intent);
    }

    @Override
    public void onEliminarClick(DocumentSnapshot documentSnapshot) {
        // Obtenemos el ID y el nombre de la habitación
        String id = documentSnapshot.getId();
        Habitacion habitacion = documentSnapshot.toObject(Habitacion.class);
        String nombre = (habitacion != null && habitacion.getNombre() != null) ? habitacion.getNombre() : "esta habitación";

        // 1. Construir el Diálogo de Confirmación
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar '" + nombre + "'? Esta acción no se puede deshacer.")

                // 2. Botón "Eliminar" (Positivo)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 3. Si confirma, llamar al método para borrar
                        eliminarHabitacionDeFirestore(id);
                    }
                })

                // 4. Botón "Cancelar" (Negativo)
                .setNegativeButton("Cancelar", null) // 'null' significa que solo cierra el diálogo
                .setIcon(R.drawable.ic_delete) // Opcional: añade un ícono de basura
                .show(); // ¡No olvides mostrarlo!
    }
    private void eliminarHabitacionDeFirestore(String id) {
        if (id == null) return;

        db.collection("habitaciones").document(id).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // ¡Éxito!
                        Toast.makeText(ListarHabitacionesAdminActivity.this, "Habitación eliminada", Toast.LENGTH_SHORT).show();
                        // El FirestoreRecyclerAdapter actualizará la lista automáticamente
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ListarHabitacionesAdminActivity.this, "Error al eliminar la habitación", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error al eliminar", e);
                    }
                });
    }


    // 7. --- Ciclo de Vida del Adaptador ---
    //    Esto es MUY IMPORTANTE para que la lista se actualice en tiempo real.

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening(); // El adaptador empieza a "escuchar" cambios
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening(); // El adaptador deja de "escuchar"
        }
    }
}