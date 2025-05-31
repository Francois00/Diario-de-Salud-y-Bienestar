package com.example.diariodesaludybienestar;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AppPreferenciasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);

        // Agregar botón de retroceso en la barra superior
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Preferencias");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    // Este método se activa al presionar la flecha de retroceso
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y vuelve a la principal
        return true;
    }
}
