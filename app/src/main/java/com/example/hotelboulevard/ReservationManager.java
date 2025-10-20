package com.example.hotelboulevard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReservationManager {
    private static final List<Reservation> RESERVATIONS = new ArrayList<>();

    public static void addReservation(Reservation reservation) {
        RESERVATIONS.add(reservation);
    }

    public static List<Reservation> getReservationsForUser(String userName) {
        List<Reservation> out = new ArrayList<>();
        for (Reservation r : RESERVATIONS) {
            if (r.getUserName().equals(userName)) out.add(r);
        }
        return out;
    }

    public static List<Reservation> getAllReservations() {
        return Collections.unmodifiableList(RESERVATIONS);
    }

    public static void clearAll() { RESERVATIONS.clear(); } // opcional
}
