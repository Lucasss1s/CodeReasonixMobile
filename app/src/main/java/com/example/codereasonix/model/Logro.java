package com.example.codereasonix.model;

public class Logro {
    private int idLogro;
    private String icono;
    private String titulo;
    private String descripcion;
    private int xpOtorgado;
    private boolean desbloqueado;

    public Logro(int idLogro, String icono, String titulo, String descripcion, int xpOtorgado, boolean desbloqueado) {
        this.idLogro = idLogro;
        this.icono = icono;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.xpOtorgado = xpOtorgado;
        this.desbloqueado = desbloqueado;
    }

    public int getIdLogro() { return idLogro; }
    public String getIcono() { return icono; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public int getXpOtorgado() { return xpOtorgado; }
    public boolean isDesbloqueado() { return desbloqueado; }
}
