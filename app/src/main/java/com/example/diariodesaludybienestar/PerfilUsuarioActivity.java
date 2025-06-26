package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PerfilUsuarioActivity extends AppCompatActivity {

    Spinner spinnerGenero, spinnerEstilo;
    EditText edtNombre, edtEdad, edtPeso, edtAltura;
    Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        edtNombre = findViewById(R.id.edtNombre);
        spinnerGenero = findViewById(R.id.spinnerGenero);
        spinnerEstilo = findViewById(R.id.spinnerEstilo);
        edtEdad = findViewById(R.id.edtEdad);
        edtPeso = findViewById(R.id.edtPeso);
        edtAltura = findViewById(R.id.edtAltura);
        btnGuardar = findViewById(R.id.btnGuardarPerfil);

        spinnerGenero.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Masculino", "Femenino", "Otro"}));

        spinnerEstilo.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Activo", "No activo"}));

        cargarDatosPerfil(); // 👈 importante

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

                        String genero = snapshot.child("genero").getValue(String.class);
                        String estilo = snapshot.child("estiloVida").getValue(String.class);

                        if (genero != null) {
                            int pos = ((ArrayAdapter<String>) spinnerGenero.getAdapter()).getPosition(genero);
                            spinnerGenero.setSelection(pos);
                        }

                        if (estilo != null) {
                            int pos = ((ArrayAdapter<String>) spinnerEstilo.getAdapter()).getPosition(estilo);
                            spinnerEstilo.setSelection(pos);
                        }
                    }
                });
    }


    private void guardarPerfil() {

        String nombre = edtNombre.getText().toString().trim();
        String genero = spinnerGenero.getSelectedItem().toString();
        String edad = edtEdad.getText().toString();
        String peso = edtPeso.getText().toString();
        String altura = edtAltura.getText().toString();
        String estilo = spinnerEstilo.getSelectedItem().toString();

        if (edad.isEmpty() || peso.isEmpty() || altura.isEmpty()) {
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
}
