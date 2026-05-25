package com.example.proyectotfg.model;

public class Usuario {
    public String nombre;
    public String email;
    public double peso;
    public int altura;
    public int edad;
    public String objetivo;
    public String nivelActividad;
    public String rol;

    public Usuario() {}

    public Usuario(String nombre, String email, double peso, int altura, int edad, String objetivo, String nivelActividad) {
        this.nombre = nombre;
        this.email = email;
        this.peso = peso;
        this.altura = altura;
        this.edad = edad;
        this.objetivo = objetivo;
        this.nivelActividad = nivelActividad;
        this.rol = "usuario";
    }
}