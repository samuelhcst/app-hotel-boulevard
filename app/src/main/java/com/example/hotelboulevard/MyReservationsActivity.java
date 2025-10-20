package com.example.hotelboulevard;
import com.google.android.material.appbar.MaterialToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;   // <-- IMPORT CLAVE
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView reservationsRecyclerView;
    private View emptyState;
    private ReservationsAdapter adapter;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());


        Intent intent = getIntent();
        currentUserName = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME);

        emptyState = findViewById(R.id.emptyState);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Separadores sutiles (opcional; ya hay margen en las cards)
        // reservationsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        List<Reservation> data = ReservationManager.getReservationsForUser(currentUserName);
        adapter = new ReservationsAdapter(data);
        reservationsRecyclerView.setAdapter(adapter);

        toggleEmpty(data);
    }

    private void toggleEmpty(List<Reservation> data) {
        boolean isEmpty = (data == null || data.isEmpty());
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        reservationsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    static class ReservationsAdapter extends RecyclerView.Adapter<ReservationsAdapter.ResViewHolder> {
        private final List<Reservation> reservations;

        ReservationsAdapter(List<Reservation> reservations) {
            this.reservations = reservations;
        }

        @NonNull
        @Override
        public ResViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_reservation, parent, false);
            return new ResViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ResViewHolder holder, int position) {
            Reservation r = reservations.get(position);
            holder.imageView.setImageResource(r.getImageId());
            holder.title.setText(r.getRoomName());
            holder.checkIn.setText("Check-in: " + (r.getCheckIn().isEmpty() ? "—" : r.getCheckIn()));
            holder.checkOut.setText("Check-out: " + (r.getCheckOut().isEmpty() ? "—" : r.getCheckOut()));
            holder.price.setText(r.getPrice());
        }

        @Override
        public int getItemCount() {
            return reservations == null ? 0 : reservations.size();
        }

        static class ResViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView title, checkIn, checkOut, price;
            public ResViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.reservationImageView);
                title    = itemView.findViewById(R.id.reservationTitleTextView);
                checkIn  = itemView.findViewById(R.id.reservationCheckInTextView);
                checkOut = itemView.findViewById(R.id.reservationCheckOutTextView);
                price    = itemView.findViewById(R.id.reservationPriceTextView);
            }
        }
    }
}