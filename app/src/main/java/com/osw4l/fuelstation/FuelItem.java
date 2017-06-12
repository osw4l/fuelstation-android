package com.osw4l.fuelstation;


import java.text.NumberFormat;
import java.util.Locale;

public class FuelItem {

    String nombre;
    int precio;

    public FuelItem(String nombre, int precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public String humanize(){
        return NumberFormat.getNumberInstance(Locale.US).format(this.getPrecio());
    }

}
