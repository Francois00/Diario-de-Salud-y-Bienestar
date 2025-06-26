package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextInputEditText inputMood, inputSleep, inputNote;
    MaterialButton btnGuardar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // Si no hay sesión, vuelve al login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        inputMood = findViewById(R.id.inputMood);
        inputSleep = findViewById(R.id.inputSleep);
        inputNote = findViewById(R.id.inputNote);
        btnGuardar = findViewById(R.id.btnGuardar);

        Toast.makeText(this, "Bienvenido, " + user.getEmail(), Toast.LENGTH_SHORT).show();

        btnGuardar.setOnClickListener(v -> {
            String mood = inputMood.getText().toString().trim();
            String sleep = inputSleep.getText().toString().trim();
            String note = inputNote.getText().toString().trim();

            String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String resumen = "Fecha: " + fecha + "\n" +
                    "Estado de ánimo: " + mood + "\n" +
                    "Horas de sueño: " + sleep + "\n" +
                    "Comentario: " + note;

            Toast.makeText(this, resumen, Toast.LENGTH_LONG).show();
        });
    }

    // Crea el menú con opción de cerrar sesión
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Acciones del menú
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, PerfilUsuarioActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
