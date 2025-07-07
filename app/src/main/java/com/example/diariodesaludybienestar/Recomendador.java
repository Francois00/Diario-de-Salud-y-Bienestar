package com.example.diariodesaludybienestar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Recomendador {

    private static final Random random = new Random();
    private static final Map<String, Integer> progresoUsuario = new HashMap<>();

    // Niveles de progreso para metas de objetivo
    private static final String[] NIVELES_PROGRESO = {
            "Principiante", "Intermedio", "Avanzado", "Experto"
    };

    public static List<String> generarMetasPersonalizadas(String objetivo, String estiloVida,
                                                          String genero, int edad, double peso,
                                                          double altura, Map<String, Object> registroDiario) {

        List<String> metasDiarias = new ArrayList<>();
        List<String> metasObjetivo = new ArrayList<>();


        // 1. Calcular métricas importantes
        double imc = calcularIMC(peso, altura);
        String nivelProgreso = determinarNivelProgreso(objetivo, registroDiario);
        int energiaHoy = obtenerEnergiaHoy(registroDiario);

        // 2. Generar metas diarias (pueden repetirse)
        metasDiarias.addAll(generarMetasDiariasBasicas());
        metasDiarias.addAll(generarMetasDiariasPorIMC(imc));
        metasDiarias.addAll(generarMetasDiariasPorEnergia(energiaHoy));

        // 3. Generar metas de objetivo (variadas según progreso)
        metasObjetivo.addAll(generarMetasObjetivo(objetivo, nivelProgreso, registroDiario));
        metasObjetivo.addAll(generarMetasAvanzadasPorEstilo(estiloVida, nivelProgreso));
        metasObjetivo.addAll(generarMetasEspecificasPorEdad(edad, nivelProgreso));
        metasObjetivo.addAll(generarMetasPersonalizadasPorGenero(genero, nivelProgreso));

        // 4. Combinar y limitar metas
        List<String> todasLasMetas = new ArrayList<>();
        todasLasMetas.addAll(seleccionarMetasDiarias(metasDiarias, 4)); // 4 metas diarias
        todasLasMetas.addAll(seleccionarMetasObjetivo(metasObjetivo, 3)); // 3 metas de objetivo

        return todasLasMetas;
    }

    // Métodos de cálculo y determinación
    private static double calcularIMC(double peso, double altura) {
        double alturaEnMetros = altura / 100.0;
        return peso / (alturaEnMetros * alturaEnMetros);
    }

    private static String determinarNivelProgreso(String objetivo, Map<String, Object> registroDiario) {
        // Lógica para determinar nivel basado en registro histórico
        int diasConsistentes = progresoUsuario.getOrDefault(objetivo+"_dias", 0);

        if (diasConsistentes < 7) return NIVELES_PROGRESO[0]; // Principiante
        if (diasConsistentes < 21) return NIVELES_PROGRESO[1]; // Intermedio
        if (diasConsistentes < 60) return NIVELES_PROGRESO[2]; // Avanzado
        return NIVELES_PROGRESO[3]; // Experto
    }

    private static int obtenerEnergiaHoy(Map<String, Object> registroDiario) {

        if (registroDiario != null && registroDiario.containsKey("mental")) {
            Map<String, Object> mental = (Map<String, Object>) registroDiario.get("mental");
            if (mental != null && mental.containsKey("energia")) { // Agrega verificación de nulo para 'mental'
                Object energiaObj = mental.get("energia");

                if (energiaObj instanceof Number) { // Verifica si es un tipo numérico
                    Number energiaNumber = (Number) energiaObj;
                    // Convierte a double de forma segura antes de redondear
                    return (int) Math.round(energiaNumber.doubleValue());
                }
            }
        }
        return 3; // Valor por defecto (en escala de 1-5)
    }

    // Generadores de metas diarias (pueden repetirse)
    private static List<String> generarMetasDiariasBasicas() {
        return Arrays.asList(
                "Bebe 8 vasos de agua hoy",
                "Come al menos 2 porciones de vegetales",
                "Tómate 5 minutos para respirar profundamente",
                "Estírate durante 5 minutos cada hora de trabajo",
                "Registra todo lo que comes hoy",
                "Camina al menos 10 minutos después de comer",
                "Escribe 3 cosas por las que estás agradecido",
                "Duerme al menos 7 horas esta noche"
        );
    }

    private static List<String> generarMetasDiariasPorIMC(double imc) {
        List<String> metas = new ArrayList<>();

        if (imc < 18.5) {
            metas.add("Consume un snack saludable adicional hoy");
            metas.add("Incluye proteínas en cada comida principal");
        } else if (imc <= 24.9) {
            metas.add("Mantén tu balance nutricional actual");
            metas.add("Varía tus fuentes de proteína");
        } else if (imc <= 29.9) {
            metas.add("Reduce las porciones en tu cena hoy");
            metas.add("Camina al menos 8,000 pasos");
        } else {
            metas.add("Evita alimentos fritos y azúcares añadidos");
            metas.add("Realiza 30 minutos de actividad física");
        }

        return metas;
    }

    private static List<String> generarMetasDiariasPorEnergia(int energia) {
        List<String> metas = new ArrayList<>();

        if (energia <= 2) {
            metas.add("Haz una actividad suave como yoga o estiramientos");
            metas.add("Toma una siesta de 20 minutos si es posible");
        } else if (energia <= 4) {
            metas.add("Aprovecha tu energía para completar tu rutina");
            metas.add("Prueba un ejercicio nuevo hoy");
        } else {
            metas.add("Aprovecha tu energía extra para un entrenamiento intenso");
            metas.add("Ayuda a alguien con su rutina de ejercicios");
        }

        return metas;
    }

    // Generadores de metas de objetivo (variadas)
    private static List<String> generarMetasObjetivo(String objetivo, String nivel, Map<String, Object> registro) {
        List<String> metas = new ArrayList<>();

        switch (objetivo.toLowerCase()) {
            case "perder peso":
                metas.addAll(generarMetasPerderPeso(nivel, registro));
                break;
            case "ganar músculo":
                metas.addAll(generarMetasGanarMusculo(nivel, registro));
                break;
            case "reducir estrés":
                metas.addAll(generarMetasReducirEstres(nivel, registro));
                break;
            case "mejorar sueño":
                metas.addAll(generarMetasMejorarSueno(nivel, registro));
                break;
            default:
                metas.add("Enfócate en pequeños cambios sostenibles");
        }

        return metas;
    }

    private static List<String> generarMetasGanarMusculo(String nivel, Map<String, Object> registro) {
        List<String> metas = new ArrayList<>();
        int entrenamientoRealizado = registro != null && registro.containsKey("fisica") ?
                Integer.parseInt(((Map<String, Object>)registro.get("fisica")).get("duracion").toString()) : 0;
        boolean hizoFuerza = registro != null && registro.containsKey("fisica") &&
                ((Map<String, Object>)registro.get("fisica")).get("tipoEjercicio").toString().toLowerCase().contains("pesas");

        switch(nivel) {
            case "Principiante":
                metas.add("Haz 3 series de 8-10 repeticiones de ejercicios básicos");
                metas.add("Consume proteína dentro de 30 minutos post-entreno");
                metas.add("Descansa al menos 48 horas entre grupos musculares");
                metas.add("Aprende la forma correcta de 2 ejercicios nuevos");
                break;

            case "Intermedio":
                metas.add("Incrementa el peso en un 5% esta semana");
                metas.add("Prueba el método de repeticiones descendentes");
                metas.add("Incorpora superseries para grupos musculares opuestos");
                metas.add("Registra tus progresos en el libro de entrenamiento");
                break;

            case "Avanzado":
                metas.add("Implementa periodización ondulante en tu rutina");
                metas.add("Optimiza tu timing de nutrientes alrededor del entrenamiento");
                metas.add("Prueba técnicas avanzadas como dropsets o rest-pause");
                metas.add("Analiza tu composición corporal con mediciones precisas");
                break;

            case "Experto":
                metas.add("Diseña un programa de mesociclos para los próximos 3 meses");
                metas.add("Mentoriza a alguien en sus primeros pasos con pesas");
                metas.add("Experimenta con protocolos de sobrecarga excéntrica");
                metas.add("Participa en un desafío de transformación muscular");
                break;
        }

        // Ajustes basados en entrenamiento realizado
        if (hizoFuerza && entrenamientoRealizado > 60) {
            metas.add("Enfócate en recuperación con estiramientos y proteína");
        } else if (!hizoFuerza) {
            metas.add("Incluye al menos un ejercicio con pesas hoy");
        }

        return metas;
    }

    private static List<String> generarMetasReducirEstres(String nivel, Map<String, Object> registro) {
        List<String> metas = new ArrayList<>();
        double horasSueno = registro != null && registro.containsKey("sueno") ?
                Double.parseDouble(((Map<String, Object>)registro.get("sueno")).get("horas").toString()) : 0;
        double nivelEstres = registro != null && registro.containsKey("mental") ?
                (double)((Map<String, Object>)registro.get("mental")).get("ansiedad") : 3;

        switch(nivel) {
            case "Principiante":
                metas.add("Practica 5 minutos de respiración diafragmática");
                metas.add("Identifica y escribe 3 cosas positivas del día");
                metas.add("Da un paseo corto en la naturaleza");
                metas.add("Escucha música relajante durante 10 minutos");
                break;

            case "Intermedio":
                metas.add("Realiza una sesión de 10 minutos de meditación guiada");
                metas.add("Practica la técnica 4-7-8 para relajarte");
                metas.add("Establece límites digitales por 2 horas hoy");
                metas.add("Prueba aromaterapia con lavanda o bergamota");
                break;

            case "Avanzado":
                metas.add("Haz una sesión de yoga restaurativo de 30 minutos");
                metas.add("Lleva un diario de pensamientos estresantes");
                metas.add("Practica la visualización guiada antes de dormir");
                metas.add("Implementa la técnica Pomodoro para trabajar");
                break;

            case "Experto":
                metas.add("Diseña tu rutina personalizada de manejo de estrés");
                metas.add("Enseña técnicas de relajación a alguien más");
                metas.add("Analiza tus patrones de estrés con registro detallado");
                metas.add("Participa en un retiro de mindfulness");
                break;
        }

        // Ajustes basados en sueño y estrés
        if (horasSueno < 6) {
            metas.add("Prioriza el sueño con una rutina relajante esta noche");
        }
        if (nivelEstres >= 4) {
            metas.add("Tómate 15 minutos para una actividad placentera");
        }

        return metas;
    }

    private static List<String> generarMetasMejorarSueno(String nivel, Map<String, Object> registro) {
        List<String> metas = new ArrayList<>();
        double horasSueno = registro != null && registro.containsKey("sueno") ?
                Double.parseDouble(((Map<String, Object>)registro.get("sueno")).get("horas").toString()) : 0;
        String calidadSueno = registro != null && registro.containsKey("sueno") ?
                ((Map<String, Object>)registro.get("sueno")).get("calidad").toString() : "Regular";

        switch(nivel) {
            case "Principiante":
                metas.add("Establece una hora fija para acostarte esta semana");
                metas.add("Evita pantallas 1 hora antes de dormir");
                metas.add("Crea un ambiente oscuro y fresco en tu dormitorio");
                metas.add("Prueba infusiones relajantes como manzanilla");
                break;

            case "Intermedio":
                metas.add("Implementa una rutina relajante pre-sueño de 30 minutos");
                metas.add("Registra tus patrones de sueño por una semana");
                metas.add("Experimenta con sonidos blancos o ruido rosa");
                metas.add("Controla tu consumo de cafeína después del mediodía");
                break;

            case "Avanzado":
                metas.add("Optimiza tu cronotipo con horarios personalizados");
                metas.add("Prueba técnicas de restricción controlada de sueño");
                metas.add("Analiza tu sueño con wearables o aplicaciones");
                metas.add("Experimenta con suplementos naturales (melatonina, magnesio)");
                break;

            case "Experto":
                metas.add("Diseña un protocolo personalizado de higiene del sueño");
                metas.add("Mentoriza a alguien con problemas de sueño");
                metas.add("Participa en un estudio de patrones de sueño");
                metas.add("Optimiza tu entorno de sueño con tecnología");
                break;
        }

        // Ajustes basados en sueño previo
        if (horasSueno < 6 || calidadSueno.equals("Mala")) {
            metas.add("Programa una siesta reparadora de 20 minutos hoy");
            metas.add("Evita la cafeína completamente hoy");
        } else if (horasSueno >= 7 && calidadSueno.equals("Buena")) {
            metas.add("Mantén tus excelentes hábitos de sueño");
        }

        return metas;
    }

    private static List<String> generarMetasEspecificasPorEdad(int edad, String nivel) {
        List<String> metas = new ArrayList<>();

        if (edad < 18) {
            metas.add("Juega activamente al menos 1 hora hoy");
            if (nivel.equals("Intermedio") || nivel.equals("Avanzado")) {
                metas.add("Aprende sobre nutrición con un adulto");
            }
        }
        else if (edad <= 30) {
            metas.add("Establece hábitos saludables duraderos");
            if (nivel.equals("Avanzado") || nivel.equals("Experto")) {
                metas.add("Analiza tus biomarcadores clave");
            }
        }
        else if (edad <= 50) {
            metas.add("Realiza ejercicios de movilidad articular");
            metas.add("Controla tu presión arterial regularmente");
            if (nivel.equals("Experto")) {
                metas.add("Optimiza tu perfil hormonal con especialista");
            }
        }
        else {
            metas.add("Practica ejercicios de equilibrio diariamente");
            metas.add("Mantén actividad social regular");
            if (nivel.equals("Intermedio") || nivel.equals("Avanzado")) {
                metas.add("Participa en actividades cognitivamente desafiantes");
            }
        }

        return metas;
    }

    private static List<String> generarMetasPersonalizadasPorGenero(String genero, String nivel) {
        List<String> metas = new ArrayList<>();

        if (genero.equalsIgnoreCase("femenino")) {
            metas.add("Incluye alimentos ricos en hierro en tu dieta");
            if (nivel.equals("Intermedio") || nivel.equals("Avanzado")) {
                metas.add("Monitorea tu ciclo menstrual y rendimiento");
            }
            if (nivel.equals("Experto")) {
                metas.add("Optimiza tu entrenamiento según fase menstrual");
            }
        }
        else if (genero.equalsIgnoreCase("masculino")) {
            metas.add("Incorpora ejercicios para la salud prostática");
            if (nivel.equals("Avanzado") || nivel.equals("Experto")) {
                metas.add("Controla tus niveles de testosterona regularmente");
            }
        }

        // Metas comunes pero con enfoque de género
        metas.add("Realiza chequeos preventivos según tu género y edad");

        return metas;
    }

    private static List<String> generarMetasPerderPeso(String nivel, Map<String, Object> registro) {
        List<String> metas = new ArrayList<>();
        int ejercicioRealizado = registro != null && registro.containsKey("fisica") ?
                Integer.parseInt(((Map<String, Object>)registro.get("fisica")).get("duracion").toString()) : 0;

        switch(nivel) {
            case "Principiante":
                metas.add("Haz 30 minutos de caminata rápida hoy");
                metas.add("Reemplaza una bebida azucarada por agua");
                metas.add("Come vegetales antes del plato principal");
                metas.add("Usa platos más pequeños para controlar porciones");
                break;

            case "Intermedio":
                metas.add("Intercala 1 minuto de alta intensidad cada 5 minutos de cardio");
                metas.add("Prueba el ayuno intermitente 16/8 hoy");
                metas.add("Registra tu ingesta de carbohidratos");
                metas.add("Haz entrenamiento de fuerza 2 veces esta semana");
                break;

            case "Avanzado":
                metas.add("Completa un circuito HIIT de 20 minutos");
                metas.add("Calcula tu TDEE y ajusta tu déficit calórico");
                metas.add("Prueba una nueva actividad quema-calorías");
                metas.add("Analiza tu composición corporal esta semana");
                break;

            case "Experto":
                metas.add("Diseña un plan de comidas semanal equilibrado");
                metas.add("Optimiza tus macros para pérdida de grasa");
                metas.add("Mentoriza a alguien en su viaje de pérdida de peso");
                metas.add("Participa en un desafío de transformación");
                break;
        }

        // Ajustar según ejercicio realizado
        if (ejercicioRealizado > 45) {
            metas.add("Enfócate en la recuperación activa hoy");
        } else if (ejercicioRealizado < 20) {
            metas.add("Añade 15 minutos extra de actividad hoy");
        }

        return metas;
    }

    // Métodos similares para otros objetivos (ganar músculo, reducir estrés, etc.)
    // ... [Implementar según la misma estructura]

    private static List<String> generarMetasAvanzadasPorEstilo(String estilo, String nivel) {
        List<String> metas = new ArrayList<>();

        switch(estilo.toLowerCase()) {
            case "sedentario":
                metas.add("Programa recordatorios para moverte cada hora");
                if (nivel.equals("Intermedio") || nivel.equals("Avanzado")) {
                    metas.add("Prueba ejercicios de escritorio cada hora");
                }
                break;

            case "activo":
                if (nivel.equals("Avanzado") || nivel.equals("Experto")) {
                    metas.add("Analiza tus métricas de rendimiento");
                    metas.add("Optimiza tu rutina de recuperación");
                }
                break;
        }

        return metas;
    }

    // Métodos de selección
    private static List<String> seleccionarMetasDiarias(List<String> opciones, int cantidad) {
        List<String> seleccionadas = new ArrayList<>();
        for (int i = 0; i < cantidad && !opciones.isEmpty(); i++) {
            int index = random.nextInt(opciones.size());
            seleccionadas.add(opciones.get(index));
        }
        return seleccionadas;
    }

    private static List<String> seleccionarMetasObjetivo(List<String> opciones, int cantidad) {
        // Para metas de objetivo, priorizar las más relevantes
        List<String> seleccionadas = new ArrayList<>();

        // Primero asegurar al menos una meta principal
        if (!opciones.isEmpty()) {
            seleccionadas.add(opciones.get(0));
            opciones.remove(0);
        }

        // Luego seleccionar aleatoriamente el resto
        while (seleccionadas.size() < cantidad && !opciones.isEmpty()) {
            int index = random.nextInt(opciones.size());
            String meta = opciones.remove(index);
            if (!seleccionadas.contains(meta)) {
                seleccionadas.add(meta);
            }
        }

        return seleccionadas;
    }

    // Métodos adicionales para consejos motivacionales
    public static String obtenerConsejoMotivacional(String objetivo, String emocion) {
        Map<String, List<String>> consejos = new HashMap<>();

        // Consejos por objetivo
        consejos.put("perder peso", Arrays.asList(
                "Cada paso cuenta en tu viaje hacia una vida más saludable",
                "La consistencia supera a la intensidad a largo plazo",
                "Celebra cada pequeña victoria en tu camino"
        ));

        // Consejos por emoción
        consejos.put("ansiedad", Arrays.asList(
                "Respira profundo - puedes manejar esto un paso a la vez",
                "El autocuidado no es egoísta, es necesario",
                "Hoy es un nuevo día para comenzar de nuevo"
        ));

        // Combinar consejos relevantes
        List<String> consejosCombinados = new ArrayList<>();
        if (consejos.containsKey(objetivo)) {
            consejosCombinados.addAll(consejos.get(objetivo));
        }
        if (emocion != null && consejos.containsKey(emocion)) {
            consejosCombinados.addAll(consejos.get(emocion));
        }

        return consejosCombinados.isEmpty() ?
                "Recuerda que cada día es una nueva oportunidad para cuidarte" :
                consejosCombinados.get(random.nextInt(consejosCombinados.size()));
    }
}