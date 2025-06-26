package com.example.diariodesaludybienestar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FelicitacionesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_felicitaciones);

        Button btnCerrar = findViewById(R.id.btnCerrarApp);
        btnCerrar.setOnClickListener(v -> finishAffinity()); // Cierra toda la app
    }
}
