package com.example.hotelboulevard; // Reemplaza con tu paquete

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

// 1. Extendemos de FirestoreRecyclerAdapter
//    Pasamos nuestro Modelo (Habitacion) y nuestro ViewHolder (HabitacionViewHolder)
public class HabitacionAdminAdapter extends FirestoreRecyclerAdapter<Habitacion, HabitacionAdminAdapter.HabitacionViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    // 2. Constructor
    public HabitacionAdminAdapter(@NonNull FirestoreRecyclerOptions<Habitacion> options) {
        super(options);
        setHasStableIds(true); // Le dice al RecyclerView que los IDs son únicos y no cambian
    }

    // Este metodo devuelve el ID único para cada ítem. Usaremos el hashcode del ID del documento.
    @Override
    public long getItemId(int position) {
        // Asegúrate de que la posición es válida antes de acceder al snapshot
        try {
            if (position >= 0 && position < getItemCount() && getSnapshots() != null) {
                DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
                if (snapshot != null && snapshot.exists()) {
                    // Usamos el hashcode del ID del documento de Firestore como ID estable
                    return snapshot.getId().hashCode();
                }
            }
        } catch (Exception e) {
            // Si hay algún error, devolver NO_ID de forma segura
            return RecyclerView.NO_ID;
        }
        // Devuelve un ID inválido si la posición no es válida
        return RecyclerView.NO_ID;
    }

    // 3. onBindViewHolder: Conecta los datos (modelo) con las Vistas (ViewHolder)
    @Override
    protected void onBindViewHolder(@NonNull HabitacionViewHolder holder, int position, @NonNull Habitacion model) {
        // Protección adicional: verificar que la posición sigue siendo válida
        if (position < 0 || position >= getItemCount()) {
            return;
        }

        // Asignamos los datos del modelo a las vistas
        holder.tvNombre.setText(model.getNombre());

        // Formateamos el precio para que se vea bien
        String precioFormateado = String.format("$%.2f / noche", model.getPrecioPorNoche());
        holder.tvPrecio.setText(precioFormateado);

        // Cargar la imagen (Cloudinary) con Glide
        if (model.getImagenUrl() != null && !model.getImagenUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(model.getImagenUrl()) // <-- USA LA NUEVA VARIABLE
                    .placeholder(R.drawable.ic_hotel_placeholder)
                    .error(R.drawable.ic_hotel_placeholder)
                    .into(holder.ivImagen);
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_hotel_placeholder);
        }

        // --- Configuración de Clicks ---
        // Limpiar listeners previos para evitar clicks duplicados
        holder.btnEditar.setOnClickListener(null);
        holder.btnEliminar.setOnClickListener(null);

        holder.btnEditar.setOnClickListener(v -> {
            // Obtenemos la posición ACTUAL en el momento del clic
            int currentPosition = holder.getBindingAdapterPosition();

            if (listener != null && currentPosition != RecyclerView.NO_POSITION 
                    && currentPosition >= 0 && currentPosition < getItemCount()) {
                try {
                    listener.onEditarClick(getSnapshots().getSnapshot(currentPosition));
                } catch (Exception e) {
                    // Manejar cualquier error de forma segura
                    e.printStackTrace();
                }
            }
        });

        holder.btnEliminar.setOnClickListener(v -> {
            // Obtenemos la posición ACTUAL en el momento del clic
            int currentPosition = holder.getBindingAdapterPosition();

            if (listener != null && currentPosition != RecyclerView.NO_POSITION 
                    && currentPosition >= 0 && currentPosition < getItemCount()) {
                try {
                    listener.onEliminarClick(getSnapshots().getSnapshot(currentPosition));
                } catch (Exception e) {
                    // Manejar cualquier error de forma segura
                    e.printStackTrace();
                }
            }
        });
    }

    // 4. onCreateViewHolder: Crea un nuevo ViewHolder (inflando el layout del ítem)
    @NonNull
    @Override
    public HabitacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Obtenemos el contexto aquí para Glide
        this.context = parent.getContext();

        // Inflamos el layout XML de nuestro ítem
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habitacion_admin, parent, false);
        return new HabitacionViewHolder(view);
    }


    // 5. Clase ViewHolder: Contiene las vistas (Views) de nuestro layout 'item_habitacion_admin.xml'
    class HabitacionViewHolder extends RecyclerView.ViewHolder {

        // Declaramos las vistas del layout
        TextView tvNombre;
        TextView tvPrecio;
        ImageView ivImagen;
        MaterialButton btnEditar;
        MaterialButton btnEliminar;

        public HabitacionViewHolder(@NonNull View itemView) {
            super(itemView);

            // Enlazamos las vistas
            tvNombre = itemView.findViewById(R.id.tv_habitacion_nombre);
            tvPrecio = itemView.findViewById(R.id.tv_habitacion_precio);
            ivImagen = itemView.findViewById(R.id.iv_habitacion_imagen);
            btnEditar = itemView.findViewById(R.id.btn_editar);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }

    // --- Interfaz para manejar los Clicks ---
    // La Activity (ListarHabitacionesAdminActivity) implementará esta interfaz
    // para saber qué hacer cuando se presione "Editar" o "Eliminar".
    public interface OnItemClickListener {
        void onEditarClick(DocumentSnapshot documentSnapshot);
        void onEliminarClick(DocumentSnapshot documentSnapshot);
    }

    // Método para que la Activity "escuche" los clicks
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Override de onDataChanged para manejar mejor los cambios de Firestore
    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // Este método se llama cuando los datos de Firestore cambian
        // Puede ser útil para debugging o para actualizar la UI
    }

    // Override de onError para manejar errores de Firestore
    @Override
    public void onError(@NonNull com.google.firebase.firestore.FirebaseFirestoreException e) {
        super.onError(e);
        // Log del error para debugging
        android.util.Log.e("HabitacionAdapter", "Error al cargar datos de Firestore", e);
    }
}