package com.example.codereasonix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.codereasonix.model.Desafio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OfertaDetalleActivity extends AppCompatActivity {

    private int idOferta;

    private TextView txtTitulo, txtEmpresa, txtSector, txtUbicacion,
            txtDescripcion, txtRequisitos, txtFecha;
    private Button btnPostularme;
    private boolean yaPostulado = false;

    private final int COLOR_ACCENT   = 0xFF00BFA6;
    private final int COLOR_DISABLED = 0xFF424242;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oferta_detalle);

        idOferta = getIntent().getIntExtra("id_oferta", -1);

        txtTitulo      = findViewById(R.id.txtTituloDetalle);
        txtEmpresa     = findViewById(R.id.txtEmpresaDetalle);
        txtSector      = findViewById(R.id.txtSectorDetalle);
        txtUbicacion   = findViewById(R.id.txtUbicacionDetalle);
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle);
        txtRequisitos  = findViewById(R.id.txtRequisitosDetalle);
        txtFecha       = findViewById(R.id.txtFechaDetalle);
        btnPostularme  = findViewById(R.id.btnPostularme);

        btnPostularme.setOnClickListener(v -> {
            if (yaPostulado) {
                Toast.makeText(this, "Ya te postulaste a esta oferta", Toast.LENGTH_SHORT).show();
            } else {
                postularme();
            }
        });

        cargarOferta();
        verificarSiYaEstaPostulado();
    }

    private void setEstadoBoton(boolean postulado, boolean loggedInOk) {
        yaPostulado = postulado;

        if (!loggedInOk) {
            btnPostularme.setEnabled(false);
            btnPostularme.setText("Iniciá sesión para postularte");
            btnPostularme.setBackgroundTintList(ColorStateList.valueOf(COLOR_DISABLED));
            btnPostularme.setTextColor(0xFFFFFFFF);
            return;
        }

        if (postulado) {
            btnPostularme.setEnabled(false);
            btnPostularme.setText("Ya te postulaste");
            btnPostularme.setBackgroundTintList(ColorStateList.valueOf(COLOR_DISABLED));
            btnPostularme.setTextColor(0xFFFFFFFF);
        } else {
            btnPostularme.setEnabled(true);
            btnPostularme.setText("Postularme");
            btnPostularme.setBackgroundTintList(ColorStateList.valueOf(COLOR_ACCENT));
            btnPostularme.setTextColor(0xFF111111);
        }
    }

    private void cargarOferta() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(Config.BASE_URL + "/ofertas/" + idOferta);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int code = con.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    JSONObject obj = new JSONObject(sb.toString());

                    String titulo      = obj.optString("titulo", "");
                    String descripcion = obj.optString("descripcion", "");
                    String ubicacion   = obj.optString("ubicacion", "");
                    String requisitos  = obj.optString("requisitos", "");
                    String fecha       = obj.optString("fecha_publicacion", "");

                    JSONObject emp = obj.optJSONObject("empresa");
                    String nombreEmpresa = "";
                    String sector = "";
                    if (emp != null) {
                        nombreEmpresa = emp.optString("nombre", "");
                        sector        = emp.optString("sector", "");
                    }

                    final String fTitulo   = titulo;
                    final String fDesc     = descripcion;
                    final String fUbic     = ubicacion;
                    final String fReq      = requisitos;
                    final String fFecha    = fecha;
                    final String fEmpresa  = nombreEmpresa;
                    final String fSector   = sector;

                    runOnUiThread(() -> {
                        txtTitulo.setText(fTitulo);
                        txtEmpresa.setText(fEmpresa);
                        txtSector.setText(fSector.isEmpty() ? "Sin sector" : fSector);
                        txtUbicacion.setText(fUbic.isEmpty() ? "Sin ubicación" : fUbic);
                        txtDescripcion.setText(fDesc);
                        txtRequisitos.setText(fReq.isEmpty() ? "No especificados" : fReq);
                        txtFecha.setText(fFecha);
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error cargando oferta", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando oferta", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void verificarSiYaEstaPostulado() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                int idCliente = prefs.getInt("id_cliente", -1);
                if (idCliente == -1) {
                    runOnUiThread(() -> setEstadoBoton(false, false));
                    return;
                }

                URL url = new URL(Config.BASE_URL + "/postulaciones/mias/" + idCliente);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                int code = con.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    JSONArray arr = new JSONArray(sb.toString());
                    boolean encontrado = false;

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        int idOfertaRow = obj.optInt("id_oferta", -1);
                        if (idOfertaRow == -1) {
                            JSONObject ofertaObj = obj.optJSONObject("oferta");
                            if (ofertaObj != null) {
                                idOfertaRow = ofertaObj.optInt("id_oferta", -1);
                            }
                        }

                        if (idOfertaRow == idOferta) {
                            encontrado = true;
                            break;
                        }
                    }

                    final boolean fEncontrado = encontrado;
                    runOnUiThread(() -> setEstadoBoton(fEncontrado, true));
                } else {
                    runOnUiThread(() -> setEstadoBoton(false, true));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> setEstadoBoton(false, true));
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void postularme() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
                int idCliente = prefs.getInt("id_cliente", -1);
                if (idCliente == -1) {
                    runOnUiThread(() -> Toast.makeText(this, "Iniciá sesión para postularte", Toast.LENGTH_SHORT).show());
                    return;
                }

                URL url = new URL(Config.BASE_URL + "/postulaciones");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("id_oferta", idOferta);
                body.put("id_cliente", idCliente);

                try (OutputStream out = con.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                int code = con.getResponseCode();
                InputStream is = (code >= 200 && code < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Postulación enviada", Toast.LENGTH_SHORT).show();
                        setEstadoBoton(true, true);
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se pudo postular", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al postularse", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarSiYaEstaPostulado();
    }
}
