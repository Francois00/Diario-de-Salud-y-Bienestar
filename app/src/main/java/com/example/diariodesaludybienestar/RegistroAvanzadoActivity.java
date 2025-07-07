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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.*;

public class RegistroAvanzadoActivity extends AppCompatActivity {

    // Views
    private Spinner spinnerComidaTipo;
    private AutoCompleteTextView autoComidaAlimento;
    private EditText edtKcalComida, edtProtComida, edtMotivoEmocion, edtEjercicio, edtDuracionEjercicio, edtHorasSueno;
    private RatingBar ratingAnsiedad, ratingTristeza, ratingIrritabilidad, ratingEnergia, ratingFelicidad;
    private Spinner spinnerObjetivo, spinnerEjercicioTipo;
    private Button btnGuardar, btnAgregarComida;
    private NutritionAPI nutritionAPI;
    private LinearLayout comidasContainer;
    private List<Map<String, String>> comidasList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_avanzado);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        nutritionAPI = new NutritionAPI(this);
        initViews();
        setupSpinners();
        setupAutocomplete();
        setupButtons();
        setupRatingBars();

        // Verificar si estamos editando un registro existente
        if (getIntent().hasExtra("fecha")) {
            String fecha = getIntent().getStringExtra("fecha");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editando registro del " + fecha);
            }

            // Cargar datos existentes
            List<Map<String, String>> comidas = (List<Map<String, String>>) getIntent().getSerializableExtra("comidas");
            if (comidas != null) {
                comidasList.addAll(comidas);
                actualizarListaComidasUI();
            }

            edtEjercicio.setText(getIntent().getStringExtra("ejercicio"));
            edtDuracionEjercicio.setText(getIntent().getStringExtra("duracionEjercicio"));
            edtHorasSueno.setText(getIntent().getStringExtra("horasSueno"));
            edtMotivoEmocion.setText(getIntent().getStringExtra("motivoEmocion"));

            ratingAnsiedad.setRating(getIntent().getFloatExtra("ansiedad", 0));
            ratingEnergia.setRating(getIntent().getFloatExtra("energia", 0));
        }
    }

    private void initViews() {
        spinnerComidaTipo = findViewById(R.id.spinnerComidaTipo);
        autoComidaAlimento = findViewById(R.id.autoComidaAlimento);
        edtKcalComida = findViewById(R.id.edtKcalComida);
        edtProtComida = findViewById(R.id.edtProtComida);
        ratingAnsiedad = findViewById(R.id.ratingAnsiedad);
        ratingTristeza = findViewById(R.id.ratingTristeza);
        ratingIrritabilidad = findViewById(R.id.ratingIrritabilidad);
        ratingEnergia = findViewById(R.id.ratingEnergia);
        ratingFelicidad = findViewById(R.id.ratingFelicidad);
        edtMotivoEmocion = findViewById(R.id.edtMotivoEmocion);
        spinnerObjetivo = findViewById(R.id.spinnerObjetivoFisico);
        spinnerEjercicioTipo = findViewById(R.id.spinnerEjercicioTipo);
        edtEjercicio = findViewById(R.id.edtEjercicio);
        edtDuracionEjercicio = findViewById(R.id.edtDuracionEjercicio);
        edtHorasSueno = findViewById(R.id.edtHorasSueno);
        btnGuardar = findViewById(R.id.btnGuardarRegistro);
        btnAgregarComida = findViewById(R.id.btnAgregarComida);
        comidasContainer = findViewById(R.id.comidasContainer);
    }

    private void setupSpinners() {
        // Spinner tipo de comida
        ArrayAdapter<CharSequence> comidaAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_comida, android.R.layout.simple_spinner_item);
        comidaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerComidaTipo.setAdapter(comidaAdapter);

        // Spinner objetivo físico
        ArrayAdapter<CharSequence> objetivoAdapter = ArrayAdapter.createFromResource(this,
                R.array.objetivos_fisicos, android.R.layout.simple_spinner_item);
        objetivoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjetivo.setAdapter(objetivoAdapter);

        spinnerObjetivo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sugerirEjercicio();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Spinner tipo de ejercicio
        ArrayAdapter<CharSequence> ejercicioAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_ejercicio, android.R.layout.simple_spinner_item);
        ejercicioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEjercicioTipo.setAdapter(ejercicioAdapter);
    }

    private void sugerirEjercicio() {
        String objetivo = spinnerObjetivo.getSelectedItem().toString();
        String ejercicioSugerido = "";
        String duracionSugerida = "";

        switch (objetivo) {
            case "Perder peso":
                ejercicioSugerido = "Cardio intenso";
                duracionSugerida = "30";
                break;
            case "Ganar músculo":
                ejercicioSugerido = "Levantamiento de pesas";
                duracionSugerida = "45";
                break;
            case "Mantenimiento":
                ejercicioSugerido = "Entrenamiento funcional";
                duracionSugerida = "30";
                break;
            case "Mejorar flexibilidad":
                ejercicioSugerido = "Yoga";
                duracionSugerida = "40";
                break;
            default:
                ejercicioSugerido = "Caminata rápida";
                duracionSugerida = "20";
        }

        spinnerEjercicioTipo.setSelection(getIndex(spinnerEjercicioTipo, ejercicioSugerido));
        edtDuracionEjercicio.setText(duracionSugerida);
    }
    private void setupRatingBars() {
        // Configura el estilo de las RatingBars
        ratingAnsiedad.setProgressDrawable(getDrawable(R.drawable.custom_ratingbar));
        ratingTristeza.setProgressDrawable(getDrawable(R.drawable.custom_ratingbar));
        ratingIrritabilidad.setProgressDrawable(getDrawable(R.drawable.custom_ratingbar));
        ratingEnergia.setProgressDrawable(getDrawable(R.drawable.custom_ratingbar));
        ratingFelicidad.setProgressDrawable(getDrawable(R.drawable.custom_ratingbar));

        // Configura listeners para mostrar el valor seleccionado
        ratingAnsiedad.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, "Ansiedad: " + rating, Toast.LENGTH_SHORT).show();
        });
        ratingTristeza.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, "Tristeza: " + rating, Toast.LENGTH_SHORT).show();
        });
        ratingIrritabilidad.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, "Irritabilidad: " + rating, Toast.LENGTH_SHORT).show();
        });
        ratingEnergia.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, "Energía: " + rating, Toast.LENGTH_SHORT).show();
        });
        ratingFelicidad.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Toast.makeText(this, "Felicidad: " + rating, Toast.LENGTH_SHORT).show();
        });
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void setupAutocomplete() {
        String[] alimentos = getResources().getStringArray(R.array.alimentos_comunes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                alimentos
        );
        autoComidaAlimento.setAdapter(adapter);

        autoComidaAlimento.setOnItemClickListener((parent, view, position, id) -> {
            String alimento = parent.getItemAtPosition(position).toString();
            buscarInformacionNutricional(alimento);
        });
    }

    private void buscarInformacionNutricional(String alimento) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        nutritionAPI.getNutritionData(alimento, new NutritionAPI.NutritionCallback() {
            @Override
            public void onSuccess(int calories, int protein) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    edtKcalComida.setText(String.valueOf(calories));
                    edtProtComida.setText(String.valueOf(protein));
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegistroAvanzadoActivity.this,
                            "Error: " + errorMessage + "\nPuedes ingresar los datos manualmente",
                            Toast.LENGTH_LONG).show();
                    edtKcalComida.setHint("Ingresa kcal manual");
                    edtProtComida.setHint("Ingresa proteínas manual");
                });
            }
        });
    }

    private void setupButtons() {
        btnAgregarComida.setOnClickListener(v -> agregarComidaALista());

        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                guardarRegistro();
                mostrarResumenDiario();
            }
        });
    }

    private void agregarComidaALista() {
        String tipo = spinnerComidaTipo.getSelectedItem().toString();
        String alimento = autoComidaAlimento.getText().toString().trim();
        String kcal = edtKcalComida.getText().toString().trim();
        String proteinas = edtProtComida.getText().toString().trim();

        if (alimento.isEmpty() || kcal.isEmpty() || proteinas.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos de la comida", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> comida = new HashMap<>();
        comida.put("tipo", tipo);
        comida.put("alimento", alimento);
        comida.put("kcal", kcal);
        comida.put("proteinas", proteinas);
        comidasList.add(comida);

        actualizarListaComidasUI();
        limpiarCamposComida();
    }

    private void actualizarListaComidasUI() {
        comidasContainer.removeAllViews();

        for (Map<String, String> comida : comidasList) {
            View comidaView = getLayoutInflater().inflate(R.layout.item_comida, comidasContainer, false);

            TextView tvTipo = comidaView.findViewById(R.id.tvTipoComida);
            TextView tvAlimento = comidaView.findViewById(R.id.tvAlimento);
            TextView tvNutricion = comidaView.findViewById(R.id.tvNutricion);
            ImageButton btnEliminar = comidaView.findViewById(R.id.btnEliminarComida);

            tvTipo.setText(comida.get("tipo"));
            tvAlimento.setText(comida.get("alimento"));
            tvNutricion.setText(String.format("%s kcal • %s g proteína",
                    comida.get("kcal"), comida.get("proteinas")));

            btnEliminar.setOnClickListener(v -> {
                comidasList.remove(comida);
                actualizarListaComidasUI();
            });

            comidasContainer.addView(comidaView);
        }
    }

    private void limpiarCamposComida() {
        autoComidaAlimento.setText("");
        edtKcalComida.setText("");
        edtProtComida.setText("");
        autoComidaAlimento.requestFocus();
    }

    private boolean validarCampos() {
        if (comidasList.isEmpty()) {
            Toast.makeText(this, "Agrega al menos una comida", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edtEjercicio.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Describe tu ejercicio", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (edtDuracionEjercicio.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Ingresa la duración del ejercicio", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private Map<String, Object> createRegistroMap() {
        // Nutrición
        Map<String, Object> nutricion = new HashMap<>();
        nutricion.put("comidas", comidasList);

        // Calcular totales
        int totalKcal = 0;
        int totalProteinas = 0;
        for (Map<String, String> comida : comidasList) {
            totalKcal += Integer.parseInt(comida.get("kcal"));
            totalProteinas += Integer.parseInt(comida.get("proteinas"));
        }
        nutricion.put("totalKcal", totalKcal);
        nutricion.put("totalProteinas", totalProteinas);

        // Estado mental
        Map<String, Object> mental = new HashMap<>();
        mental.put("ansiedad", ratingAnsiedad.getRating());
        mental.put("tristeza", ratingTristeza.getRating());
        mental.put("irritabilidad", ratingIrritabilidad.getRating());
        mental.put("energia", ratingEnergia.getRating());
        mental.put("felicidad", ratingFelicidad.getRating());
        mental.put("motivo", edtMotivoEmocion.getText().toString());

        // Actividad física
        Map<String, Object> fisica = new HashMap<>();
        fisica.put("ejercicio", edtEjercicio.getText().toString());
        fisica.put("tipoEjercicio", spinnerEjercicioTipo.getSelectedItem().toString());
        fisica.put("duracion", edtDuracionEjercicio.getText().toString());
        fisica.put("objetivo", spinnerObjetivo.getSelectedItem().toString());

        // Sueño
        Map<String, Object> sueno = new HashMap<>();
        sueno.put("horas", edtHorasSueno.getText().toString());
        sueno.put("calidad", calcularCalidadSueno());

        // Registro completo
        Map<String, Object> registro = new HashMap<>();
        registro.put("nutricion", nutricion);
        registro.put("mental", mental);
        registro.put("fisica", fisica);
        registro.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()));

        return registro;
    }

    private void marcarMetaEjercicioSiCorresponde(String uid, String fecha) {
        try {
            int duracion = Integer.parseInt(edtDuracionEjercicio.getText().toString());
            if (duracion >= 30) {
                FirebaseDatabase.getInstance()
                        .getReference("Usuarios")
                        .child(uid)
                        .child("Metas")
                        .child(fecha)
                        .child("ejercicio_30min")
                        .setValue(true);
            }
        } catch (NumberFormatException e) {
            // No hacer nada si no es un número válido
        }
    }

    private void mostrarResumenDiario() {
        StringBuilder resumen = new StringBuilder("🌟 Resumen del día 🌟\n\n");

        // Nutrición
        resumen.append("🍎 Nutrición:\n");
        int totalKcal = 0;
        int totalProteinas = 0;

        for (Map<String, String> comida : comidasList) {
            resumen.append("- ").append(comida.get("tipo")).append(": ")
                    .append(comida.get("alimento")).append(" (")
                    .append(comida.get("kcal")).append(" kcal, ")
                    .append(comida.get("proteinas")).append("g proteína)\n");

            totalKcal += Integer.parseInt(comida.get("kcal"));
            totalProteinas += Integer.parseInt(comida.get("proteinas"));
        }

        resumen.append("\n🔹 Total: ").append(totalKcal).append(" kcal, ")
                .append(totalProteinas).append("g proteína\n\n");

        // Estado mental
        resumen.append("🧠 Estado Mental:\n");
        resumen.append("- Ansiedad: ").append(getEmojiForRating(ratingAnsiedad.getRating())).append("\n");
        resumen.append("- Tristeza: ").append(getEmojiForRating(ratingTristeza.getRating())).append("\n");
        resumen.append("- Irritabilidad: ").append(getEmojiForRating(ratingIrritabilidad.getRating())).append("\n");
        resumen.append("- Energía: ").append(getEmojiForRating(ratingEnergia.getRating())).append("\n");
        resumen.append("- Felicidad: ").append(getEmojiForRating(ratingFelicidad.getRating())).append("\n");

        if (!edtMotivoEmocion.getText().toString().isEmpty()) {
            resumen.append("- Notas: ").append(edtMotivoEmocion.getText().toString()).append("\n");
        }
        resumen.append("\n");

        // Actividad física
        resumen.append("💪 Actividad Física:\n");
        resumen.append("- Ejercicio: ").append(edtEjercicio.getText().toString()).append("\n");
        resumen.append("- Tipo: ").append(spinnerEjercicioTipo.getSelectedItem().toString()).append("\n");
        resumen.append("- Duración: ").append(edtDuracionEjercicio.getText().toString()).append(" minutos\n");
        resumen.append("- Objetivo: ").append(spinnerObjetivo.getSelectedItem().toString()).append("\n\n");

        // Sueño
        resumen.append("😴 Sueño:\n");
        resumen.append("- Horas: ").append(edtHorasSueno.getText().toString()).append("\n");
        resumen.append("- Calidad: ").append(calcularCalidadSueno()).append("\n\n");

        resumen.append("¡Buen trabajo! Sigue así mañana 💪");

        new AlertDialog.Builder(this)
                .setTitle("Resumen Diario")
                .setMessage(resumen.toString())
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setIcon(R.drawable.ic_celebration)
                .show();
    }

    private String getEmojiForRating(float rating) {
        if (rating <= 1) return "😭 " + rating + "/5";
        if (rating <= 2) return "😔 " + rating + "/5";
        if (rating <= 3) return "😐 " + rating + "/5";
        if (rating <= 4) return "🙂 " + rating + "/5";
        return "😊 " + rating + "/5";
    }

    private String calcularCalidadSueno() {
        try {
            float horas = Float.parseFloat(edtHorasSueno.getText().toString());
            if (horas >= 8) return "Excelente";
            if (horas >= 7) return "Buena";
            if (horas >= 6) return "Regular";
            return "Mala";
        } catch (NumberFormatException e) {
            return "No especificada";
        }
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
        } else if (id == R.id.action_historial) {
            startActivity(new Intent(this, HistorialActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}