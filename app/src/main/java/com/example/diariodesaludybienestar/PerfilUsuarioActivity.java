package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PerfilUsuarioActivity extends AppCompatActivity {

    Spinner spinnerGenero, spinnerEstilo, spinnerObjetivo, spinnerNivel;
    EditText edtNombre, edtEdad, edtPeso, edtAltura;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        edtNombre = findViewById(R.id.edtNombre);
        edtEdad = findViewById(R.id.edtEdad);
        edtPeso = findViewById(R.id.edtPeso);
        edtAltura = findViewById(R.id.edtAltura);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        spinnerEstilo = findViewById(R.id.spinnerEstilo);
        spinnerObjetivo = findViewById(R.id.spinnerObjetivo);
        spinnerNivel = findViewById(R.id.spinnerNivel);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        // Configuración de spinners
        configurarSpinners();

        cargarDatosPerfil();
        btnGuardar.setOnClickListener(v -> guardarPerfil());
    }

    private void configurarSpinners() {
        // Género
        spinnerGenero.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Masculino", "Femenino", "Otro", "Prefiero no decir"}));

        // Nivel de actividad (reemplaza al simple "Activo/No activo")
        spinnerNivel.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Principiante/Sedentario",       // Poca o ninguna actividad
                        "Moderadamente Activo",          // Ejercicio intermitente
                        "Activo/Consolidado",            // Rutina establecida
                        "Atleta/Avanzado",               // Entrenamiento intensivo
                        "Con necesidades específicas"    // Embarazo, lesiones, etc.
                }));

        // Objetivos ampliados
        spinnerObjetivo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Perder peso",
                        "Ganar músculo",
                        "Mantenerse",
                        "Reducir el Estrés",
                        "Mejorar el Sueño",
                        "Aumentar Energía",
                        "Mejorar Resistencia",
                        "Comer Más Saludable",
                        "Mejorar Salud Cardiovascular",
                        "Recuperación de Lesión",
                        "Preparación para Evento",
                        "Otro (especificar en observaciones)"
                }));

        // Estilo de vida (simplificado ya que tenemos nivel de actividad)
        spinnerEstilo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Urbano (mucho tiempo sentado)",
                        "Equilibrado (mezcla de actividad y sedentarismo)",
                        "Activo (mucho movimiento en el día)"
                }));
    }

    private void cargarDatosPerfil() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Perfil")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        edtNombre.setText(snapshot.child("nombre").getValue(String.class));
                        edtEdad.setText(snapshot.child("edad").getValue(String.class));
                        edtPeso.setText(snapshot.child("peso").getValue(String.class));
                        edtAltura.setText(snapshot.child("altura").getValue(String.class));

                        seleccionarEnSpinner(spinnerGenero, snapshot.child("genero").getValue(String.class));
                        seleccionarEnSpinner(spinnerNivel, snapshot.child("nivelActividad").getValue(String.class));
                        seleccionarEnSpinner(spinnerEstilo, snapshot.child("estiloVida").getValue(String.class));
                        seleccionarEnSpinner(spinnerObjetivo, snapshot.child("objetivo").getValue(String.class));
                    }
                });
    }

    private void seleccionarEnSpinner(Spinner spinner, String valor) {
        if (valor != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int pos = adapter.getPosition(valor);
            if (pos >= 0) {
                spinner.setSelection(pos);
            }
        }
    }

    private void guardarPerfil() {
        String nombre = edtNombre.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();
        String edad = edtEdad.getText().toString().trim();
        String peso = edtPeso.getText().toString().trim();
        String altura = edtAltura.getText().toString().trim();
        String nivelActividad = spinnerNivel.getSelectedItem().toString();
        String estilo = spinnerEstilo.getSelectedItem().toString();
        String objetivo = spinnerObjetivo.getSelectedItem().toString();

        if (nombre.isEmpty() || edad.isEmpty() || peso.isEmpty() || altura.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> perfil = new HashMap<>();
        perfil.put("nombre", nombre);
        perfil.put("genero", genero);
        perfil.put("edad", edad);
        perfil.put("peso", peso);
        perfil.put("altura", altura);
        perfil.put("nivelActividad", nivelActividad);
        perfil.put("estiloVida", estilo);
        perfil.put("objetivo", objetivo);

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Perfil")
                .setValue(perfil)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });

        // Validar campos vacíos
        if (edtNombre.getText().toString().trim().isEmpty() ||
                edtEdad.getText().toString().trim().isEmpty() ||
                edtPeso.getText().toString().trim().isEmpty() ||
                edtAltura.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formatos numéricos
        try {
            int edad1 = Integer.parseInt(edtEdad.getText().toString());
            double peso1 = Double.parseDouble(edtPeso.getText().toString());
            double altura1= Double.parseDouble(edtAltura.getText().toString());

            if (edad1 <= 0 || peso1 <= 0 || altura1 <= 0) {
                Toast.makeText(this, "Edad, peso y altura deben ser valores positivos", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Edad, peso y altura deben ser números válidos", Toast.LENGTH_SHORT).show();
            return;
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

        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}