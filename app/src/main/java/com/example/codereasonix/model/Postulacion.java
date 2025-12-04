package com.example.codereasonix.model;

import org.json.JSONObject;

public class Postulacion {
    private int idPostulacion;
    private int idOferta;
    private int idCliente;
    private String fecha;
    private String estado;
    private String tituloOferta;

    public Postulacion(JSONObject obj) {
        idPostulacion = obj.optInt("id_postulacion");
        idOferta      = obj.optInt("id_oferta");
        idCliente     = obj.optInt("id_cliente");
        fecha         = obj.optString("fecha", null);
        estado        = obj.optString("estado", "pendiente");

        JSONObject ofertaObj = obj.optJSONObject("oferta");
        if (ofertaObj != null) {
            tituloOferta = ofertaObj.optString("titulo", null);
        }
    }

    public int getIdPostulacion() { return idPostulacion; }
    public int getIdOferta()      { return idOferta; }
    public int getIdCliente()     { return idCliente; }
    public String getEstado()     { return estado; }
    public String getTituloOferta() { return tituloOferta; }
    public String getFecha()      { return fecha; }

    public String getEstadoLegible() {
        if (estado == null) return "Pendiente";
        switch (estado) {
            case "aceptada":    return "Aceptada";
            case "rechazada":   return "Rechazada";
            case "en_revision": return "En revisión";
            default:            return "Pendiente";
        }
    }

    public String getFechaFormateada() {
        if (fecha == null || fecha.isEmpty()) return "";
        if (fecha.length() >= 10) {
            return fecha.substring(0, 10);
        }
        return fecha;
    }
}
