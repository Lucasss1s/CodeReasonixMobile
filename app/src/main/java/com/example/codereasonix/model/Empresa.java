package com.example.codereasonix.model;

import org.json.JSONObject;

public class Empresa {
    private int idEmpresa;
    private String nombre;
    private String descripcion;
    private String sector;

    public Empresa(JSONObject obj) {
        if (obj == null) return;
        idEmpresa   = obj.optInt("id_empresa");
        nombre      = obj.optString("nombre", "");
        descripcion = obj.optString("descripcion", null);
        sector      = obj.optString("sector", null);
    }

    public int getIdEmpresa() { return idEmpresa; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getSector() { return sector; }
}
