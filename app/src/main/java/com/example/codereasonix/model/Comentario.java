package com.example.codereasonix.model;

import org.json.JSONObject;

public class Comentario {
    private int idComentario;
    private String contenido;
    private String fecha;
    private Autor autor;

    public Comentario(int idComentario, String contenido, String fecha, Autor autor) {
        this.idComentario = idComentario;
        this.contenido = contenido;
        this.fecha = fecha;
        this.autor = autor;
    }

    public static Comentario fromJson(JSONObject obj) {
        int id = obj.optInt("id_comentario", -1);
        String contenido = obj.optString("contenido", "");
        String fecha = obj.optString("fecha", "");
        Autor autor = Autor.fromJson(obj.optJSONObject("cliente"));
        return new Comentario(id, contenido, fecha, autor);
    }

    public int getIdComentario() { return idComentario; }
    public String getContenido() { return contenido; }
    public String getFecha() { return fecha; }
    public Autor getAutor() { return autor; }
}
