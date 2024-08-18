package com.example.examenfinal;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class VerEntrevistasActivity extends AppCompatActivity {
    private ListView listViewEntrevistas;
    private List<Entrevista> listaEntrevistas = new ArrayList<>();
    private EntrevistaAdapter adapter;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_entrevistas);

        listViewEntrevistas = findViewById(R.id.listViewEntrevistas);
        adapter = new EntrevistaAdapter(this, listaEntrevistas);
        listViewEntrevistas.setAdapter(adapter);

        cargarEntrevistasDesdeFirebase();

        listViewEntrevistas.setOnItemClickListener((parent, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);
            reproducirAudio(entrevistaSeleccionada.getAudioUrl());
        });

        listViewEntrevistas.setOnItemLongClickListener((parent, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);

            // Mostrar un diálogo de confirmación
            mostraropciones(entrevistaSeleccionada, entrevistaSeleccionada.getId());
            return true;
        });

    }

    private void cargarEntrevistasDesdeFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entradas");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaEntrevistas.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Entrevista entrevista = snapshot.getValue(Entrevista.class);
                    listaEntrevistas.add(entrevista);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(VerEntrevistasActivity.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reproducirAudio(String audioUrl) {
        if (player != null) {
            player.release();
        }
        player = new MediaPlayer();
        try {
            player.setDataSource(audioUrl);
            player.prepare();
            player.start();
            Toast.makeText(this, "Reproduciendo entrevista", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error al reproducir el audio", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    //dialog menu
    private void mostraropciones(Entrevista entrevista, String Mentrevista){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una acción")
                .setItems(new CharSequence[]{"Eliminar", "Modificar", "Cancelar"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Acción para la opción 1
                                eliminarEntrevista(entrevista);
                                break;
                            case 1:
                                // Acción para la opción 2
                                modificar(Mentrevista);
                                break;
                            case 2:
                                // Acción para la opción 3

                                break;

                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Eliminar entrevista
    private void eliminarEntrevista(Entrevista entrevista) {
        if (entrevista.getId() != null) {
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entradas").child(entrevista.getId());

            // Eliminar datos de Firebase Realtime Database
            databaseRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(VerEntrevistasActivity.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(VerEntrevistasActivity.this, "Error al eliminar la entrevista", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Error: ID de la entrevista es nulo", Toast.LENGTH_SHORT).show();
        }
    }

    private void modificar(String entrevista){
        Intent intent = new Intent(VerEntrevistasActivity.this, ModificarEntrevistaActivity.class);
        intent.putExtra("idEntrevista", entrevista);
        startActivity(intent);

    }


}