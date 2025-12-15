package com.example.codereasonix;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.codereasonix.adapter.PreguntaAdapter;
import com.example.codereasonix.model.Desafio;
import com.example.codereasonix.model.Pregunta;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DesafioDetalleActivity extends BaseActivity {

    private int idDesafio;
    private TextView txtNombre, txtHp, txtRecompensa;
    private Button btnInscribirme;
    private ProgressBar hpBar;
    private ImageView imgBoss;
    private RecyclerView recyclerPreguntas;

    private boolean desafioCompletado = false;

    private final List<Pregunta> preguntas = new ArrayList<>();
    private PreguntaAdapter adapter;
    private Desafio desafio;

    private final int COLOR_ACCENT   = 0xFF00BFA6;
    private final int COLOR_DISABLED = 0xFF3A3A3A;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desafio_detalle);

        enableImmersiveMode();
        setupTopBar();
        setupBottomNav();

        idDesafio         = getIntent().getIntExtra("id_desafio", -1);
        txtNombre         = findViewById(R.id.txtNombreBoss);
        txtHp             = findViewById(R.id.txtHpBoss);
        txtRecompensa     = findViewById(R.id.txtRecompensa);
        hpBar             = findViewById(R.id.hpBarDetalle);
        imgBoss           = findViewById(R.id.imgBoss);
        recyclerPreguntas = findViewById(R.id.recyclerPreguntas);
        btnInscribirme    = findViewById(R.id.btnInscribirme);

        recyclerPreguntas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreguntaAdapter(preguntas, this);
        recyclerPreguntas.setAdapter(adapter);

        btnInscribirme.setOnClickListener(v -> inscribirse());

        cargarDetalle();
        verificarParticipacionYcargar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarParticipacionYcargar();
    }

    public void refrescarBoss() {
        cargarDetalle();
    }

    private void setEstadoBotonInscripcion(boolean inscripto) {
        if (inscripto) {
            btnInscribirme.setText("Inscripto");
            btnInscribirme.setEnabled(false);
            btnInscribirme.setBackgroundTintList(ColorStateList.valueOf(COLOR_DISABLED));
            btnInscribirme.setTextColor(0xFFFFFFFF);
        } else {
            btnInscribirme.setText("Inscribirme");
            btnInscribirme.setEnabled(true);
            btnInscribirme.setBackgroundTintList(ColorStateList.valueOf(COLOR_ACCENT));
            btnInscribirme.setTextColor(0xFF111111);
        }
    }

    private void mostrarModalDesafioCompletado() {
        runOnUiThread(() -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("¡Desafío completado! 🎉")
                    .setMessage("Has derrotado al boss y finalizado este desafío.")
                    .setCancelable(false)
                    .setPositiveButton("Volver a desafíos", (dialog, which) -> {
                        finish(); 
                    })
                    .show();
        });
    }

    private void cargarDetalle() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(Config.BASE_URL + "/desafios/" + idDesafio);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject obj = new JSONObject(sb.toString());
                desafio = new Desafio(obj);

                runOnUiThread(() -> {
                    txtNombre.setText(desafio.getNombre());
                    txtHp.setText("HP: " + desafio.getHpRestante() + "/" + desafio.getHpTotal());
                    hpBar.setMax(desafio.getHpTotal());
                    hpBar.setProgress(desafio.getHpRestante());
                    txtRecompensa.setText("XP: " + desafio.getRecompensaXp()
                            + " • Monedas: " + desafio.getRecompensaMoneda());

                    if (desafio.getHpRestante() <= 0 && !desafioCompletado) {
                        desafioCompletado = true;
                        mostrarModalDesafioCompletado();
                    }

                    String img = desafio.getImagenUrl();
                    if (img != null && !img.isEmpty()) {
                        Glide.with(this).load(img).into(imgBoss);
                    } else {
                        imgBoss.setImageResource(android.R.color.transparent);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando detalle", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private boolean esDeEsteDesafio(JSONObject p) {
        try {
            Object raw = p.opt("id_desafio");
            if (raw != null && String.valueOf(raw).equals(String.valueOf(idDesafio))) return true;
            JSONObject d = p.optJSONObject("desafio");
            if (d != null) {
                Object inner = d.opt("id_desafio");
                if (inner != null && String.valueOf(inner).equals(String.valueOf(idDesafio))) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    private void verificarParticipacionYcargar() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                int idCliente = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE)
                        .getInt("id_cliente", -1);
                if (idCliente == -1) {
                    runOnUiThread(() -> {
                        setEstadoBotonInscripcion(false);
                        preguntas.clear();
                        adapter.notifyDataSetChanged();
                    });
                    return;
                }

                URL url = new URL(Config.BASE_URL + "/participante-desafio/mis/" + idCliente);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                InputStream in = (con.getResponseCode() >= 200 && con.getResponseCode() < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());
                JSONObject participanteRow = null;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject p = arr.getJSONObject(i);
                    if (esDeEsteDesafio(p)) {
                        participanteRow = p;
                        break;
                    }
                }

                if (participanteRow == null) {
                    runOnUiThread(() -> {
                        setEstadoBotonInscripcion(false);
                        preguntas.clear();
                        adapter.notifyDataSetChanged();
                    });
                } else {

                    if (desafio != null && desafio.getHpRestante() <= 0) {
                        desafioCompletado = true;

                        runOnUiThread(() -> {
                            setEstadoBotonInscripcion(true);
                            preguntas.clear();
                            adapter.notifyDataSetChanged();
                        });

                        mostrarModalDesafioCompletado();
                        return;
                    }

                    final int idPart = participanteRow.optInt(
                            "id_participante",
                            participanteRow.optJSONObject("participante_desafio") != null
                                    ? participanteRow.optJSONObject("participante_desafio")
                                    .optInt("id_participante", -1)
                                    : -1
                    );

                    runOnUiThread(() -> setEstadoBotonInscripcion(true));

                    if (idPart > 0) cargarPreguntasDeParticipante(idPart);
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error verificando participación", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void inscribirse() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                int idCliente = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE)
                        .getInt("id_cliente", -1);
                if (idCliente == -1) {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "Iniciá sesión para inscribirte",
                                    Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                URL url = new URL(Config.BASE_URL + "/participante-desafio");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("id_desafio", idDesafio);
                body.put("id_cliente", idCliente);

                try (OutputStream out = con.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                int code = con.getResponseCode();
                InputStream in = (code >= 200 && code < 300)
                        ? con.getInputStream()
                        : con.getErrorStream();

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    JSONObject resp = new JSONObject(sb.toString());
                    JSONObject participante = resp.optJSONObject("participante");

                    runOnUiThread(() -> setEstadoBotonInscripcion(true));

                    if (participante != null) {
                        int idPart = participante.optInt("id_participante", -1);
                        if (idPart > 0) cargarPreguntasDeParticipante(idPart);
                    } else {
                        verificarParticipacionYcargar();
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this,
                                    "No se pudo inscribir",
                                    Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error al inscribirse",
                                Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    private void cargarPreguntasDeParticipante(int idParticipante) {
        new Thread(() -> {
            HttpURLConnection conPreg = null;
            try {
                URL urlPreg = new URL(
                        Config.BASE_URL + "/participante-pregunta/por-participante/" + idParticipante
                );
                conPreg = (HttpURLConnection) urlPreg.openConnection();
                conPreg.setRequestMethod("GET");

                BufferedReader br2 = new BufferedReader(
                        new InputStreamReader(conPreg.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder sb2 = new StringBuilder();
                String line;
                while ((line = br2.readLine()) != null) sb2.append(line);
                br2.close();

                JSONArray arr = new JSONArray(sb2.toString());
                preguntas.clear();
                for (int i = 0; i < arr.length(); i++) {
                    preguntas.add(new Pregunta(arr.getJSONObject(i)));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Error cargando preguntas",
                                Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (conPreg != null) conPreg.disconnect();
            }
        }).start();
    }
}
