package com.example.codereasonix;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.codereasonix.model.Pregunta;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PreguntaDialog extends Dialog {

    private final Pregunta pregunta;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean sending = false;

    public PreguntaDialog(Context context, Pregunta pregunta) {
        super(context);
        this.pregunta = pregunta;
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pregunta);

        TextView txtPregunta = findViewById(R.id.txtPreguntaDialog);
        RadioGroup opciones = findViewById(R.id.radioGroupOpciones);
        Button btnEnviar = findViewById(R.id.btnEnviarRespuesta);

        txtPregunta.setText(pregunta.getTexto());

        for (String key : pregunta.getOpciones().keySet()) {
            String textoOpt = pregunta.getOpciones().get(key);
            RadioButton rb = new RadioButton(getContext());
            rb.setText(key + ". " + (textoOpt != null ? textoOpt : ""));
            rb.setTag(key);
            opciones.addView(rb);
        }

        if (pregunta.isRespondida()) {
            for (int i = 0; i < opciones.getChildCount(); i++) {
                opciones.getChildAt(i).setEnabled(false);
            }
            btnEnviar.setEnabled(false);
        }

        btnEnviar.setOnClickListener(v -> {
            if (sending) return;

            int selectedId = opciones.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Seleccioná una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton rb = findViewById(selectedId);
            String respuesta = (String) rb.getTag();
            enviarRespuesta(respuesta, btnEnviar, opciones);
        });
    }

    private void enviarRespuesta(String respuesta, Button btnEnviar, RadioGroup opciones) {
        sending = true;
        btnEnviar.setEnabled(false);

        new Thread(() -> {
            HttpURLConnection con = null;
            int responseCode = -1;
            try {
                URL url = new URL(Config.BASE_URL + "/participante-pregunta/" + pregunta.getIdParticipantePregunta() + "/respond");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("respuesta", respuesta);

                try (OutputStream out = con.getOutputStream()) {
                    out.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }

                responseCode = con.getResponseCode();

                InputStream is = (responseCode >= 200 && responseCode < 300) ? con.getInputStream() : con.getErrorStream();
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (con != null) con.disconnect();
            }

            final int finalCode = responseCode;
            mainHandler.post(() -> {
                sending = false;

                if (finalCode >= 200 && finalCode < 300) {
                    Toast.makeText(getContext(), "Respuesta enviada ✅", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < opciones.getChildCount(); i++) {
                        opciones.getChildAt(i).setEnabled(false);
                    }
                    btnEnviar.setEnabled(false);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al enviar", Toast.LENGTH_SHORT).show();
                    btnEnviar.setEnabled(true);
                }
            });
        }).start();
    }
}
