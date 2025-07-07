package com.example.diariodesaludybienestar;

import java.util.ArrayList;
import java.util.List;

public class Recomendador {

    public static List<String> generarSugerencias(String estilo, double peso, double altura, int edad, String objetivo, String genero) {
        List<String> sugerencias = new ArrayList<>();

        double alturaEnMetros = altura / 100.0;
        double imc = peso / (alturaEnMetros * alturaEnMetros);

        sugerenciasPorIMC(imc, sugerencias);
        sugerenciasPorEstilo(estilo, sugerencias);
        sugerenciasPorEdad(edad, sugerencias);
        sugerenciasPorObjetivo(objetivo, sugerencias);
        sugerenciasPorGenero(genero, sugerencias);
        sugerenciasGenerales(sugerencias);

        return sugerencias;
    }

    private static void sugerenciasPorIMC(double imc, List<String> s) {
        if (imc < 18.5) {
            s.add("Tu IMC indica bajo peso. Considera aumentar tu ingesta calórica.");
            s.add("Incluye frutos secos, aguacate y batidos en tu dieta.");
        } else if (imc <= 24.9) {
            s.add("Tu peso está en un rango saludable. ¡Sigue así!");
            s.add("Mantén una dieta balanceada y realiza actividad física con regularidad.");
        } else if (imc <= 29.9) {
            s.add("Tu IMC indica sobrepeso. Considera reducir alimentos ultraprocesados.");
            s.add("Haz caminatas diarias y cuida las porciones.");
        } else {
            s.add("Tu IMC está en el rango de obesidad. Es recomendable un plan con un especialista.");
            s.add("Evita azúcares y grasas; incluye verduras en cada comida.");
        }
    }

    private static void sugerenciasPorEstilo(String estilo, List<String> s) {
        if (estilo.equalsIgnoreCase("No activo")) {
            s.add("Inicia con rutinas suaves como yoga o caminatas cortas.");
            s.add("Camina 20-30 minutos al día.");
        } else {
            s.add("Haz al menos 150 minutos de actividad moderada por semana.");
            s.add("Incluye ejercicios de fuerza 2 veces por semana.");
        }
    }

    private static void sugerenciasPorEdad(int edad, List<String> s) {
        if (edad < 18) {
            s.add("Consume suficiente calcio y proteínas para el desarrollo.");
        } else if (edad <= 30) {
            s.add("Incluye ejercicios de fuerza y buenas rutinas alimenticias.");
        } else if (edad <= 50) {
            s.add("Aumenta fibra, agua y revisa tus niveles de colesterol.");
        } else {
            s.add("Prioriza ejercicios de bajo impacto y chequeos médicos anuales.");
        }
    }

    private static void sugerenciasPorObjetivo(String objetivo, List<String> s) {
        if (objetivo == null) return;
        switch (objetivo.toLowerCase()) {
            case "perder peso":
                s.add("Reduce tu ingesta calórica diaria en 300-500 kcal.");
                s.add("Haz cardio al menos 40 minutos, 4 días a la semana.");
                s.add("Evita comidas altas en azúcar después de las 6 p.m.");
                break;
            case "ganar músculo":
                s.add("Aumenta tu consumo de proteínas (1.6-2g por kg de peso).");
                s.add("Realiza ejercicios de fuerza al menos 3 veces por semana.");
                s.add("Come algo rico en proteína dentro de 1 hora post-entreno.");
                break;
            case "mantenerse":
                s.add("Sigue tus rutinas y mantén variedad para no aburrirte.");
                s.add("Controla tus progresos cada 2 semanas.");
                break;
        }
    }

    private static void sugerenciasPorGenero(String genero, List<String> s) {
        if (genero == null) return;
        switch (genero.toLowerCase()) {
            case "femenino":
                s.add("Asegura suficiente hierro y ácido fólico si es necesario.");
                s.add("Ten en cuenta el ciclo menstrual al planificar entrenamientos.");
                break;
            case "masculino":
                s.add("Incorpora ejercicios que fortalezcan el core y espalda.");
                break;
            case "otro":
                s.add("Adapta tu rutina a lo que te haga sentir bien física y emocionalmente.");
                break;
        }
    }

    private static void sugerenciasGenerales(List<String> s) {
        s.add("Duerme al menos 7 horas por noche.");
        s.add("Bebe 6-8 vasos de agua al día.");
        s.add("Evita pantallas al menos 30 minutos antes de dormir.");
        s.add("Tómate 5 minutos de pausa por cada hora frente a pantallas.");
        s.add("Come al menos 2 frutas al día.");
        s.add("Lleva un diario emocional y de hábitos para reflexionar.");
    }
}

