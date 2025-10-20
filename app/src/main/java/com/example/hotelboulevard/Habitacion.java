package com.example.hotelboulevard;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Habitacion {

    // Los nombres DEBEN COINCIDIR con los campos en Firestore
    private String nombre;
    private String descripcion;
    private String tipo;
    private double precioPorNoche;
    private long cantidadTotal; // <-- Mantenemos este para el inventario
    private String imagenUrl;
    private List<String> imagenes;
    private Map<String, Boolean> servicios;
    @Exclude // <-- ANOTACIÓN IMPORTANTE
    private String id;

    // Constructor vacío (Requerido por Firestore)
    public Habitacion() {
        this.imagenes = new ArrayList<>();
        this.servicios = new HashMap<>();
    }

    // --- Getters y Setters ---
    @Exclude // Para que Firestore no intente guardar este campo
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public long getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(long cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }
    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public List<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<String> imagenes) {
        this.imagenes = imagenes;
    }

    public Map<String, Boolean> getServicios() {
        return servicios;
    }

    public void setServicios(Map<String, Boolean> servicios) {
        this.servicios = servicios;
    }
}