package com.example.codereasonix.model;

import org.json.JSONObject;

public class Participante {
    private int idParticipante;
    private int idDesafio;
    private int idCliente;
    private boolean recibioRecompensa;

    public Participante(JSONObject obj) {
        idParticipante = obj.optInt("id_participante");
        idDesafio = obj.optInt("id_desafio");
        idCliente = obj.optInt("id_cliente");
        recibioRecompensa = obj.optBoolean("recibio_recompensa");
    }

    public int getIdParticipante() { return idParticipante; }
    public int getIdDesafio() { return idDesafio; }
    public int getIdCliente() { return idCliente; }
    public boolean isRecibioRecompensa() { return recibioRecompensa; }
}
