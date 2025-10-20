package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    // --- Servicios de Firebase ---
    private FirebaseAuth mAuth;

    // --- Vistas de la UI ---
    private MaterialCardView cardGestionarHabitaciones;
    private MaterialCardView cardVerReservas;
    private MaterialCardView cardHuespedes;
    private MaterialCardView cardConfiguracion;
    private MaterialButton btnCerrarSesion;
    private ImageView btnBack; // Botón de retroceso

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // --- Enlazar Vistas ---
        cardGestionarHabitaciones = findViewById(R.id.card_gestionar_habitaciones);
        cardVerReservas = findViewById(R.id.card_ver_reservas);
        cardHuespedes = findViewById(R.id.card_huespedes);
        cardConfiguracion = findViewById(R.id.card_configuracion);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
        btnBack = findViewById(R.id.btn_back); // Enlazar el botón de retroceso

        // --- Configurar Listeners (Navegación) ---

        // 1. Botón Gestionar Habitaciones
        cardGestionarHabitaciones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AÚN NO LA HEMOS CREADO, pero definimos la intención
                // La app crasheará aquí, ¡y ese es nuestro siguiente paso!
                Intent intent = new Intent(AdminDashboardActivity.this, ListarHabitacionesAdminActivity.class);
                startActivity(intent);
            }
        });

        // 2. Botón Ver Reservas (sin acción por ahora)
        cardVerReservas.setOnClickListener(v ->
                Toast.makeText(AdminDashboardActivity.this, "Sección 'Reservas' en desarrollo", Toast.LENGTH_SHORT).show()
        );

        // 3. Botón Huéspedes (sin acción por ahora)
        cardHuespedes.setOnClickListener(v ->
                Toast.makeText(AdminDashboardActivity.this, "Sección 'Huéspedes' en desarrollo", Toast.LENGTH_SHORT).show()
        );

        // 4. Botón Configuración (sin acción por ahora)
        cardConfiguracion.setOnClickListener(v ->
                Toast.makeText(AdminDashboardActivity.this, "Sección 'Configuración' en desarrollo", Toast.LENGTH_SHORT).show()
        );

        // 5. Botón de Retroceso (btn_back)
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simplemente cierra esta actividad y vuelve a la anterior
                finish();
            }
        });

        // --- Configurar Cierre de Sesión ---
        btnCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); // Cierra la sesión de Firebase

                // Enviamos al usuario de vuelta al Login
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                // Limpiamos las Activities anteriores
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // Opcional pero recomendado:
    // Verificar si el usuario sigue logueado al iniciar esta pantalla
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            // Si por alguna razón el usuario ya no está logueado,
            // lo mandamos de vuelta al Login.
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}