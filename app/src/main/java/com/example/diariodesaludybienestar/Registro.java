package com.example.diariodesaludybienestar;

public class Registro {
    private String id;
   public String comida;
    public String ejercicio;
    public String sueno;
   public String estado;
    public String fecha;


    // Constructor vacío requerido por Firebase
    public Registro() {}

    public Registro(String fecha,String id, String comida, String ejercicio, String sueno, String estado) {
        this.id = id;
        this.comida = comida;
        this.ejercicio = ejercicio;
        this.sueno = sueno;
        this.estado = estado;
        this.fecha = fecha;
    }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.id = fecha; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getComida() { return comida; }
    public void setComida(String comida) { this.comida = comida; }

    public String getEjercicio() { return ejercicio; }
    public void setEjercicio(String ejercicio) { this.ejercicio = ejercicio; }

    public String getSueno() { return sueno; }
    public void setSueno(String sueno) { this.sueno = sueno; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
