package com.example.diariodesaludybienestar;

public class Registro {
    private String fecha;
    private String desayuno;
    private String estadoAnimo;
    private String ejercicio;

    public Registro(String fecha, String desayuno, String estadoAnimo, String ejercicio) {
        this.fecha = fecha;
        this.desayuno = desayuno;
        this.estadoAnimo = estadoAnimo;
        this.ejercicio = ejercicio;
    }

    // Getters
    public String getFecha() { return fecha; }
    public String getDesayuno() { return desayuno; }
    public String getEstadoAnimo() { return estadoAnimo; }
    public String getEjercicio() { return ejercicio; }
}