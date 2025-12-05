package com.example.codereasonix.model;

import org.json.JSONObject;

public class Reaccion {
    private int idReaccion;
    private String tipo;
    private String fecha;
    private Autor autor;

    public Reaccion(int idReaccion, String tipo, String fecha, Autor autor) {
        this.idReaccion = idReaccion;
        this.tipo = tipo;
        this.fecha = fecha;
        this.autor = autor;
    }

    public static Reaccion fromJson(JSONObject obj) {
        int id = obj.optInt("id_reaccion", -1);
        String tipo = obj.optString("tipo", "");
        String fecha = obj.optString("fecha", "");
        Autor autor = Autor.fromJson(obj.optJSONObject("cliente"));
        return new Reaccion(id, tipo, fecha, autor);
    }

    public int getIdReaccion() { return idReaccion; }
    public String getTipo() { return tipo; }
    public String getFecha() { return fecha; }
    public Autor getAutor() { return autor; }
}
