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

    Spinner spinnerGenero, spinnerEstilo, spinnerObjetivo;
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
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        spinnerGenero.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Masculino", "Femenino", "Otro"}));

        spinnerEstilo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Activo", "No activo"}));

        spinnerObjetivo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Perder peso", "Ganar músculo", "Mantenerse"}));

        cargarDatosPerfil();
        btnGuardar.setOnClickListener(v -> guardarPerfil());
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
                        seleccionarEnSpinner(spinnerEstilo, snapshot.child("estiloVida").getValue(String.class));
                        seleccionarEnSpinner(spinnerObjetivo, snapshot.child("objetivo").getValue(String.class));
                    }
                });
    }

    private void seleccionarEnSpinner(Spinner spinner, String valor) {
        if (valor != null) {
            ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
            int pos = adapter.getPosition(valor);
            spinner.setSelection(pos);
        }
    }

    private void guardarPerfil() {
        String nombre = edtNombre.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();
        String edad = edtEdad.getText().toString().trim();
        String peso = edtPeso.getText().toString().trim();
        String altura = edtAltura.getText().toString().trim();
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

        if (item.getItemId() == android.R.id.home) {
            // Regresa a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
