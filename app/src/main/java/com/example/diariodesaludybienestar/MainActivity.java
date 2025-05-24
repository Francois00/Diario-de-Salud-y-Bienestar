package com.example.diariodesaludybienestar;


import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    public TextInputEditText inputMood, inputSleep, inputNote;
    MaterialButton btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMood = findViewById(R.id.inputMood);
        inputSleep = findViewById(R.id.inputSleep);
        inputNote = findViewById(R.id.inputNote);
        btnGuardar = findViewById(R.id.btnGuardar);

        btnGuardar.setOnClickListener(v -> {
            String mood = inputMood.getText().toString().trim();
            String sleep = inputSleep.getText().toString().trim();
            String note = inputNote.getText().toString().trim();

            String resumen = "Estado de ánimo: " + mood + "\n" +
                    "Horas de sueño: " + sleep + "\n" +
                    "Comentario: " + note;

            Toast.makeText(this, resumen, Toast.LENGTH_LONG).show();
        });
    }
}
