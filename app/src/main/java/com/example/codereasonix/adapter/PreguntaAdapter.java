package com.example.codereasonix.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codereasonix.Config;
import com.example.codereasonix.R;
import com.example.codereasonix.model.Pregunta;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreguntaAdapter extends RecyclerView.Adapter<PreguntaAdapter.ViewHolder> {

    private final List<Pregunta> lista;
    private final AppCompatActivity activity;

    private int expandedPosition = -1;
    private final int COLOR_DISABLED = 0xFF3A3A3A;

    public PreguntaAdapter(List<Pregunta> lista, AppCompatActivity activity) {
        this.lista = lista != null ? lista : new ArrayList<>();
        this.activity = activity;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPregunta, badgeEstado, txtSubtitulo, txtRespuestaCorrecta;
        View expandArea;
        RadioGroup radioGroupOpciones;
        Button btnEnviar;

        public ViewHolder(@NonNull View v) {
            super(v);
            txtPregunta = v.findViewById(R.id.txtPregunta);
            badgeEstado = v.findViewById(R.id.badgeEstado);
            expandArea = v.findViewById(R.id.expandArea);
            txtSubtitulo = v.findViewById(R.id.txtSubtitulo);
            radioGroupOpciones = v.findViewById(R.id.radioGroupOpciones);
            btnEnviar = v.findViewById(R.id.btnEnviar);

            txtRespuestaCorrecta = new TextView(v.getContext());
            txtRespuestaCorrecta.setTextColor(0xFF22C55E);
            txtRespuestaCorrecta.setPadding(8, 16, 8, 0);
            txtRespuestaCorrecta.setVisibility(View.GONE);

            ((ViewGroup) expandArea).addView(txtRespuestaCorrecta);
        }
    }

    @NonNull
    @Override
    public PreguntaAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pregunta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PreguntaAdapter.ViewHolder holder, int position) {
        final Pregunta p = lista.get(position);

        holder.txtPregunta.setText(p.getTexto());

        if (p.isRespondida()) {
            if (p.isCorrecta()) {
                holder.badgeEstado.setText("✅ Correcta");
                holder.badgeEstado.setTextColor(0xFF16A34A);
            } else {
                holder.badgeEstado.setText("❌ Incorrecta");
                holder.badgeEstado.setTextColor(0xFFEF4444);
            }
        } else {
            holder.badgeEstado.setText("Sin responder");
            holder.badgeEstado.setTextColor(0xFFFFD166);
        }

        boolean isExpanded =
                expandedPosition == position &&
                        (!p.isRespondida() || !p.isCorrecta());

        holder.expandArea.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.txtSubtitulo.setText("Marcá la respuesta correcta");

        holder.radioGroupOpciones.removeAllViews();
        holder.txtRespuestaCorrecta.setVisibility(View.GONE);

        if (!p.isRespondida() || !p.isCorrecta()) {

            List<String> claves = new ArrayList<>(p.getOpciones().keySet());
            Collections.sort(claves);

            for (String key : claves) {
                String texto = p.getOpciones().get(key);
                RadioButton rb = new RadioButton(holder.itemView.getContext());
                rb.setText(key + ". " + texto);
                rb.setTag(key);
                rb.setTextColor(0xFFEFEFEF);
                rb.setEnabled(!p.isRespondida());

                holder.radioGroupOpciones.addView(rb);
            }
        }

        if (p.isRespondida() && !p.isCorrecta()) {
            String keyCorrecta = p.getRespuestaCorrecta();
            String textoCorrecto = p.getOpciones().get(keyCorrecta);

            holder.txtRespuestaCorrecta.setText(
                    "✔ Respuesta correcta: " + keyCorrecta + ". " + textoCorrecto
            );
            holder.txtRespuestaCorrecta.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Pregunta actual = lista.get(adapterPos);

            if (actual.isRespondida() && actual.isCorrecta()) return;

            int old = expandedPosition;
            expandedPosition = expandedPosition == adapterPos ? -1 : adapterPos;

            notifyItemChanged(adapterPos);
            if (old != -1 && old != adapterPos) notifyItemChanged(old);
        });

        if (p.isRespondida()) {
            holder.btnEnviar.setText("Respuesta enviada");
            holder.btnEnviar.setEnabled(false);
            holder.btnEnviar.setBackgroundTintList(ColorStateList.valueOf(COLOR_DISABLED));
            holder.btnEnviar.setTextColor(0xFFFFFFFF);
        } else {
            holder.btnEnviar.setText("Enviar");
            holder.btnEnviar.setEnabled(true);
        }

        holder.btnEnviar.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;

            Pregunta actual = lista.get(adapterPos);
            if (actual.isRespondida()) return;

            int checkedId = holder.radioGroupOpciones.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(activity, "Seleccioná una opción", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton rb = holder.itemView.findViewById(checkedId);
            String respuesta = (String) rb.getTag();

            holder.btnEnviar.setEnabled(false);

            new Thread(() -> {
                int code = -1;
                boolean esCorrecta = false;

                try {
                    URL url = new URL(Config.BASE_URL + "/participante-pregunta/" + actual.getIdParticipantePregunta() + "/respond");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    con.setDoOutput(true);

                    JSONObject body = new JSONObject();
                    body.put("respuesta", respuesta);

                    try (OutputStream out = con.getOutputStream()) {
                        out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                        out.flush();
                    }

                    code = con.getResponseCode();

                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream(),
                                    StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }

                    if (sb.length() > 0) {
                        JSONObject resp = new JSONObject(sb.toString());
                        esCorrecta = resp.optBoolean("correcta", false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean ok = code >= 200 && code < 300;
                boolean finalCorrecta = esCorrecta;

                activity.runOnUiThread(() -> {
                    holder.btnEnviar.setEnabled(true);

                    if (ok) {
                        try {
                            Field fResp = Pregunta.class.getDeclaredField("respondida");
                            fResp.setAccessible(true);
                            fResp.set(actual, true);

                            Field fCorr = Pregunta.class.getDeclaredField("correcta");
                            fCorr.setAccessible(true);
                            fCorr.set(actual, finalCorrecta);
                        } catch (Exception ignored) {}

                        expandedPosition = -1;
                        notifyItemChanged(holder.getAdapterPosition());

                        Toast.makeText(
                                activity,
                                finalCorrecta ? "¡Correcta! ✅" : "Respuesta incorrecta ❌",
                                Toast.LENGTH_SHORT
                        ).show();

                        if (activity instanceof com.example.codereasonix.DesafioDetalleActivity) {
                            ((com.example.codereasonix.DesafioDetalleActivity) activity).refrescarBoss();
                        }
                    }
                });
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
}
