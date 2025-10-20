package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Importa Button
import android.widget.ImageView;
import android.widget.LinearLayout; // Importa LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Importa Glide

import java.util.List;
import java.util.Locale; // Importa Locale

// Adaptador estándar (NO usa FirestoreRecyclerAdapter)
public class HabitacionResultadoAdapter extends RecyclerView.Adapter<HabitacionResultadoAdapter.HabitacionViewHolder> {

    private List<Habitacion> habitacionesList;
    private Context context;
    // (Aquí podríamos añadir un listener para el botón "Ver Detalles")
    // private OnItemClickListener listener;

    // Constructor
    public HabitacionResultadoAdapter(List<Habitacion> habitacionesList, Context context) {
        this.habitacionesList = habitacionesList;
        this.context = context;
    }

    @NonNull
    @Override
    public HabitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habitacion_resultado, parent, false);
        return new HabitacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitacionViewHolder holder, int position) {
        Habitacion habitacion = habitacionesList.get(position);

        // --- Llenar datos básicos ---
        holder.tvNombre.setText(habitacion.getNombre());
        String precioFormateado = String.format(Locale.getDefault(), "%.2f€", habitacion.getPrecioPorNoche()); // Ajusta el símbolo de moneda si es necesario
        holder.tvPrecio.setText(precioFormateado);

        // --- Cargar Imagen Principal ---
        if (habitacion.getImagenUrl() != null && !habitacion.getImagenUrl().isEmpty()) {
            Glide.with(context)
                    .load(habitacion.getImagenUrl())
                    .placeholder(R.drawable.ic_hotel_placeholder) // CORREGIDO
                    .error(R.drawable.ic_hotel_placeholder)     // CORREGIDO
                    .into(holder.ivImagen);
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_hotel_placeholder); // CORREGIDO
        }

        // --- (PENDIENTE) Lógica para los iconos de servicios ---
        // holder.layoutServiciosIconos.removeAllViews();
        // Map<String, Boolean> servicios = habitacion.getServicios();
        // if (servicios != null) { ... }

        holder.btnVerDetalles.setOnClickListener(v -> {
            // Obtenemos el ID que guardamos en el objeto Habitación
            String id = habitacion.getId();

            if (listener != null && id != null) {
                listener.onItemClick(id); // <-- Pasamos el ID real
            } else {
                Log.e("Adapter", "Error: No se pudo obtener el ID de la habitación en posición " + position);
                Toast.makeText(context, "Error al obtener ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitacionesList.size();
    }

    // --- ViewHolder ---
    static class HabitacionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvNombre;
        TextView tvPrecio;
        LinearLayout layoutServiciosIconos; // El contenedor para los iconos
        Button btnVerDetalles; // O MaterialButton si usas ese

        public HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.iv_habitacion_imagen_resultado);
            tvNombre = itemView.findViewById(R.id.tv_habitacion_nombre_resultado);
            tvPrecio = itemView.findViewById(R.id.tv_habitacion_precio_resultado);
            layoutServiciosIconos = itemView.findViewById(R.id.layout_servicios_iconos);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_detalles);
        }
    }

    // (Aquí iría la interfaz OnItemClickListener si la necesitamos)
    public interface OnItemClickListener {
        void onItemClick(String habitacionId);
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // public void setOnItemClickListener(OnItemClickListener listener) { this.listener = listener; }
}
