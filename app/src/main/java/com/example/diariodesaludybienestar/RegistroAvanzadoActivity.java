package com.example.diariodesaludybienestar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistroAvanzadoActivity extends AppCompatActivity {

    // Views
    private AutoCompleteTextView autoDesayuno;
    private EditText edtKcalDesayuno, edtProtDesayuno, edtMotivoEmocion, edtEjercicio;
    private CheckBox checkAnsiedad, checkTristeza, checkIrritabilidad;
    private Spinner spinnerObjetivo;
    private Button btnGuardar;
    private NutritionAPI nutritionAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_avanzado);

        nutritionAPI = new NutritionAPI(this);
        initViews();
        setupAutocomplete();
        setupSpinner();
        setupGuardarButton();
    }

    private void initViews() {
        autoDesayuno = findViewById(R.id.autoDesayuno);
        edtKcalDesayuno = findViewById(R.id.edtKcalDesayuno);
        edtProtDesayuno = findViewById(R.id.edtProtDesayuno);
        checkAnsiedad = findViewById(R.id.checkAnsiedad);
        checkTristeza = findViewById(R.id.checkTristeza);
        checkIrritabilidad = findViewById(R.id.checkIrritabilidad);
        edtMotivoEmocion = findViewById(R.id.edtMotivoEmocion);
        spinnerObjetivo = findViewById(R.id.spinnerObjetivoFisico);
        edtEjercicio = findViewById(R.id.edtEjercicio);
        btnGuardar = findViewById(R.id.btnGuardarRegistro);
    }

    private void setupAutocomplete() {
        String[] alimentos = {"Avena", "Huevos", "Pan integral", "Yogur griego"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                alimentos
        );
        autoDesayuno.setAdapter(adapter);

        autoDesayuno.setOnItemClickListener((parent, view, position, id) -> {
            String alimento = parent.getItemAtPosition(position).toString();

            // Muestra ProgressBar mientras carga
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            nutritionAPI.getNutritionData(alimento, new NutritionAPI.NutritionCallback() {
                @Override
                public void onSuccess(int calories, int protein) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        edtKcalDesayuno.setText(String.valueOf(calories));
                        edtProtDesayuno.setText(String.valueOf(protein));
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegistroAvanzadoActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        // Permite ingreso manual
                        edtKcalDesayuno.setHint("Ingresa kcal manual");
                        edtProtDesayuno.setHint("Ingresa proteínas manual");
                    });
                }
            });
        });
    }

    private void setupSpinner() {
        spinnerObjetivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String objetivo = parent.getItemAtPosition(position).toString();
                String ejercicioSugerido = "";
                switch (objetivo) {
                    case "Perder peso":
                        ejercicioSugerido = "Cardio 30 min";
                        break;
                    case "Ganar músculo":
                        ejercicioSugerido = "Pesas 3 series";
                        break;
                    default:
                        ejercicioSugerido = "Caminar 20 min";
                }
                edtEjercicio.setText(ejercicioSugerido);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupGuardarButton() {
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarRegistro();
                mostrarResumenDiario();
            }
        });
    }

    private boolean validarCampos() {
        if (autoDesayuno.getText().toString().isEmpty()) {
            Toast.makeText(this, "Ingresa al menos el desayuno", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void guardarRegistro() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String uid = user.getUid();

        Map<String, Object> registro = createRegistroMap();

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(uid)
                .child("Registros")
                .child(fecha)
                .setValue(registro)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registro guardado", Toast.LENGTH_SHORT).show();
                    marcarMetaEjercicioSiCorresponde(uid, fecha);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                );
    }

    private Map<String, Object> createRegistroMap() {
        Map<String, Object> nutricion = new HashMap<>();
        nutricion.put("desayuno", autoDesayuno.getText().toString());
        nutricion.put("kcal", edtKcalDesayuno.getText().toString());
        nutricion.put("proteinas", edtProtDesayuno.getText().toString());

        Map<String, Object> mental = new HashMap<>();
        mental.put("ansiedad", checkAnsiedad.isChecked());
        mental.put("tristeza", checkTristeza.isChecked());
        mental.put("irritabilidad", checkIrritabilidad.isChecked());
        mental.put("motivo", edtMotivoEmocion.getText().toString());

        Map<String, Object> fisica = new HashMap<>();
        fisica.put("ejercicio", edtEjercicio.getText().toString());
        fisica.put("objetivo", spinnerObjetivo.getSelectedItem().toString());

        Map<String, Object> registro = new HashMap<>();
        registro.put("nutricion", nutricion);
        registro.put("mental", mental);
        registro.put("fisica", fisica);

        return registro;
    }

    private void marcarMetaEjercicioSiCorresponde(String uid, String fecha) {
        if (!edtEjercicio.getText().toString().isEmpty()) {
            FirebaseDatabase.getInstance()
                    .getReference("Usuarios")
                    .child(uid)
                    .child("Metas")
                    .child(fecha)
                    .child("ejercicio_30min")
                    .setValue(true);
        }
    }

    private void mostrarResumenDiario() {
        List<String> emociones = new ArrayList<>();
        if (checkAnsiedad.isChecked()) emociones.add("Ansiedad");
        if (checkTristeza.isChecked()) emociones.add("Tristeza");
        if (checkIrritabilidad.isChecked()) emociones.add("Irritabilidad");

        String motivo = edtMotivoEmocion.getText().toString();
        String resumen = buildResumenString(emociones, motivo);

        new AlertDialog.Builder(this)
                .setTitle("Resumen del día")
                .setMessage(resumen)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private String buildResumenString(List<String> emociones, String motivo) {
        return "🍎 Nutrición: " + autoDesayuno.getText() +
                " (" + edtKcalDesayuno.getText() + " kcal, " +
                edtProtDesayuno.getText() + "g proteína)\n\n" +
                "🧠 Mental: " + String.join(", ", emociones) +
                (motivo.isEmpty() ? "" : "\n- Motivo: " + motivo) + "\n\n" +
                "💪 Física: " + edtEjercicio.getText() +
                " (Objetivo: " + spinnerObjetivo.getSelectedItem() + ")";
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
            startActivity(new Intent(this, RecomendacionesActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (id == R.id.action_avanzado) {
            startActivity(new Intent(this, RegistroAvanzadoActivity.class));
            return true;
        }else if (id == R.id.action_historial) {
            startActivity(new Intent(this, HistorialActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

