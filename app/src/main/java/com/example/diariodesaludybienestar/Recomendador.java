package com.example.diariodesaludybienestar;

import java.util.ArrayList;
import java.util.List;

public class Recomendador {

    public static List<String> generarSugerencias(String estilo, double peso, double altura, int edad) {
        List<String> sugerencias = new ArrayList<>();

        double alturaEnMetros = altura / 100.0;
        double imc = peso / (alturaEnMetros * alturaEnMetros);

        // Recomendaciones por IMC
        if (imc < 18.5) {
            sugerencias.add("Tu IMC indica bajo peso. Considera aumentar tu ingesta calórica.");
            sugerencias.add("Incluye frutos secos, aguacate y batidos en tu dieta.");
            sugerencias.add("Consulta con un nutricionista para un plan de aumento de peso saludable.");
        } else if (imc >= 18.5 && imc <= 24.9) {
            sugerencias.add("Tu peso está en un rango saludable. ¡Sigue así!");
            sugerencias.add("Mantén una dieta balanceada y realiza actividad física con regularidad.");
        } else if (imc >= 25 && imc <= 29.9) {
            sugerencias.add("Tu IMC indica sobrepeso. Considera reducir el consumo de alimentos ultraprocesados.");
            sugerencias.add("Realiza caminatas diarias y monitorea tus hábitos de alimentación.");
        } else {
            sugerencias.add("Tu IMC está en el rango de obesidad. Es importante consultar con un especialista.");
            sugerencias.add("Intenta reducir azúcares y grasas en tus comidas.");
            sugerencias.add("Realiza actividad física al menos 30 minutos al día.");
        }

        // Recomendaciones por estilo de vida
        if (estilo.equalsIgnoreCase("No activo")) {
            sugerencias.add("Inicia con rutinas suaves como yoga o estiramientos.");
            sugerencias.add("Camina al menos 20-30 minutos al día para comenzar.");
            sugerencias.add("Evita estar más de una hora seguido sentado.");
        } else {
            sugerencias.add("Mantente activo con al menos 150 minutos de actividad moderada por semana.");
            sugerencias.add("Agrega variedad a tu rutina: caminata, bici, ejercicios de fuerza.");
        }

        // Recomendaciones por edad
        if (edad < 18) {
            sugerencias.add("Estás en etapa de desarrollo, consume suficiente calcio y proteínas.");
            sugerencias.add("Evita el consumo excesivo de bebidas azucaradas.");
        } else if (edad >= 18 && edad <= 30) {
            sugerencias.add("Aprovecha tu metabolismo activo para establecer buenos hábitos.");
            sugerencias.add("Incluye ejercicios de fuerza para fortalecer huesos y músculos.");
        } else if (edad > 30 && edad <= 50) {
            sugerencias.add("Aumenta el consumo de fibra y agua.");
            sugerencias.add("Chequea regularmente tu presión y niveles de glucosa.");
        } else {
            sugerencias.add("Realiza chequeos médicos anuales.");
            sugerencias.add("Prefiere ejercicios de bajo impacto como natación o caminatas.");
            sugerencias.add("Reduce el consumo de sal y grasas saturadas.");
        }

        // Recomendación genérica
        sugerencias.add("Dormir al menos 7 horas por noche mejora tu salud general.");
        sugerencias.add("Mantén hidratación constante, bebe al menos 6-8 vasos de agua al día.");
        sugerencias.add("Registra tu estado de ánimo y hábitos diariamente para detectar patrones.");

        return sugerencias;
    }
}
