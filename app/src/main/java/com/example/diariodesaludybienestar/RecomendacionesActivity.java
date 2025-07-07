package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecomendacionesActivity extends AppCompatActivity {

    private LinearLayout layoutMetasDia, layoutMetasObjetivo;
    private ArrayList<CheckBox> checksDia = new ArrayList<>();
    private ArrayList<CheckBox> checksObjetivo = new ArrayList<>();
    private Button btnGuardar;
    private String fecha;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendaciones);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        layoutMetasDia = findViewById(R.id.layoutMetasDia);
        layoutMetasObjetivo = findViewById(R.id.layoutMetasObjetivo);
        btnGuardar = findViewById(R.id.btnGuardarMetas);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Mostrar metas
        cargarMetasDesdeFirebase();

        btnGuardar.setOnClickListener(v -> guardarMetasCompletadas());
    }

    private void cargarMetasDesdeFirebase() {
        String uid = user.getUid();

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(uid).child("MetasDelDia").child(fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String> metasDia = new ArrayList<>();
                    for (DataSnapshot meta : snapshot.getChildren()) {
                        metasDia.add(meta.getValue(String.class));
                    }
                    mostrarCheckboxes(metasDia, layoutMetasDia, checksDia, "MetasDelDiaMarcadas");
                });

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(uid).child("MetasObjetivo").child(fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String> metasObj = new ArrayList<>();
                    for (DataSnapshot meta : snapshot.getChildren()) {
                        metasObj.add(meta.getValue(String.class));
                    }
                    mostrarCheckboxes(metasObj, layoutMetasObjetivo, checksObjetivo, "MetasObjetivoMarcadas");
                });
    }

    private void mostrarCheckboxes(ArrayList<String> metas, LinearLayout layout, ArrayList<CheckBox> listaChecks, String ramaMarcadas) {
        listaChecks.clear();
        layout.removeAllViews();

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child(ramaMarcadas)
                .child(fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Boolean> marcadas = new HashMap<>();
                    for (DataSnapshot s : snapshot.getChildren()) {
                        marcadas.put(s.getKey(), s.getValue(Boolean.class));
                    }

                    for (String meta : metas) {
                        CheckBox cb = new CheckBox(this);
                        cb.setText(meta);
                        cb.setChecked(Boolean.TRUE.equals(marcadas.get(meta)));
                        layout.addView(cb);
                        listaChecks.add(cb);
                    }
                });
    }

    private void guardarMetasCompletadas() {
        guardarGrupo("MetasDelDiaMarcadas", checksDia);
        guardarGrupo("MetasObjetivoMarcadas", checksObjetivo);

        Toast.makeText(this, "Metas guardadas", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, FelicitacionesActivity.class));
        finish();
    }

    private void guardarGrupo(String ruta, ArrayList<CheckBox> checks) {
        Map<String, Boolean> datos = new HashMap<>();
        for (CheckBox cb : checks) {
            datos.put(cb.getText().toString(), cb.isChecked());
        }

        FirebaseDatabase.getInstance().getReference("Usuarios")
                .child(user.getUid())
                .child(ruta)
                .child(fecha)
                .setValue(datos);
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
