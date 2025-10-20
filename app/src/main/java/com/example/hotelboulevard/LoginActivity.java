package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_USER_NAME = "com.example.hotelboulevard.USER_NAME";

    // --- Vistas de la UI ---
    private ImageView loginLogo;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView forgotPasswordTextView;
    private TextView registerTextView;

    // --- Servicios de Firebase ---
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // --- Inicializar Firebase Auth y Firestore ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- Enlazar Vistas ---
        loginLogo = findViewById(R.id.login_logo);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordTextView = findViewById(R.id.forgot_password_textview);
        registerTextView = findViewById(R.id.register_textview);

        // --- Lógica de Login ---
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailInput = emailEditText.getText().toString().trim();
                String passwordInput = passwordEditText.getText().toString().trim();

                // Validaciones básicas
                if (emailInput.isEmpty()) {
                    emailEditText.setError("El correo es obligatorio");
                    return;
                }
                if (passwordInput.isEmpty()) {
                    passwordEditText.setError("La contraseña es obligatoria");
                    return;
                }

                // 1. Iniciar sesión con Firebase Authentication
                mAuth.signInWithEmailAndPassword(emailInput, passwordInput)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // ¡Contraseña y email correctos!
                                    // Ahora verificamos el rol en Firestore
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    checkUserRole(user);
                                } else {
                                    // Error de autenticación (usuario no existe, contraseña incorrecta)
                                    Toast.makeText(LoginActivity.this, "Error: Correo o contraseña incorrectos.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // --- Lógica de Registro ---
        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Verifica el rol del usuario en Firestore y redirige a la Activity correcta.
     */
    private void checkUserRole(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;

        String uid = firebaseUser.getUid();

        // 2. Consultar el documento del usuario en Firestore usando su UID
        DocumentReference docRef = db.collection("usuarios").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {

                        // 3. ¡Encontramos el documento! Leemos el campo "rol"
                        String rol = document.getString("rol");
                        String nombre = document.getString("nombre"); // Obtenemos el nombre

                        if ("admin".equals(rol)) {
                            // 4. Si el rol es "admin", vamos al AdminDashboard
                            Toast.makeText(LoginActivity.this, "Bienvenido Admin", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                            startActivity(intent);
                            finish(); // Cerramos LoginActivity
                        } else {
                            // 5. Si el rol es "usuario" (o cualquier otro), vamos al Home
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra(EXTRA_USER_NAME, nombre); // Le pasamos el nombre
                            startActivity(intent);
                            finish(); // Cerramos LoginActivity
                        }
                    } else {
                        // Esto pasa si el usuario existe en Auth pero no en Firestore
                        // (Ej. se borró la base de datos, pero no el usuario de Auth)
                        Toast.makeText(LoginActivity.this, "Error: No se encontraron datos de usuario.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error al intentar leer Firestore
                    Toast.makeText(LoginActivity.this, "Error al verificar rol: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Opcional: Si el usuario ya inició sesión, mandarlo directo
    /*
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya hay un usuario logueado, verificamos su rol y lo mandamos
            // a la pantalla correcta sin pedirle login de nuevo.
            checkUserRole(currentUser);
        }
    }
    */
}