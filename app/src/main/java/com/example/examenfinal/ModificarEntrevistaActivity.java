package com.example.examenfinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ModificarEntrevistaActivity extends AppCompatActivity {

    private EditText descripcionEditText;
    private TextView fechaTextView;
    private ImageView imagenImageView;
    private Button guardarButton;

    private String idEntrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modificar_entrevista);

        // Inicializar vistas
        descripcionEditText = findViewById(R.id.descripcionEditText);
        fechaTextView = findViewById(R.id.fechaTextView);
        imagenImageView = findViewById(R.id.imagenImageView);
        guardarButton = findViewById(R.id.guardarButton);

        // Recuperar el id de la entrevista
        idEntrevista = getIntent().getStringExtra("idEntrevista");

        // Cargar la entrevista desde Firebase
        cargarEntrevista(idEntrevista);

        // Configurar el botón de guardar
        guardarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nuevaDescripcion = descripcionEditText.getText().toString();
                String nuevaFecha = fechaTextView.getText().toString();
                guardarCambios(idEntrevista, nuevaDescripcion, nuevaFecha);
            }
        });
    }

    private void cargarEntrevista(String idEntrevista) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entradas").child(idEntrevista);

        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Entrevista entrevista = dataSnapshot.getValue(Entrevista.class);
                    if (entrevista != null) {
                        // Rellenar los campos de la UI con los datos de la entrevista
                        descripcionEditText.setText(entrevista.getDescripcion());
                        fechaTextView.setText(entrevista.getFecha());

                        // Cargar y mostrar la imagen desde la URL en Firebase Storage
                        Picasso.get().load(entrevista.getImagenUrl()).into(imagenImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ModificarEntrevistaActivity.this, "Error al cargar la entrevista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarCambios(String idEntrevista, String nuevaDescripcion, String nuevaFecha) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entradas").child(idEntrevista);

        Map<String, Object> actualizaciones = new HashMap<>();
        actualizaciones.put("descripcion", nuevaDescripcion);
        actualizaciones.put("fecha", nuevaFecha);
        // Agrega más campos si es necesario

        databaseRef.updateChildren(actualizaciones).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ModificarEntrevistaActivity.this, "Entrevista actualizada exitosamente", Toast.LENGTH_SHORT).show();
                finish();  // Volver a la actividad anterior
            } else {
                Toast.makeText(ModificarEntrevistaActivity.this, "Error al actualizar la entrevista", Toast.LENGTH_SHORT).show();
            }
        });
    }
}