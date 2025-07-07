package com.example.diariodesaludybienestar;

import java.util.List;
import java.util.Map;

public class RegistroCompleto {
    private String fecha;
    private int totalKcal;
    private List<Map<String, String>> comidas;
    private float ansiedad;
    private float energia;
    private String motivoEmocion;
    private String ejercicio;
    private String duracionEjercicio;
    private String horasSueno;
    private String calidadSueno;
    private List<String> metasCompletadas;

    public RegistroCompleto(String fecha) {
        this.fecha = fecha;
    }

    // Getters y Setters
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getTotalKcal() { return totalKcal; }
    public void setTotalKcal(int totalKcal) { this.totalKcal = totalKcal; }

    public List<Map<String, String>> getComidas() { return comidas; }
    public void setComidas(List<Map<String, String>> comidas) { this.comidas = comidas; }

    public float getAnsiedad() { return ansiedad; }
    public void setAnsiedad(float ansiedad) { this.ansiedad = ansiedad; }

    public float getEnergia() { return energia; }
    public void setEnergia(float energia) { this.energia = energia; }

    public String getMotivoEmocion() { return motivoEmocion; }
    public void setMotivoEmocion(String motivoEmocion) { this.motivoEmocion = motivoEmocion; }

    public String getEjercicio() { return ejercicio; }
    public void setEjercicio(String ejercicio) { this.ejercicio = ejercicio; }

    public String getDuracionEjercicio() { return duracionEjercicio; }
    public void setDuracionEjercicio(String duracionEjercicio) { this.duracionEjercicio = duracionEjercicio; }

    public String getHorasSueno() { return horasSueno; }
    public void setHorasSueno(String horasSueno) { this.horasSueno = horasSueno; }

    public String getCalidadSueno() { return calidadSueno; }
    public void setCalidadSueno(String calidadSueno) { this.calidadSueno = calidadSueno; }

    public List<String> getMetasCompletadas() { return metasCompletadas; }
    public void setMetasCompletadas(List<String> metasCompletadas) { this.metasCompletadas = metasCompletadas; }

    public String getResumen() {
        return "Día: " + fecha + "\n" +
                "Kcal: " + totalKcal + " | Ejercicio: " + ejercicio + " (" + duracionEjercicio + " min)";
    }
}