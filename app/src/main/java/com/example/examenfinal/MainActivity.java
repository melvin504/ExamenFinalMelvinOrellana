package com.example.examenfinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button btnentrevistar,btnmostrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        btnentrevistar = (Button) findViewById(R.id.btnCrearEntrevista);


        btnentrevistar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearentrevista();
            }
        });

        btnmostrar = (Button) findViewById(R.id.btnVerEntrevista);
        btnmostrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verentrevista();
            }
        });

    }

    //funciones
    private void crearentrevista(){
        Intent intent = new Intent(MainActivity.this,CrearEntrevistaActivity.class);
        startActivity(intent);
    }

    private void verentrevista(){
        Intent intent = new Intent(MainActivity.this,VerEntrevistasActivity.class);
        startActivity(intent);
    }

}