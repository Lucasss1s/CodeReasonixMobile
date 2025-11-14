package com.example.codereasonix.model;

import org.json.JSONObject;

public class Desafio {
    private int idDesafio;
    private String nombre, descripcion, imagenUrl, estado;
    private int hpTotal, hpRestante, recompensaXp, recompensaMoneda;
    private String dificultad;
    private String lenguaje;

    public Desafio(JSONObject obj) {
        idDesafio = obj.optInt("id_desafio");
        nombre = obj.optString("nombre");
        descripcion = obj.optString("descripcion");
        imagenUrl = obj.optString("imagen_url");
        estado = obj.optString("estado");
        hpTotal = obj.optInt("hp_total");
        hpRestante = obj.isNull("hp_restante") ? hpTotal : obj.optInt("hp_restante", hpTotal);
        recompensaXp = obj.optInt("recompensa_xp");
        recompensaMoneda = obj.optInt("recompensa_moneda");
        dificultad = obj.optString("dificultad", null);
        lenguaje   = obj.optString("lenguaje", null);
    }

    public int getIdDesafio() { return idDesafio; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getImagenUrl() { return imagenUrl; }
    public int getHpTotal() { return hpTotal; }
    public int getHpRestante() { return hpRestante; }
    public String getEstado() { return estado; }
    public int getRecompensaXp() { return recompensaXp; }
    public int getRecompensaMoneda() { return recompensaMoneda; }
    public String getDificultad() { return dificultad; }
    public String getLenguaje()   { return lenguaje; }
}
