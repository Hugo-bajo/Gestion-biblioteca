package com.biblioteca.service;

import com.biblioteca.dao.AutorDao;
import com.biblioteca.dao.LibroDao;
import com.biblioteca.dto.AutorDTO;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.ValidationException;

import java.util.List;
import java.util.Objects;

public class AutorService {
    private final AutorDao autorDao;
    private final LibroDao libroDao;

    public AutorService(AutorDao autorDao, LibroDao libroDao) {
        this.autorDao = Objects.requireNonNull(autorDao);
        this.libroDao = Objects.requireNonNull(libroDao);
    }

    public AutorDTO create(AutorDTO autor) {
        validateAutor(autor);
        return autorDao.create(autor);
    }

    public AutorDTO findById(Long idAutor) {
        validateId(idAutor);
        return autorDao.findById(idAutor);
    }

    public List<AutorDTO> findAll() {
        return autorDao.findAll();
    }

    public AutorDTO update(AutorDTO autor) {
        validateAutor(autor);
        validateId(autor.getIdAutor());
        return autorDao.update(autor);
    }

    public void delete(Long idAutor) {
        validateId(idAutor);
        List<LibroDTO> libros = libroDao.findAll();
        if (libros.stream().anyMatch(libro -> Objects.equals(libro.getIdAutor(), idAutor))) {
            throw new ValidationException("No se puede borrar un autor que tiene libros");
        }
        autorDao.delete(idAutor);
    }

    private void validateAutor(AutorDTO autor) {
        if (autor == null || isBlank(autor.getNombre()) || isBlank(autor.getApellido())) {
            throw new ValidationException("El nombre y el apellido del autor son obligatorios");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("El id del autor debe ser positivo");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}