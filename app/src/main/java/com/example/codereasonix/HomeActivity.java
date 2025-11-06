package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private TextView textoBienvenida;
    private Button botonCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textoBienvenida = findViewById(R.id.txtWelcome);
        botonCerrarSesion = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombre_usuario", "");
        int idCliente = prefs.getInt("id_cliente", -1);

        String mensaje = "Bienvenido a CodeReasonix";
        if (!nombreUsuario.isEmpty()) {
            mensaje += ", " + nombreUsuario;
        }
        if (idCliente != -1) {
            mensaje += " (id_cliente: " + idCliente + ")";
        }

        textoBienvenida.setText(mensaje);

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
