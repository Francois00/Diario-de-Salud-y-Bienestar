package com.example.diariodesaludybienestar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecomendacionesActivity extends AppCompatActivity {

    private LinearLayout layoutMetasDia, layoutMetasObjetivo;
    private MaterialButton btnGuardar, btnGenerarNuevas;
    private TextView tvResumenProgreso;
    private String fecha;
    private FirebaseUser user;
    private Map<String, Object> perfilUsuario;
    private Map<String, Object> registroHoy;
    private List<CheckBox> checksDia = new ArrayList<>();
    private List<CheckBox> checksObjetivo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendaciones);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initViews();
        setupFirebase();
        setupButtons();
    }

    private void initViews() {
        layoutMetasDia = findViewById(R.id.layoutMetasDia);
        layoutMetasObjetivo = findViewById(R.id.layoutMetasObjetivo);
        btnGuardar = findViewById(R.id.btnGuardarMetas);
        btnGenerarNuevas = findViewById(R.id.btnGenerarNuevasMetas);
        tvResumenProgreso = findViewById(R.id.tvResumenProgreso);
    }

    private void setupFirebase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        cargarPerfilUsuario();
        cargarRegistroHoy();
    }

    private void setupButtons() {
        btnGuardar.setOnClickListener(v -> guardarMetasCompletadas());
        btnGenerarNuevas.setOnClickListener(v -> generarNuevasMetasPersonalizadas());
    }

    private void cargarPerfilUsuario() {
        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child("Perfil")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            perfilUsuario = (Map<String, Object>) snapshot.getValue();
                            generarMetasBasadasEnPerfil();
                        } else {
                            Toast.makeText(RecomendacionesActivity.this,
                                    "Completa tu perfil para obtener recomendaciones",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showError("Error al cargar perfil");
                    }
                });
    }

    private void cargarRegistroHoy() {
        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child("Registros")
                .child(fecha)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            registroHoy = (Map<String, Object>) snapshot.getValue();
                            actualizarResumenProgreso();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showError("Error al cargar registro");
                    }
                });
    }

    private void actualizarResumenProgreso() {
        if (registroHoy == null) return;

        StringBuilder resumen = new StringBuilder("Tu progreso hoy:\n");

        // Nutrición
        if (registroHoy.containsKey("nutricion")) {
            Map<String, Object> nutricion = (Map<String, Object>) registroHoy.get("nutricion");
            int calorias = nutricion.containsKey("totalKcal") ?
                    Integer.parseInt(nutricion.get("totalKcal").toString()) : 0;
            resumen.append("🍎 ").append(calorias).append(" kcal\n");
        }

        // Ejercicio
        if (registroHoy.containsKey("fisica")) {
            Map<String, Object> fisica = (Map<String, Object>) registroHoy.get("fisica");
            String ejercicio = fisica.containsKey("ejercicio") ?
                    fisica.get("ejercicio").toString() : "Sin ejercicio";
            resumen.append("💪 ").append(ejercicio).append("\n");
        }

        // Sueño
        if (registroHoy.containsKey("sueno")) {
            Map<String, Object> sueno = (Map<String, Object>) registroHoy.get("sueno");
            String horas = sueno.containsKey("horas") ?
                    sueno.get("horas").toString() : "No registrado";
            resumen.append("😴 ").append(horas).append(" horas\n");
        }

        tvResumenProgreso.setText(resumen.toString());
    }



    private void generarMetasBasadasEnPerfil() {
        if (perfilUsuario == null) {
            showError("Perfil no cargado. Completa tu perfil primero.");
            return;
        }

        try {
            // Verificar que todos los campos necesarios existen
            String[] camposRequeridos = {"objetivo", "estiloVida", "genero", "edad", "peso", "altura"};
            for (String campo : camposRequeridos) {
                if (!perfilUsuario.containsKey(campo)) {
                    showError("Falta completar el campo: " + campo + " en tu perfil");
                    return;
                }
            }

            // Convertir con verificación
            String objetivo = perfilUsuario.get("objetivo").toString();
            String estiloVida = perfilUsuario.get("estiloVida").toString();
            String genero = perfilUsuario.get("genero").toString();

            int edad;
            double peso, altura;

            try {
                edad = Integer.parseInt(perfilUsuario.get("edad").toString());
                peso = Double.parseDouble(perfilUsuario.get("peso").toString());
                altura = Double.parseDouble(perfilUsuario.get("altura").toString());
            } catch (NumberFormatException e) {
                showError("Edad, peso y altura deben ser números válidos");
                return;
            }

            // Verificar valores positivos
            if (edad <= 0 || peso <= 0 || altura <= 0) {
                showError("Edad, peso y altura deben ser valores positivos");
                return;
            }

            // Resto de la lógica...
            Map<String, Object> registroHoy = obtenerRegistroDiario();
            List<String> metas = Recomendador.generarMetasPersonalizadas(
                    objetivo, estiloVida, genero, edad, peso, altura, registroHoy);

            dividirYMostrarMetas(metas);
            mostrarConsejoMotivacional(objetivo);

        } catch (Exception e) {
            showError("Error al procesar perfil: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Map<String, Object> obtenerRegistroDiario() {
        // Implementación para obtener el registro actual de Firebase
        // Esto es un placeholder - debes implementar la lógica real de Firebase
        Map<String, Object> registro = new HashMap<>();

        if (registroHoy != null) {
            registro.putAll(registroHoy);
        }

        return registro;
    }

    private void mostrarConsejoMotivacional(String objetivo) {
        // Obtener emoción predominante del registro
        String emocion = obtenerEmocionPredominante();

        String consejo = Recomendador.obtenerConsejoMotivacional(objetivo, emocion);

        // Mostrar en un TextView o Toast
        Toast.makeText(this, "Consejo del día: " + consejo, Toast.LENGTH_LONG).show();

        // O también podrías mostrarlo en un TextView en tu layout
        // tvConsejo.setText(consejo);
    }

    private String obtenerEmocionPredominante() {
        if (registroHoy == null || !registroHoy.containsKey("mental")) {
            return null;
        }

        Map<String, Object> estadoMental = (Map<String, Object>) registroHoy.get("mental");

        // Lógica para determinar la emoción predominante
        // Esto es un ejemplo básico - puedes hacerlo más sofisticado
        double ansiedad = (double) estadoMental.get("ansiedad");
        double tristeza = (double) estadoMental.get("tristeza");
        double felicidad = (double) estadoMental.get("felicidad");

        if (ansiedad >= 4) return "ansiedad";
        if (tristeza >= 4) return "tristeza";
        if (felicidad >= 4) return "felicidad";

        return null;
    }

    private void dividirYMostrarMetas(List<String> metas) {
        // Esta lógica puede mantenerse similar, pero ahora las metas ya vienen organizadas
        // del Recomendador, así que simplemente las mostramos

        layoutMetasDia.removeAllViews();
        layoutMetasObjetivo.removeAllViews();
        checksDia.clear();
        checksObjetivo.clear();

        // Asumimos que las primeras son diarias y las siguientes de objetivo
        // (o puedes modificar el Recomendador para que devuelva dos listas separadas)
        int mitad = Math.min(4, metas.size()); // Primeras 4 son diarias

        for (int i = 0; i < metas.size(); i++) {
            CheckBox cb = new CheckBox(this);
            cb.setText(metas.get(i));
            cb.setTextSize(16);
            cb.setPadding(0, 16, 0, 16);

            if (i < mitad) {
                layoutMetasDia.addView(cb);
                checksDia.add(cb);
            } else {
                layoutMetasObjetivo.addView(cb);
                checksObjetivo.add(cb);
            }
        }

        // Alternativa: si el Recomendador devuelve dos listas separadas:
        // for (String meta : metasDiarias) { ... }
        // for (String meta : metasObjetivo) { ... }
    }



    private boolean esMetaDiaria(String meta) {
        return meta.toLowerCase().contains("hoy") ||
                meta.toLowerCase().contains("diario") ||
                meta.toLowerCase().contains("día");
    }

    private void guardarYMostrarMetas(String tipo, List<String> metas,
                                      LinearLayout layout, List<CheckBox> checks) {
        if (metas.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No hay metas " + (tipo.equals("MetasDelDia") ? "diarias" : "de objetivo"));
            layout.addView(tv);
            return;
        }

        // Guardar en Firebase
        Map<String, Object> metasMap = new HashMap<>();
        for (String meta : metas) {
            metasMap.put(meta, meta);
        }

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child(tipo)
                .child(fecha)
                .setValue(metasMap)
                .addOnSuccessListener(aVoid -> mostrarMetas(metas, layout, checks, tipo + "Marcadas"))
                .addOnFailureListener(e -> showError("Error al guardar metas"));
    }

    private void mostrarMetas(List<String> metas, LinearLayout layout,
                              List<CheckBox> checks, String ramaMarcadas) {
        layout.removeAllViews();
        checks.clear();

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child(ramaMarcadas)
                .child(fecha)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Boolean> estadoMetas = new HashMap<>();
                        for (DataSnapshot meta : snapshot.getChildren()) {
                            estadoMetas.put(meta.getKey(), meta.getValue(Boolean.class));
                        }

                        for (String meta : metas) {
                            CardView card = createMetaCard(meta, estadoMetas.getOrDefault(meta, false));
                            layout.addView(card);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showError("Error al cargar estado de metas");
                    }
                });
    }

    private CardView createMetaCard(String textoMeta, boolean completada) {
        CardView card = new CardView(this);
        card.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        card.setCardElevation(4);
        card.setRadius(12);
        card.setContentPadding(16, 16, 16, 16);
        card.setCardBackgroundColor(getResources().getColor(R.color.white));

        CheckBox checkBox = new CheckBox(this);
        checkBox.setText(textoMeta);
        checkBox.setChecked(completada);
        checkBox.setTextSize(16);
        checkBox.setPadding(8, 16, 8, 16);
        checkBox.setTextColor(getResources().getColor(R.color.black));

        card.addView(checkBox);

        if (layoutMetasDia.getChildCount() == 0) {
            checksDia.add(checkBox);
        } else {
            checksObjetivo.add(checkBox);
        }

        return card;
    }

    private void generarNuevasMetasPersonalizadas() {
        if (perfilUsuario == null) {
            Toast.makeText(this, "Cargando perfil...", Toast.LENGTH_SHORT).show();
            cargarPerfilUsuario();
            return;
        }

        // Mostrar progreso
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Generando nuevas metas...");
        progress.show();

        try {
            generarMetasBasadasEnPerfil();
        } finally {
            progress.dismiss();
        }
    }

    private void guardarMetasCompletadas() {
        guardarEstadoMetas("MetasDelDiaMarcadas", checksDia);
        guardarEstadoMetas("MetasObjetivoMarcadas", checksObjetivo);

        Toast.makeText(this, "Progreso guardado", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, FelicitacionesActivity.class));
    }

    private void guardarEstadoMetas(String ruta, List<CheckBox> checks) {
        Map<String, Boolean> estado = new HashMap<>();
        for (CheckBox check : checks) {
            estado.put(check.getText().toString(), check.isChecked());
        }

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child(ruta)
                .child(fecha)
                .setValue(estado);
    }

    private void showError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(this, PerfilUsuarioActivity.class));
            return true;
        } else if (id == R.id.action_metas) {
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (id == R.id.action_avanzado) {
            startActivity(new Intent(this, RegistroAvanzadoActivity.class));
            return true;
        } else if (id == R.id.action_historial) {
            startActivity(new Intent(this, HistorialActivity.class));
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}