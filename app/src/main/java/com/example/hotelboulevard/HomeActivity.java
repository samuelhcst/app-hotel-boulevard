package com.example.hotelboulevard; // Asegúrate que tu paquete sea el correcto

// Imports necesarios (limpiados)
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton; // Para el botón de buscar
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Ya no necesitamos DatePickerDialog, DatePicker, Calendar, ni Button normal
// import android.app.DatePickerDialog;
// import android.widget.Button;
// import android.widget.DatePicker;
// import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    // Constantes para pasar datos (están bien)
    public static final String EXTRA_CHECK_IN = "com.example.hotelboulevard.CHECK_IN";
    public static final String EXTRA_CHECK_OUT = "com.example.hotelboulevard.CHECK_OUT";

    // --- Vistas de la UI ---
    private TextView tvWelcomeUser; // Cambiado de greetingTextView
    private MaterialButton btnBuscarDisponibilidad; // Usamos el nombre y tipo correcto
    private TextInputEditText etFechaLlegada; // Cambiado de checkInEditText
    private TextInputEditText etFechaSalida; // Cambiado de checkOutEditText
    private TextInputEditText etHuespedes;

    // --- Variables de Lógica ---
    private String currentUserName; // Para guardar el nombre del usuario
    private Long fechaLlegadaTimestamp = null; // Para guardar la fecha como número
    private Long fechaSalidaTimestamp = null; // Para guardar la fecha como número

    // Ya no necesitamos myCalendar
    // private final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- Enlazar Vistas (Usando los IDs correctos del XML) ---
        tvWelcomeUser = findViewById(R.id.tv_welcome_user);
        btnBuscarDisponibilidad = findViewById(R.id.btn_buscar_disponibilidad);
        etFechaLlegada = findViewById(R.id.et_fecha_llegada);
        etFechaSalida = findViewById(R.id.et_fecha_salida);
        etHuespedes = findViewById(R.id.et_huespedes);

        // --- Obtener nombre de usuario ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(LoginActivity.EXTRA_USER_NAME)) {
            currentUserName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME);
        } else {
            currentUserName = "Usuario"; // Valor por defecto
        }
        tvWelcomeUser.setText("Bienvenido, " + currentUserName); // Actualizado el saludo

        // --- Configurar Listeners ---

        // 1. Para los campos de fecha (Usando MaterialDatePicker)
        etFechaLlegada.setOnClickListener(v -> mostrarDatePicker(etFechaLlegada, true));
        etFechaSalida.setOnClickListener(v -> mostrarDatePicker(etFechaSalida, false));

        // 2. Para el botón "Buscar Disponibilidad" (Usando la lógica nueva)
        btnBuscarDisponibilidad.setOnClickListener(v -> {
            // Obtener datos
            String huespedesStr = etHuespedes.getText().toString().trim();

            // Validar
            if (fechaLlegadaTimestamp == null || fechaSalidaTimestamp == null) {
                Toast.makeText(HomeActivity.this, "Por favor, seleccione ambas fechas", Toast.LENGTH_SHORT).show();
                return;
            }
            if (fechaSalidaTimestamp <= fechaLlegadaTimestamp) {
                Toast.makeText(HomeActivity.this, "La fecha de salida debe ser posterior a la de llegada", Toast.LENGTH_SHORT).show();
                return;
            }
            if (huespedesStr.isEmpty()) {
                etHuespedes.setError("Ingrese el número de huéspedes");
                etHuespedes.requestFocus();
                return;
            }
            int numHuespedes;
            try {
                numHuespedes = Integer.parseInt(huespedesStr);
                if (numHuespedes <= 0) {
                    etHuespedes.setError("El número de huéspedes debe ser mayor a 0");
                    etHuespedes.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etHuespedes.setError("Ingrese un número válido");
                etHuespedes.requestFocus();
                return;
            }

            // Navegar (si es válido)
            Toast.makeText(HomeActivity.this, "Buscando...", Toast.LENGTH_SHORT).show();

            // Preparamos el Intent para la siguiente pantalla (ResultadosBusquedaActivity)
            // Asegúrate de crear esta Activity después.
            Intent searchIntent = new Intent(HomeActivity.this, ResultadosBusquedaActivity.class);
            searchIntent.putExtra("FECHA_LLEGADA", fechaLlegadaTimestamp); // Pasamos el timestamp
            searchIntent.putExtra("FECHA_SALIDA", fechaSalidaTimestamp); // Pasamos el timestamp
            searchIntent.putExtra("NUM_HUESPEDES", numHuespedes); // Pasamos el número
            startActivity(searchIntent);
        });

        // Ya no necesitamos el listener viejo de searchRoomsButton
        /* searchRoomsButton.setOnClickListener(new View.OnClickListener() { ... }); */

        // Tampoco necesitamos el botón myReservationsButton aquí (se maneja con BottomNav)
        /* myReservationsButton = findViewById(R.id.myReservationsButton);
           myReservationsButton.setOnClickListener(...) */
    }

    // Ya no necesitamos estos métodos que usaban DatePickerDialog
    // private void setupDatePickers() { ... }
    // private void updateLabel(TextInputEditText editText) { ... }

    /**
     * Muestra un MaterialDatePicker y pone la fecha seleccionada en el EditText.
     * Guarda el timestamp seleccionado.
     */
    private void mostrarDatePicker(TextInputEditText etFecha, boolean isFechaLlegada) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Seleccionar Fecha");
        builder.setSelection(MaterialDatePicker.todayInUtcMilliseconds());
        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Formatear para mostrar en el EditText
            Date date = new Date(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String fechaFormateada = sdf.format(date);
            etFecha.setText(fechaFormateada);

            // Guardar el timestamp (número)
            if (isFechaLlegada) {
                fechaLlegadaTimestamp = selection;
            } else {
                fechaSalidaTimestamp = selection;
            }
        });

        datePicker.show(getSupportFragmentManager(), isFechaLlegada ? "DATE_PICKER_LLEGADA" : "DATE_PICKER_SALIDA");
    }
}