package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerHorasSueno, spinnerActividad;
    RadioGroup radioEstadoAnimo;
    Button btnGuardar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        spinnerHorasSueno = findViewById(R.id.spinnerHorasSueno);
        spinnerActividad = findViewById(R.id.spinnerActividad);
        radioEstadoAnimo = findViewById(R.id.groupEstadoAnimo);
        btnGuardar = findViewById(R.id.btnGuardar);

        spinnerHorasSueno.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.horas_sueno)));

        spinnerActividad.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.actividad_fisica)));

        btnGuardar.setOnClickListener(v -> verificarSiYaRegistroHoy());
    }

    private void verificarSiYaRegistroHoy() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Diario")
                .child(fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Toast.makeText(this, "Ya registraste tus datos hoy. Inténtalo mañana.", Toast.LENGTH_LONG).show();
                    } else {
                        guardarDatos();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al verificar entrada diaria", Toast.LENGTH_SHORT).show());
    }

    private void guardarDatos() {
        String horas = spinnerHorasSueno.getSelectedItem().toString();
        String actividad = spinnerActividad.getSelectedItem().toString();

        int selectedId = radioEstadoAnimo.getCheckedRadioButtonId();
        RadioButton selectedRadio = findViewById(selectedId);
        String estado = selectedRadio != null ? selectedRadio.getText().toString() : "";

        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Guardar diario
        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Diario")
                .child(fecha)
                .setValue(Map.of(
                        "horasDormidas", horas,
                        "actividadFisica", actividad,
                        "estadoAnimo", estado
                ))
                .addOnSuccessListener(aVoid -> generarYGuardarMetas(horas, actividad, estado));
    }

    private void generarYGuardarMetas(String horas, String actividad, String estado) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 1. Metas del día
        ArrayList<String> metasDia = new ArrayList<>();
        int h = Integer.parseInt(horas);
        if (h < 6) metasDia.add("Dormir al menos 7 horas esta noche");
        if (actividad.equals("Nada")) metasDia.add("Caminar 30 minutos hoy");
        else if (actividad.equals("Menos de 30 min")) metasDia.add("Aumentar la actividad física a 30 minutos");

        if (estado.equalsIgnoreCase("Estresado")) metasDia.add("Hacer respiraciones profundas 3 veces al día");
        if (estado.equalsIgnoreCase("Cansado")) metasDia.add("Reducir uso de pantallas antes de dormir");
        if (estado.equalsIgnoreCase("Triste")) metasDia.add("Llamar a un amigo o familiar cercano");
        if (estado.equalsIgnoreCase("Motivado")) metasDia.add("Aprovecha y haz 10 minutos de estiramiento");

        // Extras
        metasDia.add("Beber 2 litros de agua");
        metasDia.add("Meditar por 10 minutos");
        metasDia.add("Tomar luz solar 15 minutos");
        metasDia.add("Evitar pantallas 30 minutos antes de dormir");

        // Guardar metas del día
        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(uid)
                .child("MetasDelDia")
                .child(fecha)
                .setValue(metasDia);

        // 2. Metas objetivo (leer perfil)
        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(uid)
                .child("Perfil")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    try {
                        double peso = Double.parseDouble(snapshot.child("peso").getValue(String.class));
                        double altura = Double.parseDouble(snapshot.child("altura").getValue(String.class));
                        int edad = Integer.parseInt(snapshot.child("edad").getValue(String.class));
                        String estilo = snapshot.child("estiloVida").getValue(String.class);
                        String genero = snapshot.child("genero").getValue(String.class);
                        String objetivo = snapshot.child("objetivo").getValue(String.class);

                        ArrayList<String> metasObjetivo = new ArrayList<>(
                                Recomendador.generarSugerencias(estilo, peso, altura, edad, objetivo, genero)
                        );

                        // Guardar metas objetivo
                        FirebaseDatabase.getInstance()
                                .getReference("Usuarios")
                                .child(uid)
                                .child("MetasObjetivo")
                                .child(fecha)
                                .setValue(metasObjetivo);

                        // Ir a la pantalla de recomendaciones
                        Intent intent = new Intent(this, RecomendacionesActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Toast.makeText(this, "Error leyendo perfil", Toast.LENGTH_SHORT).show();
                    }
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
