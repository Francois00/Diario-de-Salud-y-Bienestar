package com.example.diariodesaludybienestar;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    EditText etComida, etEjercicio, etSueno, etEstado;
    Button btnVerPrefs, btnMostrarPrefs, btnGuardarActividad, btnCrearActividad, btnEditarActividad, btnBorrarActividad;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etComida = findViewById(R.id.etComida);
        etEjercicio = findViewById(R.id.etEjercicio);
        etSueno = findViewById(R.id.etSueno);
        etEstado = findViewById(R.id.etEstado);

        btnVerPrefs = findViewById(R.id.btnVerPrefs);
        btnMostrarPrefs = findViewById(R.id.btnMostrarPrefs);
        btnGuardarActividad = findViewById(R.id.btnGuardarActividad);
        btnCrearActividad = findViewById(R.id.btnCrearActividad);
        btnEditarActividad = findViewById(R.id.btnEditarActividad);
        btnBorrarActividad = findViewById(R.id.btnBorrarActividad);

        dbRef = FirebaseDatabase.getInstance().getReference("Registros");

        btnGuardarActividad.setOnClickListener(v -> guardarActividad());
        btnCrearActividad.setOnClickListener(v -> crearActividad());
        btnEditarActividad.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ListaActividadesActivity.class);
            i.putExtra("modo", "editar");
            startActivityForResult(i, 100);
        });
        btnBorrarActividad.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ListaActividadesActivity.class);
            i.putExtra("modo", "borrar");
            startActivityForResult(i, 200);
        });

        btnVerPrefs.setOnClickListener(v -> startActivity(new Intent(this, AppPreferenciasActivity.class)));
        btnMostrarPrefs.setOnClickListener(v -> mostrarPreferencias());
    }

    void guardarActividad() {
        String comida = etComida.getText().toString();
        String ejercicio = etEjercicio.getText().toString();
        String sueno = etSueno.getText().toString();
        String estado = etEstado.getText().toString();
        String fecha = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (TextUtils.isEmpty(comida) || TextUtils.isEmpty(ejercicio)) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Registro r = new Registro(fecha, fecha, comida, ejercicio, sueno, estado);

        // Firebase
        dbRef.child(fecha).setValue(r);

        // SQLite
        AdminSQLiteOpenHelper helper = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("fecha", fecha);
        values.put("comida", comida);
        values.put("ejercicio", ejercicio);
        values.put("sueno", sueno);
        values.put("estado", estado);
        db.insertWithOnConflict("registros", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        // Archivo
        String datos = fecha + "," + comida + "," + ejercicio + "," + sueno + "," + estado;
        try (FileOutputStream fos = openFileOutput(fecha + ".txt", Context.MODE_PRIVATE)) {
            fos.write(datos.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Actividad guardada", Toast.LENGTH_SHORT).show();
    }

    void crearActividad() {
        etComida.setText("");
        etEjercicio.setText("");
        etSueno.setText("");
        etEstado.setText("");
        Toast.makeText(this, "Listo para crear nueva actividad", Toast.LENGTH_SHORT).show();
    }

    void mostrarPreferencias() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String usuario = prefs.getString("usuario", "No definido");
        boolean notificaciones = prefs.getBoolean("notificaciones", true);
        Toast.makeText(this, "Usuario: " + usuario + "\nNotif: " + notificaciones, Toast.LENGTH_LONG).show();
    }

    void cargarActividadPorFecha(String fecha) {
        dbRef.child(fecha).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Registro r = snapshot.getValue(Registro.class);
                if (r != null) {
                    etComida.setText(r.comida);
                    etEjercicio.setText(r.ejercicio);
                    etSueno.setText(r.sueno);
                    etEstado.setText(r.estado);
                    Toast.makeText(MainActivity.this, "Cargado para editar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error al cargar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void borrarActividadPorFecha(String fecha) {
        dbRef.child(fecha).removeValue();

        AdminSQLiteOpenHelper helper = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("registros", "fecha=?", new String[]{fecha});
        db.close();

        deleteFile(fecha + ".txt");

        Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String fecha = data.getStringExtra("fecha");
            if (requestCode == 100) {
                cargarActividadPorFecha(fecha);
            } else if (requestCode == 200) {
                borrarActividadPorFecha(fecha);
            }
        }
    }
}
