package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecomendacionesActivity extends AppCompatActivity {

    private LinearLayout layoutMetas;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private Button btnGuardarMetas;
    private String fecha;
    private DatabaseReference metasMarcadasRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomendaciones);

        layoutMetas = findViewById(R.id.layoutMetas);
        btnGuardarMetas = findViewById(R.id.btnGuardarMetas);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }

        fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        metasMarcadasRef = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("MetasMarcadas")
                .child(fecha);

        ArrayList<String> recibidas = getIntent().getStringArrayListExtra("recomendaciones");
        if (recibidas != null) {
            mostrarCheckboxes(recibidas);
        } else {
            cargarMetasDesdeFirebase();
        }

        btnGuardarMetas.setOnClickListener(v -> guardarMetasCompletadas());
    }

    private void mostrarCheckboxes(ArrayList<String> lista) {
        checkBoxes.clear();
        layoutMetas.removeAllViews();

        metasMarcadasRef.get().addOnSuccessListener(snapshot -> {
            Map<String, Boolean> marcadas = new HashMap<>();
            for (DataSnapshot snap : snapshot.getChildren()) {
                marcadas.put(snap.getKey(), snap.getValue(Boolean.class));
            }

            for (String meta : lista) {
                CheckBox cb = new CheckBox(this);
                cb.setText(meta);
                cb.setChecked(marcadas.containsKey(meta) && Boolean.TRUE.equals(marcadas.get(meta)));
                layoutMetas.addView(cb);
                checkBoxes.add(cb);
            }
        });
    }

    private void cargarMetasDesdeFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(uid)
                .child("Metas")
                .child(fecha)
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String> metasGuardadas = new ArrayList<>();
                    for (DataSnapshot metaSnap : snapshot.getChildren()) {
                        metasGuardadas.add(metaSnap.getValue(String.class));
                    }
                    mostrarCheckboxes(metasGuardadas);
                });
    }

    private void guardarMetasCompletadas() {
        Map<String, Boolean> estadoMetas = new HashMap<>();

        for (CheckBox cb : checkBoxes) {
            estadoMetas.put(cb.getText().toString(), cb.isChecked());
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("MetasMarcadas")
                .child(fecha)
                .setValue(estadoMetas)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Metas guardadas", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, FelicitacionesActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
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
