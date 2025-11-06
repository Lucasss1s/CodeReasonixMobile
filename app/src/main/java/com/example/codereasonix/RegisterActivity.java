package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegisterActivity extends AppCompatActivity {

    private EditText campoNombre, campoEmailRegistro, campoPasswordRegistro;
    private Button botonRegistrar;
    private TextView textoIrALogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        campoNombre = findViewById(R.id.editName);
        campoEmailRegistro = findViewById(R.id.editRegisterEmail);
        campoPasswordRegistro = findViewById(R.id.editRegisterPassword);
        botonRegistrar = findViewById(R.id.btnRegister);
        textoIrALogin = findViewById(R.id.txtGoToLogin);

        botonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hacerRegistro();
            }
        });

        textoIrALogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void hacerRegistro() {
        final String nombre = campoNombre.getText().toString().trim();
        final String email = campoEmailRegistro.getText().toString().trim();
        final String password = campoPasswordRegistro.getText().toString().trim();

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Completá todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Ingresá un email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conexion = null;
                try {
                    URL url = new URL(Config.BASE_URL + "/usuarios/register");
                    conexion = (HttpURLConnection) url.openConnection();
                    conexion.setRequestMethod("POST");
                    conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conexion.setDoOutput(true);

                    JSONObject cuerpo = new JSONObject();
                    cuerpo.put("nombre", nombre);
                    cuerpo.put("email", email);
                    cuerpo.put("password", password);

                    OutputStream salida = conexion.getOutputStream();
                    salida.write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
                    salida.flush();
                    salida.close();

                    int codigoRespuesta = conexion.getResponseCode();

                    InputStream entrada;
                    if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
                        entrada = conexion.getInputStream();
                    } else {
                        entrada = conexion.getErrorStream();
                    }

                    BufferedReader lector = new BufferedReader(
                            new InputStreamReader(entrada, StandardCharsets.UTF_8)
                    );
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = lector.readLine()) != null) {
                        respuesta.append(linea);
                    }
                    lector.close();

                    final String textoRespuesta = respuesta.toString();
                    final int codigoFinal = codigoRespuesta;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            procesarRespuestaRegistro(codigoFinal, textoRespuesta);
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String mensaje = "Error de conexión: " + e.getClass().getSimpleName();
                            if (e.getMessage() != null) {
                                mensaje += " - " + e.getMessage();
                            }
                            Toast.makeText(RegisterActivity.this, mensaje, Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (conexion != null) {
                        conexion.disconnect();
                    }
                }
            }
        }).start();
    }

    private void procesarRespuestaRegistro(int codigoRespuesta, String respuestaTexto) {
        try {
            JSONObject json = new JSONObject(respuestaTexto);

            if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
                JSONObject usuarioJson = json.optJSONObject("usuario");
                JSONObject clienteJson = json.optJSONObject("cliente");

                String nombre = "";
                int idCliente = -1;

                if (usuarioJson != null) {
                    nombre = usuarioJson.optString("nombre", "");
                }
                if (clienteJson != null) {
                    idCliente = clienteJson.optInt("id_cliente", -1);
                }

                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putInt("id_cliente", idCliente)
                        .putString("nombre_usuario", nombre)
                        .apply();

                Toast.makeText(this,
                        "Usuario registrado correctamente",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            } else {
                String mensajeError = json.optString("error", "Error registrando usuario");
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error procesando la respuesta del servidor",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
