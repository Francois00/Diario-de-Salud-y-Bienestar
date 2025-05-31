package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class ListaActividadesActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> fechas = new ArrayList<>();
    ArrayAdapter<String> adapter;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_actividades);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = findViewById(R.id.listViewFechas);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fechas);
        listView.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference("Registros");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fechas.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    fechas.add(child.getKey()); // clave = fecha
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaActividadesActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String fechaSeleccionada = fechas.get(position);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("fecha", fechaSeleccionada);
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y vuelve a la principal
        return true;
    }
}