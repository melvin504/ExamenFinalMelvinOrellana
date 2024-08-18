package com.example.examenfinal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CrearEntrevistaActivity extends AppCompatActivity {

    static final int peticion_acceso_camara = 101;
    static final int peticion_captura_imagen = 102;
    ImageView ObjectoImagen;
    Button btncaptura, btnenviar;
    Button recordButton, stopButton, playButton;
    EditText descripciontxt,periodistatxt;
    TextView fecha;
    private MediaRecorder recorder;
    private MediaPlayer player;
    String fileName;
    Bitmap imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_entrvista);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ObjectoImagen = (ImageView) findViewById(R.id.imageView);
        btncaptura = (Button) findViewById(R.id.btntakefoto);
        fecha = (TextView) findViewById(R.id.textViewFecha);

        btncaptura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permisos();
            }
        });

        recordButton = findViewById(R.id.recordButton);
        stopButton = findViewById(R.id.stopButton);
        playButton = findViewById(R.id.playButton);

        fileName = getExternalCacheDir().getAbsolutePath() + "/audiorecordtest.3gp";

        // Solicitar permisos si no están otorgados
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
                recordButton.setEnabled(false);
                stopButton.setEnabled(true);
                playButton.setEnabled(false);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                recordButton.setEnabled(true);
                stopButton.setEnabled(false);
                playButton.setEnabled(true);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlaying();
            }
        });

        descripciontxt = (EditText) findViewById(R.id.editTextdescription);
        periodistatxt = (EditText) findViewById(R.id.editTextPeriodista);
        showCurrentDate(this);

        btnenviar = (Button) findViewById(R.id.btnupload);
        btnenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descrip = descripciontxt.getText().toString();
                String perio = periodistatxt.getText().toString();
                if(fileName.length()==0||imagen == null||descripciontxt.getText().length()==0||periodistatxt.getText().length()==0){
                    Toast.makeText(CrearEntrevistaActivity.this,"Llene todos los campos",Toast.LENGTH_SHORT).show();
                }else{
                    uploadDataToFirebase( imagen, fileName, descrip, perio,fecha.getText().toString());
                    finish();
                    startActivity(getIntent());
                }
            }
        });




    }

    // fotografia codigo

    private void Permisos() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String [] {Manifest.permission.CAMERA},
                    peticion_acceso_camara);
        } else {
            TomarFoto();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == peticion_acceso_camara) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TomarFoto();
            } else {
                Toast.makeText(getApplicationContext(), "Acceso Denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void TomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager())!= null) {
            startActivityForResult(intent,  peticion_captura_imagen);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==  peticion_captura_imagen) {
            Bundle extras = data.getExtras();
            imagen = (Bitmap) extras.get("data");
            ObjectoImagen.setImageBitmap(imagen);
        }
    }

    //fin de codigo de foto
    // comienzo del audio

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            Toast.makeText(this, "Grabación detenida", Toast.LENGTH_SHORT).show();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
            Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
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
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    //fin
    //fecha
    public void showCurrentDate(Context context) {
        // Obtener la fecha actual
        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        fecha.setText(""+currentDate);
        //Toast.makeText(context, "Fecha actual: " + currentDate, Toast.LENGTH_SHORT).show();

    }

    private void uploadDataToFirebase(Bitmap imagenBitmap, String audioPath, String descripcion, String periodista, String fecha) {
        // Referencia a Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Referencia a Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("entradas");

        // Generar un nuevo ID para la entrada
        String id = databaseRef.push().getKey();

        // Crear una referencia de almacenamiento para la imagen
        String imagenPath = "imagenes/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imagenRef = storageRef.child(imagenPath);

        // Convertir el bitmap de la imagen a un array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] dataImagen = baos.toByteArray();

        // Subir la imagen a Firebase Storage
        imagenRef.putBytes(dataImagen).addOnSuccessListener(taskSnapshot -> {
            // Obtener la URL de descarga de la imagen
            imagenRef.getDownloadUrl().addOnSuccessListener(imagenUrl -> {
                // Crear una referencia de almacenamiento para el audio
                String audioPathInStorage = "audios/" + UUID.randomUUID().toString() + ".3gp";
                StorageReference audioRef = storageRef.child(audioPathInStorage);

                // Subir el archivo de audio a Firebase Storage
                Uri file = Uri.fromFile(new File(audioPath));
                audioRef.putFile(file).addOnSuccessListener(taskSnapshot1 -> {
                    // Obtener la URL de descarga del audio
                    audioRef.getDownloadUrl().addOnSuccessListener(audioUrl -> {
                        // Crear un objeto para almacenar en Realtime Database
                        Map<String, Object> entrada = new HashMap<>();
                        entrada.put("id", id);  // Guardar el ID generado
                        entrada.put("imagenUrl", imagenUrl.toString());
                        entrada.put("audioUrl", audioUrl.toString());
                        entrada.put("descripcion", descripcion);
                        entrada.put("periodista", periodista);
                        entrada.put("fecha", fecha);

                        // Guardar la entrada en Realtime Database usando el ID generado
                        databaseRef.child(id).setValue(entrada).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Datos subidos exitosamente", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Error al subir datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Error al obtener URL del audio", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Error al subir el audio", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getApplicationContext(), "Error al obtener URL de la imagen", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getApplicationContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
        });
    }


}