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

        // Populate sleep hours spinner (0-12 hours)
        ArrayList<String> horasSueno = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            horasSueno.add(String.valueOf(i));
        }
        ArrayAdapter<String> adapterHoras = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, horasSueno);
        adapterHoras.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHorasSueno.setAdapter(adapterHoras);

        // Populate physical activity spinner
        ArrayList<String> actividades = new ArrayList<>();
        actividades.add("Nada");
        actividades.add("10 min");
        actividades.add("15 min");
        actividades.add("20 min");
        actividades.add("30 min");
        actividades.add("45 min");
        actividades.add("1 hora");
        actividades.add("1:30 horas");
        actividades.add("2 horas");
        ArrayAdapter<String> adapterActividad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, actividades);
        adapterActividad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerActividad.setAdapter(adapterActividad);


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