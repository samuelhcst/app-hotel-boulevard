package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GestionarHabitacionActivity extends AppCompatActivity {

    // --- ¡REEMPLAZA ESTO! ---
    private final String UPLOAD_PRESET = "android_upload";

    // --- Vistas de la UI ---
    private MaterialToolbar toolbar;
    private TextInputEditText etNombre, etDescripcion, etPrecio, etCantidadTotal;
    private AutoCompleteTextView actTipo;
    private ImageView ivImagenPreview;
    private MaterialButton btnSeleccionarImagen, btnGuardar;
    private CheckBox cbWifi, cbSmartTv, cbAireAcondicionado, cbMinibar;
    private CheckBox cbCajaSeguridad, cbServicioHabitacion, cbBanoPrivado, cbBalconPrivado;
    private ProgressBar progressBar;
    private boolean isSaving = false; // Indicador de guardado

    // --- Lógica ---
    private Uri imagenUri = null;
    private ActivityResultLauncher<String> mGetContent;
    private FirebaseFirestore db;

    // --- ¡NUEVAS VARIABLES PARA MODO EDICIÓN! ---
    private String habitacionId = null;
    private boolean isEditMode = false;
    private String imagenUrlActual = null; // Para guardar la URL si estamos editando
    private String textoBotonOriginal = ""; // Para guardar el texto original del botón
    // ------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestionar_habitacion_admin);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // --- Enlazar Vistas ---
        toolbar = findViewById(R.id.toolbar_gestionar);
        etNombre = findViewById(R.id.et_nombre);
        etDescripcion = findViewById(R.id.et_descripcion);
        etPrecio = findViewById(R.id.et_precio);
        etCantidadTotal = findViewById(R.id.et_cantidad_total);
        actTipo = findViewById(R.id.act_tipo);
        ivImagenPreview = findViewById(R.id.iv_imagen_preview);
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen);
        btnGuardar = findViewById(R.id.btn_guardar);

        // Checkboxes
        cbWifi = findViewById(R.id.cb_wifi);
        cbSmartTv = findViewById(R.id.cb_smart_tv);
        cbAireAcondicionado = findViewById(R.id.cb_aire_acondicionado);
        cbMinibar = findViewById(R.id.cb_minibar);
        cbCajaSeguridad = findViewById(R.id.cb_caja_seguridad);
        cbServicioHabitacion = findViewById(R.id.cb_servicio_habitacion);
        cbBanoPrivado = findViewById(R.id.cb_bano_privado);
        cbBalconPrivado = findViewById(R.id.cb_balcon_privado);

        // (Añade un ProgressBar a tu XML o comenta esta línea)
        // progressBar = findViewById(R.id.progress_bar);

        // --- Configurar Toolbar y Dropdown ---
        toolbar.setNavigationOnClickListener(v -> finish());
        setupTipoDropdown();

        // --- Configurar Selector de Imagen (sin cambios) ---
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imagenUri = uri; // El usuario seleccionó una *nueva* imagen
                        imagenUrlActual = null; // Borramos la URL antigua
                        Glide.with(this).load(imagenUri).into(ivImagenPreview);
                    }
                });

        btnSeleccionarImagen.setOnClickListener(v -> mGetContent.launch("image/*"));

        // --- ¡NUEVO! LÓGICA DEL BOTÓN "GUARDAR" (Actualizada) ---
        btnGuardar.setOnClickListener(v -> {
            if (isSaving) { // Si ya estamos guardando, no hacer nada
                return;
            }
            isSaving = true; // Marcar como guardando
            
            // Guardar el texto original del botón si no lo hemos guardado aún
            if (textoBotonOriginal.isEmpty()) {
                textoBotonOriginal = btnGuardar.getText().toString();
            }
            
            // Cambiar el texto para indicar que está procesando
            btnGuardar.setText(isEditMode ? "Actualizando..." : "Guardando...");
            btnGuardar.setEnabled(false); // Deshabilitar botón

            if (isEditMode) {
                actualizarHabitacion();
            } else {
                guardarNuevaHabitacion();
            }
        });

        // --- ¡NUEVO! VERIFICAR MODO CREAR O EDITAR ---
        if (getIntent().hasExtra("HABITACION_ID")) {
            // --- MODO EDICIÓN ---
            isEditMode = true;
            habitacionId = getIntent().getStringExtra("HABITACION_ID");

            // Cambiamos el UI
            toolbar.setTitle("Editar Habitación");
            btnGuardar.setText("Actualizar Habitación");
            textoBotonOriginal = "Actualizar Habitación"; // Guardar texto original

            // Cargamos los datos
            loadHabitacionData();

        } else {
            // --- MODO CREAR ---
            isEditMode = false;
            toolbar.setTitle("Crear Nueva Habitación");
            btnGuardar.setText("Guardar Habitación");
            textoBotonOriginal = "Guardar Habitación"; // Guardar texto original
        }
    }

    /**
     * ¡NUEVO! Carga los datos de Firestore en el formulario.
     */
    private void loadHabitacionData() {
        if (habitacionId == null) return;

        DocumentReference docRef = db.collection("habitaciones").document(habitacionId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Convertimos el documento al modelo Habitacion
                Habitacion habitacion = documentSnapshot.toObject(Habitacion.class);

                if (habitacion != null) {
                    // 1. Rellenar campos de texto
                    etNombre.setText(habitacion.getNombre());
                    etDescripcion.setText(habitacion.getDescripcion());
                    etPrecio.setText(String.valueOf(habitacion.getPrecioPorNoche()));
                    etCantidadTotal.setText(String.valueOf(habitacion.getCantidadTotal()));

                    // 2. Rellenar Dropdown (tipo)
                    actTipo.setText(habitacion.getTipo(), false); // 'false' para no filtrar

                    // 3. Rellenar Checkboxes (Servicios)
                    Map<String, Boolean> servicios = habitacion.getServicios();
                    if (servicios != null) {
                        cbWifi.setChecked(servicios.getOrDefault("wifi", false));
                        cbSmartTv.setChecked(servicios.getOrDefault("smartTV", false));
                        cbAireAcondicionado.setChecked(servicios.getOrDefault("aireAcondicionado", false));
                        cbMinibar.setChecked(servicios.getOrDefault("minibar", false));
                        cbCajaSeguridad.setChecked(servicios.getOrDefault("cajaSeguridad", false));
                        cbServicioHabitacion.setChecked(servicios.getOrDefault("servicioHabitacion", false));
                        cbBanoPrivado.setChecked(servicios.getOrDefault("banoPrivado", false));
                        cbBalconPrivado.setChecked(servicios.getOrDefault("balconPrivado", false));
                    }

                    // 4. Cargar Imagen Principal
                    imagenUrlActual = habitacion.getImagenUrl();
                    if (imagenUrlActual != null && !imagenUrlActual.isEmpty()) {
                        Glide.with(this)
                                .load(imagenUrlActual)
                                .placeholder(R.drawable.ic_hotel_placeholder)
                                .into(ivImagenPreview);
                    }
                }
            } else {
                Toast.makeText(this, "Error: No se encontró la habitación.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void guardarNuevaHabitacion() {
        // 1. Validaciones
        if (!validarCampos()) {
            // Rehabilitar el botón si la validación falla
            restaurarBotonGuardar();
            return;
        }

        if (imagenUri == null) {
            // Rehabilitar el botón si no hay imagen
            restaurarBotonGuardar();
            Toast.makeText(this, "Por favor, seleccione una imagen principal", Toast.LENGTH_SHORT).show();
            return;
        }

        // (Mostrar ProgressBar)
        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_LONG).show();

        // 2. Subir Imagen a Cloudinary
        MediaManager.get().upload(imagenUri)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Subida iniciada...");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("Cloudinary", "Subida exitosa: " + resultData.get("url").toString());

                        String urlImagenPrincipal;

                        if (resultData.containsKey("secure_url")) {
                            // Opción 1 (Ideal): Usamos la URL segura que nos da
                            urlImagenPrincipal = resultData.get("secure_url").toString();
                        } else if (resultData.containsKey("url")) {
                            // Opción 2 (Plan B): Tomamos la URL http y la forzamos a https
                            urlImagenPrincipal = resultData.get("url").toString().replace("http://", "https://");
                        } else {
                            // Si falla todo
                            restaurarBotonGuardar();
                            Log.e("Cloudinary", "No se encontró 'url' ni 'secure_url' en la respuesta");
                            Toast.makeText(GestionarHabitacionActivity.this, "Error: No se pudo obtener la URL de la imagen", Toast.LENGTH_SHORT).show();
                            return; // No continuar
                        }
                        Log.d("Cloudinary", "URL Segura Obtenida: " + urlImagenPrincipal);

                        // 3. Guardar en Firestore (Esto ya lo tenías)
                        guardarDatosEnFirestore(urlImagenPrincipal);
                    }

                    // --- ¡Y AQUÍ! ---
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        restaurarBotonGuardar();
                        Log.e("Cloudinary", "Error al subir: " + error.getDescription());
                        Toast.makeText(GestionarHabitacionActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        // (Ocultar ProgressBar)
                        // progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d("Cloudinary", "Subida re-programada");
                    }
                })
                .dispatch();
    }

    /**
     * ¡NUEVO! Lógica para actualizar una habitación existente.
     */
    private void actualizarHabitacion() {
        if (!validarCampos()) {
            // Rehabilitar el botón si la validación falla
            restaurarBotonGuardar();
            return;
        }

        // (Mostrar ProgressBar)
        Toast.makeText(this, "Actualizando...", Toast.LENGTH_LONG).show();

        if (imagenUri != null) {
            // CASO 1: El usuario seleccionó una NUEVA imagen
            MediaManager.get().upload(imagenUri)
                    .unsigned(UPLOAD_PRESET)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) { }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) { }

                        // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Log.d("Cloudinary", "Respuesta de Cloudinary: " + resultData.toString());

                            String nuevaUrl;

                            // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
                            if (resultData.containsKey("secure_url")) {
                                nuevaUrl = resultData.get("secure_url").toString();
                            } else if (resultData.containsKey("url")) {
                                nuevaUrl = resultData.get("url").toString().replace("http://", "https://");
                            } else {
                                restaurarBotonGuardar();
                                Log.e("Cloudinary", "No se encontró URL en la respuesta");
                                Toast.makeText(GestionarHabitacionActivity.this, "Error al obtener URL", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // --- FIN DEL CAMBIO ---

                            Log.d("Cloudinary", "URL Segura (Actualización): " + nuevaUrl);
                            actualizarDatosEnFirestore(nuevaUrl);
                        }

                        // --- ¡Y AQUÍ! ---
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            restaurarBotonGuardar();
                            Toast.makeText(GestionarHabitacionActivity.this, "Error al subir nueva imagen", Toast.LENGTH_SHORT).show();
                            // (Ocultar ProgressBar)
                            // progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) { }
                    })
                    .dispatch();

        } else {
            // CASO 2: El usuario NO cambió la imagen
            actualizarDatosEnFirestore(imagenUrlActual);
        }
    }

    /**
     * ¡NUEVO! Reúne los datos del formulario y los GUARDA en Firestore (Modo Crear)
     */
    private void guardarDatosEnFirestore(String urlImagen) {
        // Crea el objeto Habitación
        Habitacion habitacion = crearObjetoHabitacion(urlImagen);

        // Guardamos en la colección "habitaciones"
        db.collection("habitaciones").add(habitacion)
                .addOnSuccessListener(documentReference -> {
                    restaurarBotonGuardar();
                    Toast.makeText(GestionarHabitacionActivity.this, "Habitación guardada", Toast.LENGTH_SHORT).show();
                    //finish(); // Volver a la lista
                    // Creamos un Intent para ir DIRECTAMENTE a la lista
                    Intent intent = new Intent(GestionarHabitacionActivity.this, ListarHabitacionesAdminActivity.class);

                    // Flags importantes:
                    // FLAG_ACTIVITY_CLEAR_TOP: Si ya existe una instancia de la lista, elimina las que estén encima y trae esa al frente.
                    // FLAG_ACTIVITY_SINGLE_TOP: Si la lista ya está en la cima, no crea una nueva, reutiliza la existente.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent); // Iniciamos la lista
                    finish(); // Cerramos esta pantalla (el formulario) AHORA SÍ.
                })
                .addOnFailureListener(e -> {
                    restaurarBotonGuardar();
                    Toast.makeText(GestionarHabitacionActivity.this, "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ¡NUEVO! Reúne los datos del formulario y los ACTUALIZA en Firestore (Modo Editar)
     */
    private void actualizarDatosEnFirestore(String urlImagen) {
        if (habitacionId == null) return;

        // Crea el objeto Habitación
        Habitacion habitacion = crearObjetoHabitacion(urlImagen);

        // Usamos .document(id).set() para sobrescribir el documento
        db.collection("habitaciones").document(habitacionId).set(habitacion)
                .addOnSuccessListener(aVoid -> {
                    restaurarBotonGuardar();
                    Toast.makeText(GestionarHabitacionActivity.this, "Habitación actualizada", Toast.LENGTH_SHORT).show();
                    //finish(); // Volver a la lista
                    // Creamos un Intent para ir DIRECTAMENTE a la lista
                    Intent intent = new Intent(GestionarHabitacionActivity.this, ListarHabitacionesAdminActivity.class);

                    // Flags importantes:
                    // FLAG_ACTIVITY_CLEAR_TOP: Si ya existe una instancia de la lista, elimina las que estén encima y trae esa al frente.
                    // FLAG_ACTIVITY_SINGLE_TOP: Si la lista ya está en la cima, no crea una nueva, reutiliza la existente.
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    startActivity(intent); // Iniciamos la lista
                    finish(); // Cerramos esta pantalla (el formulario) AHORA SÍ.
                })
                .addOnFailureListener(e -> {
                    restaurarBotonGuardar();
                    Toast.makeText(GestionarHabitacionActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * ¡NUEVO! Método ayudante para validar los campos
     */
    private boolean validarCampos() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = actTipo.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String cantidadStr = etCantidadTotal.getText().toString().trim();

        if (nombre.isEmpty() || descripcion.isEmpty() || tipo.isEmpty() || precioStr.isEmpty() || cantidadStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // En modo "Crear", la imagen es obligatoria.
        // En modo "Editar", no (porque ya existe una).
        if (!isEditMode && imagenUri == null) {
            Toast.makeText(this, "Por favor, seleccione una imagen principal", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * ¡NUEVO! Método ayudante que lee el formulario y crea un objeto Habitación
     */
    private Habitacion crearObjetoHabitacion(String urlImagen) {
        double precio = Double.parseDouble(etPrecio.getText().toString());
        long cantidad = Long.parseLong(etCantidadTotal.getText().toString());

        Map<String, Boolean> serviciosMap = new HashMap<>();
        serviciosMap.put("wifi", cbWifi.isChecked());
        serviciosMap.put("smartTV", cbSmartTv.isChecked());
        serviciosMap.put("aireAcondicionado", cbAireAcondicionado.isChecked());
        serviciosMap.put("minibar", cbMinibar.isChecked());
        serviciosMap.put("cajaSeguridad", cbCajaSeguridad.isChecked());
        serviciosMap.put("servicioHabitacion", cbServicioHabitacion.isChecked());
        serviciosMap.put("banoPrivado", cbBanoPrivado.isChecked());
        serviciosMap.put("balconPrivado", cbBalconPrivado.isChecked());

        Habitacion habitacion = new Habitacion();
        habitacion.setNombre(etNombre.getText().toString().trim());
        habitacion.setDescripcion(etDescripcion.getText().toString().trim());
        habitacion.setTipo(actTipo.getText().toString().trim());
        habitacion.setPrecioPorNoche(precio);
        habitacion.setCantidadTotal(cantidad);
        habitacion.setServicios(serviciosMap);
        habitacion.setImagenUrl(urlImagen);
        // (La galería 'imagenes' sigue vacía por ahora)

        return habitacion;
    }

    /**
     * Método helper para restaurar el estado del botón Guardar
     */
    private void restaurarBotonGuardar() {
        isSaving = false;
        btnGuardar.setEnabled(true);
        if (!textoBotonOriginal.isEmpty()) {
            btnGuardar.setText(textoBotonOriginal);
        }
    }

    private void setupTipoDropdown() {
        // (Este método se queda igual)
        String[] tipos = getResources().getStringArray(R.array.tipos_de_habitacion);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tipos);
        actTipo.setAdapter(adapter);
    }
}