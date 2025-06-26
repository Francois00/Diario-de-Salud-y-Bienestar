package com.example.diariodesaludybienestar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

        Map<String, Object> registro = new HashMap<>();
        registro.put("horasDormidas", horas);
        registro.put("actividadFisica", actividad);
        registro.put("estadoAnimo", estado);

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Diario")
                .child(fecha)
                .setValue(registro)
                .addOnSuccessListener(aVoid -> mostrarRecomendaciones(horas, actividad, estado));
    }

    private void mostrarRecomendaciones(String horas, String actividad, String estado) {
        ArrayList<String> metas = new ArrayList<>();

        int h = Integer.parseInt(horas);
        if (h < 6) metas.add("Dormir al menos 7 horas esta noche");
        if (actividad.equals("Nada")) metas.add("Caminar 30 minutos hoy");
        else if (actividad.equals("Menos de 30 min")) metas.add("Aumentar la actividad física a 30 minutos");

        if (estado.equalsIgnoreCase("Estresado")) metas.add("Hacer respiraciones profundas 3 veces al día");
        if (estado.equalsIgnoreCase("Cansado")) metas.add("Reducir uso de pantallas antes de dormir");
        if (estado.equalsIgnoreCase("Triste")) metas.add("Llamar a un amigo o familiar cercano");
        if (estado.equalsIgnoreCase("Motivado")) metas.add("Aprovecha y haz 10 minutos de estiramiento");

        // Metas adicionales
        metas.add("Beber 2 litros de agua");
        metas.add("Tomarte 5 minutos de descanso cada hora");
        metas.add("Escuchar música relajante");
        metas.add("Evitar cafeína después de las 5pm");
        metas.add("Meditar por 10 minutos");
        metas.add("Tomar luz solar al menos 15 minutos");
        metas.add("Evitar el celular 30 minutos antes de dormir");
        metas.add("Leer 5 páginas de un libro que disfrutes");
        metas.add("Desconectarte 1h de redes sociales");
        metas.add("Camina por un parque o espacio verde");
        metas.add("Anota 3 cosas buenas del día");

        // Guardar metas en Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Metas")
                .child(fecha)
                .setValue(metas);

        // Ir a la pantalla de metas
        Intent intent = new Intent(this, RecomendacionesActivity.class);
        intent.putStringArrayListExtra("recomendaciones", metas);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, PerfilUsuarioActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_metas) {
            startActivity(new Intent(this, RecomendacionesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
