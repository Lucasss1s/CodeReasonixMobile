package com.example.codereasonix.model;

import org.json.JSONObject;

public class OfertaLaboral {
    private int idOferta;
    private int idEmpresa;
    private String titulo;
    private String descripcion;
    private String ubicacion;
    private String requisitos;
    private String fechaPublicacion;
    private Empresa empresa;

    public OfertaLaboral(JSONObject obj) {
        idOferta         = obj.optInt("id_oferta");
        idEmpresa        = obj.optInt("id_empresa");
        titulo           = obj.optString("titulo", "");
        descripcion      = obj.optString("descripcion", "");
        ubicacion        = obj.optString("ubicacion", "");
        requisitos       = obj.optString("requisitos", "");
        fechaPublicacion = obj.optString("fecha_publicacion", "");

        JSONObject empresaObj = obj.optJSONObject("empresa");
        if (empresaObj != null) {
            empresa = new Empresa(empresaObj);
        }
    }

    public int getIdOferta() { return idOferta; }
    public int getIdEmpresa() { return idEmpresa; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getUbicacion() { return ubicacion; }
    public String getRequisitos() { return requisitos; }
    public String getFechaPublicacion() { return fechaPublicacion; }
    public Empresa getEmpresa() { return empresa; }
}
