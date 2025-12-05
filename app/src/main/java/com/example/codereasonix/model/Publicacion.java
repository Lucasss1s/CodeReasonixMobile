package com.example.codereasonix.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Publicacion {
    private int idPublicacion;
    private String contenido;
    private String imagenUrl;
    private String fecha;
    private Autor autor;
    private List<Comentario> comentarios;
    private List<Reaccion> reacciones;

    public Publicacion(int idPublicacion, String contenido, String imagenUrl, String fecha,
                       Autor autor, List<Comentario> comentarios, List<Reaccion> reacciones) {
        this.idPublicacion = idPublicacion;
        this.contenido = contenido;
        this.imagenUrl = imagenUrl;
        this.fecha = fecha;
        this.autor = autor;
        this.comentarios = comentarios;
        this.reacciones = reacciones;
    }

    public static Publicacion fromJson(JSONObject obj) {
        int id = obj.optInt("id_publicacion", -1);
        String contenido = obj.optString("contenido", "");
        String imagenUrl = obj.optString("imagen_url", null);
        String fecha = obj.optString("fecha", "");
        Autor autor = Autor.fromJson(obj.optJSONObject("cliente"));

        List<Comentario> comentarios = new ArrayList<>();
        JSONArray arrCom = obj.optJSONArray("comentarios");
        if (arrCom != null) {
            for (int i = 0; i < arrCom.length(); i++) {
                JSONObject cObj = arrCom.optJSONObject(i);
                if (cObj != null) comentarios.add(Comentario.fromJson(cObj));
            }
        }

        List<Reaccion> reacciones = new ArrayList<>();
        JSONArray arrReac = obj.optJSONArray("reacciones");
        if (arrReac != null) {
            for (int i = 0; i < arrReac.length(); i++) {
                JSONObject rObj = arrReac.optJSONObject(i);
                if (rObj != null) reacciones.add(Reaccion.fromJson(rObj));
            }
        }

        return new Publicacion(id, contenido, imagenUrl, fecha, autor, comentarios, reacciones);
    }

    public int getIdPublicacion() { return idPublicacion; }
    public String getContenido() { return contenido; }
    public String getImagenUrl() { return imagenUrl; }
    public String getFecha() { return fecha; }
    public Autor getAutor() { return autor; }
    public List<Comentario> getComentarios() { return comentarios; }
    public List<Reaccion> getReacciones() { return reacciones; }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public void setReacciones(List<Reaccion> reacciones) {
        this.reacciones = reacciones;
    }
}
