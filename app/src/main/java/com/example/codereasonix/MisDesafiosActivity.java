package com.example.codereasonix;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MisDesafiosActivity extends BaseActivity {

    private RecyclerView recycler;
    private final List<Participacion> lista = new ArrayList<>();
    private MiDesafioAdapter adapter;
    private int idCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_desafios);

        enableImmersiveMode();

        setupTopBar();
        setupBottomNav();

        idCliente = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE)
                .getInt("id_cliente", -1);

        recycler = findViewById(R.id.recyclerMisDesafios);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MiDesafioAdapter(lista);
        recycler.setAdapter(adapter);

        cargarMisDesafios();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarMisDesafios();
    }

    private void cargarMisDesafios() {
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                URL url = new URL(Config.BASE_URL + "/participante-desafio/mis/" + idCliente);
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONArray arr = new JSONArray(sb.toString());
                lista.clear();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject row = arr.getJSONObject(i);
                    Participacion p = Participacion.fromJson(row);
                    if (p != null) lista.add(p);
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error cargando mis desafíos", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }

    static class Participacion {
        int idParticipante;
        int idDesafio;
        String nombre;
        String estado;
        int recompensaXp;
        int recompensaMoneda;
        boolean recibioRecompensa;
        int danoTotal;
        int aciertos;
        String imagenUrl;
        String dificultad;
        String lenguaje;

        static Participacion fromJson(JSONObject row) {
            try {
                Participacion p = new Participacion();
                p.idParticipante = row.optInt("id_participante");
                p.recibioRecompensa = row.optBoolean("recibio_recompensa", false);
                p.danoTotal = row.optInt("dano_total", 0);
                p.aciertos = row.optInt("aciertos", 0);

                JSONObject d = row.optJSONObject("desafio");
                if (d == null) return null;
                p.idDesafio = d.optInt("id_desafio", -1);
                p.nombre = d.optString("nombre", "Desafío");
                p.estado = d.optString("estado", "activo");
                p.recompensaXp = d.optInt("recompensa_xp", 0);
                p.recompensaMoneda = d.optInt("recompensa_moneda", 0);
                p.imagenUrl = d.optString("imagen_url", "");
                p.dificultad = d.optString("dificultad", null);
                p.lenguaje   = d.optString("lenguaje", null);

                return p;
            } catch (Exception e) {
                return null;
            }
        }
    }

    class MiDesafioAdapter extends RecyclerView.Adapter<MiDesafioAdapter.VH> {
        private final List<Participacion> data;

        MiDesafioAdapter(List<Participacion> data) {
            this.data = data;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView imgThumb;
            TextView txtTitulo, txtStats, txtRecompensa, txtEstado;
            TextView txtDificultadMi, txtLenguajeMi;
            Button btnAccion;
            ProgressBar claimingBar;

            VH(View v) {
                super(v);
                imgThumb      = v.findViewById(R.id.imgThumb);
                txtTitulo     = v.findViewById(R.id.txtTituloMi);
                txtStats      = v.findViewById(R.id.txtStats);
                txtRecompensa = v.findViewById(R.id.txtRecompensaMi);
                txtEstado     = v.findViewById(R.id.txtEstadoMi);
                btnAccion     = v.findViewById(R.id.btnAccionMi);
                claimingBar   = v.findViewById(R.id.claimingBar);

                txtDificultadMi = v.findViewById(R.id.txtDificultadMi);
                txtLenguajeMi   = v.findViewById(R.id.txtLenguajeMi);
            }
        }

        private String formatDificultad(String dif) {
            if (dif == null || dif.isEmpty()) return "Sin dificultad";
            switch (dif.toLowerCase()) {
                case "facil": return "Fácil";
                case "intermedio": return "Intermedio";
                case "dificil": return "Difícil";
                case "experto": return "Experto";
                default: return dif;
            }
        }

        private String formatLenguaje(String lang) {
            if (lang == null || lang.isEmpty()) return "Sin lenguaje";
            switch (lang.toLowerCase()) {
                case "java": return "Java";
                case "python": return "Python";
                case "javascript": return "JavaScript";
                case "php": return "PHP";
                default: return lang;
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_mi_desafio, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int position) {
            Participacion p = data.get(position);

            if (p.imagenUrl != null && !p.imagenUrl.isEmpty()) {
                Glide.with(MisDesafiosActivity.this)
                        .load(p.imagenUrl)
                        .into(h.imgThumb);
            } else {
                h.imgThumb.setImageResource(android.R.color.transparent);
            }

            h.txtTitulo.setText(p.nombre);
            h.txtStats.setText("Daño: " + p.danoTotal + "   •   Aciertos: " + p.aciertos);

            int verde = android.graphics.Color.parseColor("#00BFA6");
            if (h.txtDificultadMi != null) {
                h.txtDificultadMi.setText(formatDificultad(p.dificultad));
                h.txtDificultadMi.setTextColor(verde);
            }
            if (h.txtLenguajeMi != null) {
                h.txtLenguajeMi.setText(formatLenguaje(p.lenguaje));
                h.txtLenguajeMi.setTextColor(verde);
            }

            h.txtRecompensa.setText(
                    "Recompensa: " + p.recompensaXp + " XP • " + p.recompensaMoneda + " monedas"
            );
            h.txtEstado.setText("Estado: " + p.estado);

            h.btnAccion.setEnabled(true);
            h.claimingBar.setVisibility(View.GONE);

            if ("activo".equalsIgnoreCase(p.estado)) {
                h.btnAccion.setText("Ir al desafío");
                h.btnAccion.setOnClickListener(v -> {
                    int pos = h.getAdapterPosition();
                    if (pos == RecyclerView.NO_POSITION) return;
                    Participacion actual = data.get(pos);
                    Intent i = new Intent(MisDesafiosActivity.this, DesafioDetalleActivity.class);
                    i.putExtra("id_desafio", actual.idDesafio);
                    startActivity(i);
                });

            } else {
                if (p.recibioRecompensa) {
                    h.btnAccion.setText("✅ Reclamado");
                    h.btnAccion.setEnabled(false);
                    h.btnAccion.setOnClickListener(null);
                } else {
                    h.btnAccion.setText("Reclamar recompensa");
                    h.btnAccion.setOnClickListener(v -> {
                        int pos = h.getAdapterPosition();
                        if (pos == RecyclerView.NO_POSITION) return;
                        reclamar(pos);
                    });
                }
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        private void reclamar(int adapterPos) {
            if (adapterPos == RecyclerView.NO_POSITION) return;
            Participacion p = data.get(adapterPos);

            notifyItemChanged(adapterPos, "loading");

            new Thread(() -> {
                HttpURLConnection con = null;
                int code = -1;
                String message = "Recompensa reclamada";
                Integer xp = null, monedas = null;
                try {
                    URL url = new URL(Config.BASE_URL + "/participante-desafio/" + p.idParticipante + "/claim");
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);

                    try (OutputStream out = con.getOutputStream()) {
                        out.write("{}".getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }

                    code = con.getResponseCode();
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream(),
                            StandardCharsets.UTF_8
                    ));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    br.close();

                    JSONObject resp = new JSONObject(sb.toString());
                    message = resp.optString("message", message);
                    xp = resp.optInt("xp", 0);
                    monedas = resp.optInt("monedas", 0);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) con.disconnect();
                }

                int finalCode = code;
                String finalMessage = message;
                Integer finalXp = xp, finalMonedas = monedas;

                runOnUiThread(() -> {
                    notifyItemChanged(adapterPos, "stop_loading");

                    if (finalCode >= 200 && finalCode < 300) {
                        Participacion px = data.get(adapterPos);
                        px.recibioRecompensa = true;
                        notifyItemChanged(adapterPos);

                        String extra = "";
                        if (finalXp != null && finalXp > 0)
                            extra += finalXp + " XP";
                        if (finalMonedas != null && finalMonedas > 0)
                            extra += (extra.isEmpty() ? "" : " • ") + finalMonedas + " monedas";

                        String toastMsg;

                        if ((finalXp == null || finalXp == 0) && (finalMonedas == null || finalMonedas == 0)) {
                            toastMsg = "❌ No recibiste recompensas porque no participaste en el desafío";
                        } else if (finalXp < p.recompensaXp || finalMonedas < p.recompensaMoneda) {
                            toastMsg = "⚠️ Recompensa parcial obtenida (" + extra + ")";
                        } else {
                            toastMsg = "🎉 Recompensa reclamada con éxito (" + extra + ")";
                        }
                        Toast.makeText(
                                MisDesafiosActivity.this,
                                toastMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
            }).start();
        }

        @Override
        public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
            if (!payloads.isEmpty()) {
                Object payload = payloads.get(0);
                if ("loading".equals(payload)) {
                    holder.btnAccion.setEnabled(false);
                    holder.claimingBar.setVisibility(View.VISIBLE);
                    return;
                } else if ("stop_loading".equals(payload)) {
                    holder.btnAccion.setEnabled(true);
                    holder.claimingBar.setVisibility(View.GONE);
                    return;
                }
            }
            super.onBindViewHolder(holder, position, payloads);
        }
    }
}
