package com.example.diariodesaludybienestar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorialActivity extends AppCompatActivity implements HistorialAdapter.OnRegistroClickListener {

    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private List<RegistroCompleto> registros = new ArrayList<>();
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Tu Historial Completo");
        }

        recyclerView = findViewById(R.id.recyclerHistorial);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistorialAdapter(registros, this);
        recyclerView.setAdapter(adapter);

        cargarHistorialCompleto();
    }

    private void cargarHistorialCompleto() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference refRegistros = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Registros");

        DatabaseReference refMetas = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("MetasDelDiaMarcadas");

        refRegistros.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot registrosSnapshot) {
                registros.clear();

                // Primero cargar todos los registros
                for (DataSnapshot fechaSnapshot : registrosSnapshot.getChildren()) {
                    String fecha = fechaSnapshot.getKey();
                    Map<String, Object> registroMap = (Map<String, Object>) fechaSnapshot.getValue();

                    // Convertir a objeto RegistroCompleto
                    RegistroCompleto registro = parsearRegistro(fecha, registroMap);
                    registros.add(registro);
                }

                // Luego cargar las metas completadas para cada fecha
                refMetas.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot metasSnapshot) {
                        for (RegistroCompleto registro : registros) {
                            if (metasSnapshot.hasChild(registro.getFecha())) {
                                DataSnapshot metasDiaSnapshot = metasSnapshot.child(registro.getFecha());
                                List<String> metasCompletadas = new ArrayList<>();

                                for (DataSnapshot metaSnapshot : metasDiaSnapshot.getChildren()) {
                                    if (Boolean.TRUE.equals(metaSnapshot.getValue(Boolean.class))) {
                                        metasCompletadas.add(metaSnapshot.getKey());
                                    }
                                }
                                registro.setMetasCompletadas(metasCompletadas);
                            }
                        }

                        // Ordenar por fecha (más reciente primero)
                        Collections.sort(registros, (r1, r2) -> r2.getFecha().compareTo(r1.getFecha()));

                        adapter.notifyDataSetChanged();
                        tvEmptyState.setVisibility(registros.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvEmptyState.setText("Error al cargar metas: " + error.getMessage());
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvEmptyState.setText("Error al cargar registros: " + error.getMessage());
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private RegistroCompleto parsearRegistro(String fecha, Map<String, Object> registroMap) {
        RegistroCompleto registro = new RegistroCompleto(fecha);

        try {
            // Nutrición
            if (registroMap.containsKey("nutricion")) {
                Map<String, Object> nutricion = (Map<String, Object>) registroMap.get("nutricion");

                // Manejar totalKcal que podría ser número o string
                if (nutricion.containsKey("totalKcal")) {
                    Object kcalObj = nutricion.get("totalKcal");
                    if (kcalObj instanceof Number) {
                        registro.setTotalKcal(((Number) kcalObj).intValue());
                    } else if (kcalObj instanceof String) {
                        try {
                            registro.setTotalKcal(Integer.parseInt((String) kcalObj));
                        } catch (NumberFormatException e) {
                            registro.setTotalKcal(0);
                        }
                    }
                }

                // Comidas
                if (nutricion.containsKey("comidas")) {
                    Object comidasObj = nutricion.get("comidas");
                    if (comidasObj instanceof List) {
                        registro.setComidas((List<Map<String, String>>) comidasObj);
                    }
                }
            }

            // Estado mental - manejar valores que podrían ser booleanos o números
            if (registroMap.containsKey("mental")) {
                Map<String, Object> mental = (Map<String, Object>) registroMap.get("mental");

                // Ansiedad
                if (mental.containsKey("ansiedad")) {
                    Object ansiedadObj = mental.get("ansiedad");
                    if (ansiedadObj instanceof Number) {
                        registro.setAnsiedad(((Number) ansiedadObj).floatValue());
                    } else if (ansiedadObj instanceof String) {
                        try {
                            registro.setAnsiedad(Float.parseFloat((String) ansiedadObj));
                        } catch (NumberFormatException e) {
                            registro.setAnsiedad(0);
                        }
                    } else if (ansiedadObj instanceof Boolean) {
                        registro.setAnsiedad(((Boolean) ansiedadObj) ? 1f : 0f);
                    }
                }

                // Energía
                if (mental.containsKey("energia")) {
                    Object energiaObj = mental.get("energia");
                    if (energiaObj instanceof Number) {
                        registro.setEnergia(((Number) energiaObj).floatValue());
                    } else if (energiaObj instanceof String) {
                        try {
                            registro.setEnergia(Float.parseFloat((String) energiaObj));
                        } catch (NumberFormatException e) {
                            registro.setEnergia(0);
                        }
                    } else if (energiaObj instanceof Boolean) {
                        registro.setEnergia(((Boolean) energiaObj) ? 1f : 0f);
                    }
                }

                // Motivo emoción
                if (mental.containsKey("motivo")) {
                    Object motivoObj = mental.get("motivo");
                    registro.setMotivoEmocion(motivoObj != null ? motivoObj.toString() : "");
                }
            }

            // Actividad física
            if (registroMap.containsKey("fisica")) {
                Map<String, Object> fisica = (Map<String, Object>) registroMap.get("fisica");

                // Ejercicio
                if (fisica.containsKey("ejercicio")) {
                    Object ejercicioObj = fisica.get("ejercicio");
                    registro.setEjercicio(ejercicioObj != null ? ejercicioObj.toString() : "");
                }

                // Duración ejercicio
                if (fisica.containsKey("duracion")) {
                    Object duracionObj = fisica.get("duracion");
                    if (duracionObj instanceof Number) {
                        registro.setDuracionEjercicio(String.valueOf(((Number) duracionObj).intValue()));
                    } else if (duracionObj instanceof String) {
                        registro.setDuracionEjercicio((String) duracionObj);
                    } else {
                        registro.setDuracionEjercicio("0");
                    }
                }
            }

            // Sueño
            if (registroMap.containsKey("sueno")) {
                Map<String, Object> sueno = (Map<String, Object>) registroMap.get("sueno");

                // Horas sueño
                if (sueno.containsKey("horas")) {
                    Object horasObj = sueno.get("horas");
                    if (horasObj instanceof Number) {
                        registro.setHorasSueno(String.valueOf(((Number) horasObj).floatValue()));
                    } else if (horasObj instanceof String) {
                        registro.setHorasSueno((String) horasObj);
                    } else {
                        registro.setHorasSueno("0");
                    }
                }

                // Calidad sueño
                if (sueno.containsKey("calidad")) {
                    Object calidadObj = sueno.get("calidad");
                    registro.setCalidadSueno(calidadObj != null ? calidadObj.toString() : "No registrada");
                }
            }
        } catch (Exception e) {
            // Registrar el error pero continuar con los datos que sí se pudieron parsear
            e.printStackTrace();
        }

        return registro;
    }

    @Override
    public void onRegistroClick(int position) {
        // Mostrar detalles completos
        RegistroCompleto registro = registros.get(position);
        mostrarDetallesRegistro(registro);
    }

    @Override
    public void onEditarClick(int position) {
        // Editar registro (abrir RegistroAvanzadoActivity con datos)
        RegistroCompleto registro = registros.get(position);
        editarRegistro(registro);
    }

    @Override
    public void onEliminarClick(int position) {
        // Confirmar eliminación
        RegistroCompleto registro = registros.get(position);
        confirmarEliminacion(registro);
    }

    private void mostrarDetallesRegistro(RegistroCompleto registro) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Registro del " + registro.getFecha());

        StringBuilder detalles = new StringBuilder();

        // Nutrición
        detalles.append("🍎 Nutrición:\n");
        detalles.append("- Total kcal: ").append(registro.getTotalKcal()).append("\n");
        if (registro.getComidas() != null && !registro.getComidas().isEmpty()) {
            for (Map<String, String> comida : registro.getComidas()) {
                detalles.append("- ").append(comida.get("tipo")).append(": ")
                        .append(comida.get("alimento")).append(" (")
                        .append(comida.get("kcal")).append(" kcal)\n");
            }
        }
        detalles.append("\n");

        // Estado mental
        detalles.append("🧠 Estado Mental:\n");
        detalles.append("- Ansiedad: ").append(registro.getAnsiedad()).append("/5\n");
        detalles.append("- Energía: ").append(registro.getEnergia()).append("/5\n");
        if (!registro.getMotivoEmocion().isEmpty()) {
            detalles.append("- Notas: ").append(registro.getMotivoEmocion()).append("\n");
        }
        detalles.append("\n");

        // Actividad física
        detalles.append("💪 Actividad Física:\n");
        detalles.append("- Ejercicio: ").append(registro.getEjercicio()).append("\n");
        detalles.append("- Duración: ").append(registro.getDuracionEjercicio()).append(" minutos\n\n");

        // Sueño
        detalles.append("😴 Sueño:\n");
        detalles.append("- Horas: ").append(registro.getHorasSueno()).append("\n");
        detalles.append("- Calidad: ").append(registro.getCalidadSueno()).append("\n\n");

        // Metas completadas
        if (registro.getMetasCompletadas() != null && !registro.getMetasCompletadas().isEmpty()) {
            detalles.append("✅ Metas Completadas:\n");
            for (String meta : registro.getMetasCompletadas()) {
                detalles.append("- ").append(meta).append("\n");
            }
        }

        builder.setMessage(detalles.toString());
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    private void editarRegistro(RegistroCompleto registro) {
        Intent intent = new Intent(this, RegistroAvanzadoActivity.class);
        intent.putExtra("fecha", registro.getFecha());
        intent.putExtra("comidas", new ArrayList<>(registro.getComidas()));
        intent.putExtra("ansiedad", registro.getAnsiedad());
        intent.putExtra("energia", registro.getEnergia());
        intent.putExtra("motivoEmocion", registro.getMotivoEmocion());
        intent.putExtra("ejercicio", registro.getEjercicio());
        intent.putExtra("duracionEjercicio", registro.getDuracionEjercicio());
        intent.putExtra("horasSueno", registro.getHorasSueno());
        startActivity(intent);
    }

    private void confirmarEliminacion(RegistroCompleto registro) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar el registro del " + registro.getFecha() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarRegistro(registro.getFecha()))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarRegistro(String fecha) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference refRegistros = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("Registros")
                .child(fecha);

        DatabaseReference refMetas = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid())
                .child("MetasDelDiaMarcadas")
                .child(fecha);

        refRegistros.removeValue();
        refMetas.removeValue();

        Toast.makeText(this, "Registro eliminado", Toast.LENGTH_SHORT).show();
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
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}