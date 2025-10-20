package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar; // <-- Importa ProgressBar
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // --- Vistas de la UI ---
    private TextInputEditText etNombreCompleto, etEmail, etPassword, etConfirmarPassword;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private ImageButton btnBack;
    private ProgressBar progressBar; // <-- ProgressBar

    // --- Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Enlazar Vistas
        etNombreCompleto = findViewById(R.id.et_nombre_completo_register);
        etEmail = findViewById(R.id.et_email_register);
        etPassword = findViewById(R.id.et_password_register);
        etConfirmarPassword = findViewById(R.id.et_confirmar_password_register);
        btnRegister = findViewById(R.id.btn_register);
        tvGoToLogin = findViewById(R.id.tv_go_to_login);
        btnBack = findViewById(R.id.btn_back_register);

        // Asume que tienes un <ProgressBar> en tu XML con este ID
        progressBar = findViewById(R.id.progress_bar_register);

        // --- Configurar Listeners ---

        // Botón "Atrás"
        btnBack.setOnClickListener(v -> finish());

        // Botón "Ir a Login"
        tvGoToLogin.setOnClickListener(v -> finish()); // 'finish()' cierra esta pantalla y vuelve a Login

        // Botón "Registrarse"
        btnRegister.setOnClickListener(v -> {
            // Llamamos al método principal de registro
            registrarUsuario();
        });
    }

    private void registrarUsuario() {
        // Obtener texto de los campos
        String nombre = etNombreCompleto.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmarPassword = etConfirmarPassword.getText().toString().trim();

        // --- Validaciones ---
        if (nombre.isEmpty()) {
            etNombreCompleto.setError("El nombre es obligatorio");
            etNombreCompleto.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("El correo es obligatorio");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Por favor, ingrese un correo válido");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("La contraseña es obligatoria");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmarPassword)) {
            etConfirmarPassword.setError("Las contraseñas no coinciden");
            etConfirmarPassword.requestFocus();
            return;
        }

        // Si todo es válido, mostramos el ProgressBar y ocultamos el botón
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show();

        // --- PASO 1: Crear Usuario en Firebase Authentication ---
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // ¡Éxito en Auth!
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // --- PASO 2: Guardar datos en Firestore ---
                                guardarUsuarioEnFirestore(user.getUid(), nombre, email);
                            }
                        } else {
                            // Falló el registro en Auth (ej. el correo ya existe)
                            Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            btnRegister.setEnabled(true);
                        }
                    }
                });
    }

    /**
     * Guarda la información adicional del usuario en Firestore
     * (nombre y ROL)
     */
    private void guardarUsuarioEnFirestore(String uid, String nombre, String email) {
        // Creamos un Map (diccionario) con los datos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("nombre", nombre);
        userData.put("email", email);
        userData.put("rol", "usuario"); // <-- ¡El rol del cliente!

        // Guardamos en la colección "usuarios", usando el UID como ID del documento
        db.collection("usuarios").document(uid).set(userData)
                .addOnSuccessListener(aVoid -> {
                    // ¡Éxito en Firestore!
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);

                    Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();

                    // El usuario ya está logueado, lo mandamos al Home
                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    // Limpiamos las pantallas anteriores (Login, Register)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(LoginActivity.EXTRA_USER_NAME, nombre); // Pasamos el nombre
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Falló el guardado en Firestore
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                });
    }
}