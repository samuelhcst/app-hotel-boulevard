package com.example.hotelboulevard;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;   // <-- necesario
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;               // <-- necesario
import androidx.recyclerview.widget.RecyclerView; // <-- necesario

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Room> roomList;
    private Context context;

    private String currentUserName;
    private String checkIn;
    private String checkOut;

    public RoomAdapter(Context context, List<Room> roomList, String currentUserName, String checkIn, String checkOut) {
        this.context = context;
        this.roomList = roomList;
        this.currentUserName = currentUserName;
        this.checkIn = (checkIn == null ? "" : checkIn);
        this.checkOut = (checkOut == null ? "" : checkOut);
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        holder.roomImageView.setImageResource(room.getImageId());
        holder.roomNameTextView.setText(room.getName());
        holder.ratingTextView.setText(String.valueOf(room.getRating()));
        holder.specsTextView.setText(room.getSpecs());
        holder.priceTextView.setText(room.getPrice());

        holder.selectButton.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_confirm_reservation, null);

            ImageView img = dialogView.findViewById(R.id.dialogImage);
            TextView tTitle = dialogView.findViewById(R.id.dialogTitle);
            TextView tRoom  = dialogView.findViewById(R.id.dialogRoom);
            TextView tPrice = dialogView.findViewById(R.id.dialogPrice);
            TextView tIn    = dialogView.findViewById(R.id.dialogCheckIn);
            TextView tOut   = dialogView.findViewById(R.id.dialogCheckOut);
            TextView tQ     = dialogView.findViewById(R.id.dialogQuestion);

            img.setImageResource(room.getImageId());
            tTitle.setText("Confirmar reserva");
            tRoom.setText(room.getName());
            tPrice.setText(room.getPrice());
            tIn.setText(checkIn.isEmpty() ? "—" : checkIn);
            tOut.setText(checkOut.isEmpty() ? "—" : checkOut);
            tQ.setText("¿Deseas confirmar tu reserva?");

            final androidx.appcompat.app.AlertDialog dialog =
                    new MaterialAlertDialogBuilder(context)
                            .setView(dialogView)
                            .create();

            dialogView.findViewById(R.id.btnCancel).setOnClickListener(btn -> dialog.dismiss());
            dialogView.findViewById(R.id.btnConfirm).setOnClickListener(btn -> {
                Reservation r = new Reservation(
                        currentUserName,
                        room.getName(),
                        checkIn,
                        checkOut,
                        room.getPrice(),
                        room.getImageId()
                );
                ReservationManager.addReservation(r);
                Toast.makeText(context, "Reserva guardada ✔", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        });

        holder.favoriteIconImageView.setOnClickListener(v ->
                Toast.makeText(context, "Corazón presionado: " + room.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return roomList == null ? 0 : roomList.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView roomImageView;
        ImageView favoriteIconImageView;
        TextView roomNameTextView;
        TextView ratingTextView;
        TextView specsTextView;
        LinearLayout serviceTagsLayout;
        TextView priceTextView;
        Button selectButton;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomImageView = itemView.findViewById(R.id.roomImageView);
            favoriteIconImageView = itemView.findViewById(R.id.favoriteIconImageView);
            roomNameTextView = itemView.findViewById(R.id.roomNameTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            specsTextView = itemView.findViewById(R.id.specsTextView);
            serviceTagsLayout = itemView.findViewById(R.id.serviceTagsLayout);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            selectButton = itemView.findViewById(R.id.selectButton);
        }
    }
}
