package com.example.diariodesaludybienestar;

import android.content.*;
import android.database.sqlite.*;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.io.*;

public class EditarActividadActivity extends AppCompatActivity {

    EditText etComida, etEjercicio, etSueno, etEstado;
    Button btnActualizar, btnEliminar;
    String fecha;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        etComida = findViewById(R.id.etComidaEditar);
        etEjercicio = findViewById(R.id.etEjercicioEditar);
        etSueno = findViewById(R.id.etSuenoEditar);
        etEstado = findViewById(R.id.etEstadoEditar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar = findViewById(R.id.btnEliminar);

        fecha = getIntent().getStringExtra("fecha");
        dbRef = FirebaseDatabase.getInstance().getReference("Registros");

        cargarActividad();

        btnActualizar.setOnClickListener(v -> actualizarActividad());
        btnEliminar.setOnClickListener(v -> eliminarActividad());
    }

    void cargarActividad() {
        dbRef.child(fecha).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Registro r = snapshot.getValue(Registro.class);
                if (r != null) {
                    etComida.setText(r.comida);
                    etEjercicio.setText(r.ejercicio);
                    etSueno.setText(r.sueno);
                    etEstado.setText(r.estado);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditarActividadActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void actualizarActividad() {
        String comida = etComida.getText().toString();
        String ejercicio = etEjercicio.getText().toString();
        String sueno = etSueno.getText().toString();
        String estado = etEstado.getText().toString();

        Registro r = new Registro(fecha, fecha, comida, ejercicio, sueno, estado);

        // Firebase
        dbRef.child(fecha).setValue(r);

        // SQLite
        AdminSQLiteOpenHelper helper = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("comida", comida);
        values.put("ejercicio", ejercicio);
        values.put("sueno", sueno);
        values.put("estado", estado);
        db.update("registros", values, "fecha=?", new String[]{fecha});
        db.close();

        // Archivo
        String datos = fecha + "," + comida + "," + ejercicio + "," + sueno + "," + estado;
        try (FileOutputStream fos = openFileOutput(fecha + ".txt", Context.MODE_PRIVATE)) {
            fos.write(datos.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Actividad actualizada", Toast.LENGTH_SHORT).show();
    }

    void eliminarActividad() {

        // Firebase
        dbRef.child(fecha).removeValue();

        // SQLite
        AdminSQLiteOpenHelper helper = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("registros", "fecha=?", new String[]{fecha});
        db.close();

        // Archivo
        deleteFile(fecha + ".txt");

        Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
        finish(); // volver atrás
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Cierra esta actividad y vuelve a la principal
        return true;
    }
}
