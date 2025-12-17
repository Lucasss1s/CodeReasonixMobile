package com.example.codereasonix.model;

import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;

public class Pregunta {
    private int idParticipantePregunta;
    private String texto;
    private HashMap<String, String> opciones = new HashMap<>();
    private boolean respondida, correcta;
    private String respuestaCorrecta;

    public Pregunta(JSONObject obj) {
        idParticipantePregunta = obj.optInt("id_participante_pregunta");
        JSONObject preg = obj.optJSONObject("pregunta");
        if (preg != null) {
            texto = preg.optString("texto");
            JSONObject ops = preg.optJSONObject("opciones");
            if (ops != null) {
                Iterator<String> keys = ops.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    opciones.put(k, ops.optString(k));
                }
            }
            respuestaCorrecta = preg.optString("correcta", null);
        }
        respondida = obj.optBoolean("respondida");
        correcta = obj.optBoolean("correcta");
    }

    public int getIdParticipantePregunta() { return idParticipantePregunta; }
    public String getTexto() { return texto; }
    public HashMap<String, String> getOpciones() { return opciones; }
    public boolean isRespondida() { return respondida; }
    public boolean isCorrecta() { return correcta; }
    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }
}
