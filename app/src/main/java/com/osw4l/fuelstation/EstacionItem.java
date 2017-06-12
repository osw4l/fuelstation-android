package com.osw4l.fuelstation;

/**
 * Created by osw4l on 12/06/17.
 */

public class EstacionItem {
    String nombre, direccion;
    double latitud, longitud;
    int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public EstacionItem(int id , String nombre, String direccion, double latitud, double longitud) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
