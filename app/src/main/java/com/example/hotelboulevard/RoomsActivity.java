package com.example.hotelboulevard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RoomsActivity extends AppCompatActivity {

    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private List<Room> roomList;

    private String userName;
    private String checkIn;
    private String checkOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        roomsRecyclerView = findViewById(R.id.roomsRecyclerView);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        userName  = intent.getStringExtra(LoginActivity.EXTRA_USER_NAME);
        checkIn   = intent.getStringExtra(HomeActivity.EXTRA_CHECK_IN);
        checkOut  = intent.getStringExtra(HomeActivity.EXTRA_CHECK_OUT);

        loadDummyRoomData();

        roomAdapter = new RoomAdapter(this, roomList, userName, checkIn, checkOut);
        roomsRecyclerView.setAdapter(roomAdapter);
    }

    private void loadDummyRoomData() {
        roomList = new ArrayList<>();
        // Asegúrate de tener imágenes con estos nombres en tu carpeta drawable
        // o reemplaza con @mipmap/ic_launcher o equivalentes.
        roomList.add(new Room("Habitación Estandar Doble", 4.8f, "55m² • 1 Cama King • Balcón", "$350 / noche", R.drawable.estandardoble));
        roomList.add(new Room("Habitación con Jacuzzi", 4.5f, "30m² • 2 Camas Individuales", "$180 / noche", R.drawable.jacuzi));
        roomList.add(new Room("Habitación Matrimonial", 4.6f, "42m² • 1 Cama Queen • Sala de estar", "$250 / noche", R.drawable.matrimonial));
        roomList.add(new Room("Habitación Panorámica", 4.2f, "20m² • 1 Cama Individual", "$120 / noche", R.drawable.panoramica));
        roomList.add(new Room("Suite Light", 5.0f, "120m² • 2 Camas King • Jacuzzi Privado", "$800 / noche", R.drawable.suite));
    }
}
