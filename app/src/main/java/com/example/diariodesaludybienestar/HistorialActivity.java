package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private List<Registro> registros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistorialAdapter(registros);
        recyclerView.setAdapter(adapter);



        cargarHistorial();
    }

    private void cargarHistorial() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Registros");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                registros.clear();
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String fecha = dateSnapshot.getKey();
                    Map<String, Object> registroMap = (Map<String, Object>) dateSnapshot.getValue();

                    Registro registro = new Registro(
                            fecha,
                            ((Map<String, Object>) registroMap.get("nutricion")).get("desayuno").toString(),
                            ((Map<String, Object>) registroMap.get("mental")).get("ansiedad").toString(),
                            ((Map<String, Object>) registroMap.get("fisica")).get("ejercicio").toString()
                    );
                    registros.add(registro);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar error
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