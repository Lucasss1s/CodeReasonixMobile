package com.example.codereasonix.model;

import org.json.JSONObject;

public class Autor {
    private int idCliente;
    private int idUsuario;
    private String nombre;
    private String email;

    public Autor(int idCliente, int idUsuario, String nombre, String email) {
        this.idCliente = idCliente;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
    }

    public static Autor fromJson(JSONObject clienteObj) {
        if (clienteObj == null) return null;
        int idCliente = clienteObj.optInt("id_cliente", -1);
        int idUsuario = clienteObj.optInt("id_usuario", -1);
        String nombre = null;
        String email = null;

        JSONObject usuarioObj = clienteObj.optJSONObject("usuario");
        if (usuarioObj != null) {
            idUsuario = usuarioObj.optInt("id_usuario", idUsuario);
            nombre = usuarioObj.optString("nombre", null);
            email = usuarioObj.optString("email", null);
        }

        return new Autor(idCliente, idUsuario, nombre, email);
    }

    public int getIdCliente() { return idCliente; }
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
}
