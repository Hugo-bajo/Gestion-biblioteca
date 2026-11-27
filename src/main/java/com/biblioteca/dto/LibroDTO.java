package com.biblioteca.dto;

import java.util.ArrayList;
import java.util.List;

public class LibroDTO {
    private Long idLibro;
    private String titulo;
    private String isbn;
    private Integer anioPublicacion;
    private Long idAutor;
    private List<Long> idCategorias = new ArrayList<>();

    public LibroDTO() {
    }

    public LibroDTO(Long idLibro, String titulo, String isbn, Integer anioPublicacion, Long idAutor) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.isbn = isbn;
        this.anioPublicacion = anioPublicacion;
        this.idAutor = idAutor;
    }

    public Long getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Long idLibro) {
        this.idLibro = idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public Long getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(Long idAutor) {
        this.idAutor = idAutor;
    }

    public List<Long> getIdCategorias() {
        return idCategorias;
    }

    public void setIdCategorias(List<Long> idCategorias) {
        this.idCategorias = idCategorias == null ? new ArrayList<>() : new ArrayList<>(idCategorias);
    }
}