package com.example.diariodesaludybienestar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.Random;

public class FelicitacionesActivity extends Activity {

    private TextView textViewFelicitaciones;
    private Button btnCerrar;

    // Array con mensajes aleatorios de felicitación
    private String[] mensajesFelicitacion = {
            "¡Felicidades! Hoy has dado un gran paso hacia tu bienestar.",
            "¡Excelente trabajo! Tu dedicación es admirable.",
            "¡Lo estás haciendo genial! Sigue así.",
            "¡Buenísimo! Cada día eres mejor.",
            "¡Eres increíble! Celebra tus logros.",
            "¡Maravilloso! Tu esfuerzo está dando frutos.",
            "¡Fantástico! Hoy has sido productivo.",
            "¡Bravo! Has cumplido con tus objetivos.",
            "¡Sensacional! Tu disciplina es inspiradora.",
            "¡Magnífico! Estás construyendo hábitos saludables."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felicitaciones);

        textViewFelicitaciones = findViewById(R.id.textViewFelicitaciones);
        btnCerrar = findViewById(R.id.btnCerrarApp);

        // Mostrar mensaje aleatorio
        mostrarMensajeAleatorio();

        btnCerrar.setOnClickListener(v -> {
            // Animación del botón
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100))
                    .start();

            // Redirigir al MainActivity después de la animación
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(FelicitacionesActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }, 200);
        });
    }

    private void mostrarMensajeAleatorio() {
        Random random = new Random();
        String mensaje = mensajesFelicitacion[random.nextInt(mensajesFelicitacion.length)];
        textViewFelicitaciones.setText(mensaje);

        // Animación de aparición
        textViewFelicitaciones.setAlpha(0f);
        textViewFelicitaciones.animate().alpha(1f).setDuration(1000).start();
    }
}