package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private EditText campoEmail, campoPassword;
    private Button botonLogin;
    private TextView textoIrARegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        campoEmail = findViewById(R.id.editEmail);
        campoPassword = findViewById(R.id.editPassword);
        botonLogin = findViewById(R.id.btnLogin);
        textoIrARegistro = findViewById(R.id.txtGoToRegister);

        botonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hacerLogin();
            }
        });

        textoIrARegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void hacerLogin() {
        final String email = campoEmail.getText().toString().trim();
        final String password = campoPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Completá email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conexion = null;
                try {
                    URL url = new URL(Config.BASE_URL + "/usuarios/login");
                    conexion = (HttpURLConnection) url.openConnection();
                    conexion.setRequestMethod("POST");
                    conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conexion.setDoOutput(true);

                    JSONObject cuerpo = new JSONObject();
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
                            procesarRespuestaLogin(codigoFinal, textoRespuesta);
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
                            Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
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

    private void procesarRespuestaLogin(int codigoRespuesta, String respuestaTexto) {
        try {
            JSONObject json = new JSONObject(respuestaTexto);

            if (codigoRespuesta >= 200 && codigoRespuesta < 300) {
                JSONObject usuarioJson = json.optJSONObject("usuario");
                int idCliente = json.optInt("id_cliente", -1);
                String nombre = "";
                if (usuarioJson != null) {
                    nombre = usuarioJson.optString("nombre", "");
                }

                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putInt("id_cliente", idCliente)
                        .putString("nombre_usuario", nombre)
                        .apply();

                String mensajeBienvenida = "Bienvenido";
                if (!nombre.isEmpty()) {
                    mensajeBienvenida += " " + nombre;
                }
                Toast.makeText(this, mensajeBienvenida, Toast.LENGTH_SHORT).show();

                if (idCliente > 0) {
                    otorgarXpPorLogin(idCliente);
                }

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();

            } else {
                String mensajeError = json.optString("error", "Error en login");
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this,
                    "Error procesando la respuesta del servidor",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void otorgarXpPorLogin(final int idCliente) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conexion = null;
                try {
                    URL url = new URL(Config.BASE_URL + "/gamificacion/login-xp");
                    conexion = (HttpURLConnection) url.openConnection();
                    conexion.setRequestMethod("POST");
                    conexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conexion.setDoOutput(true);

                    JSONObject cuerpo = new JSONObject();
                    cuerpo.put("id_cliente", idCliente);

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
                            procesarRespuestaXpLogin(codigoFinal, textoRespuesta);
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                } finally {
                    if (conexion != null) {
                        conexion.disconnect();
                    }
                }
            }
        }).start();
    }

    private void procesarRespuestaXpLogin(int codigoRespuesta, String respuestaTexto) {
        if (codigoRespuesta < 200 || codigoRespuesta >= 300) {
            return;
        }

        try {
            JSONObject json = new JSONObject(respuestaTexto);
            boolean otorgado = json.optBoolean("otorgado", false);

            if (!otorgado) return;

            JSONObject rewardLogin = json.optJSONObject("reward_login");
            JSONObject rewardStreak = json.optJSONObject("reward_streak");
            int xpLogin = rewardLogin != null ? rewardLogin.optInt("amount", 0) : 0;
            int xpStreak = rewardStreak != null ? rewardStreak.optInt("amount", 0) : 0;
            int xpTotal = xpLogin + xpStreak;

            if (xpTotal > 0) {
                String icono = xpStreak > 0 ? "🔥" : "💎";
                Toast.makeText(this,
                        icono + " XP por login: +" + xpTotal,
                        Toast.LENGTH_SHORT).show();
            }

            JSONArray nuevosLogros = json.optJSONArray("nuevosLogros");
            if (nuevosLogros != null) {
                for (int i = 0; i < nuevosLogros.length(); i++) {
                    JSONObject logro = nuevosLogros.optJSONObject(i);
                    if (logro == null) continue;

                    String titulo = logro.optString("titulo", "Logro");
                    String icono = logro.optString("icono", "🏅");
                    int xpOtorgado = logro.optInt("xp_otorgado", 0);

                    String mensaje = "¡Logro desbloqueado! " + icono + " " + titulo;
                    if (xpOtorgado > 0) {
                        mensaje += " (+" + xpOtorgado + " XP)";
                    }

                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
