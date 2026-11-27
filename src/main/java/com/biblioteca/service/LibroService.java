package com.biblioteca.service;

import com.biblioteca.dao.AutorDao;
import com.biblioteca.dao.CategoriaDao;
import com.biblioteca.dao.LibroDao;
import com.biblioteca.dto.LibroDTO;
import com.biblioteca.exception.ValidationException;

import java.time.Year;
import java.util.List;
import java.util.Objects;

public class LibroService {
    private final LibroDao libroDao;
    private final AutorDao autorDao;
    private final CategoriaDao categoriaDao;

    public LibroService(LibroDao libroDao, AutorDao autorDao, CategoriaDao categoriaDao) {
        this.libroDao = Objects.requireNonNull(libroDao);
        this.autorDao = Objects.requireNonNull(autorDao);
        this.categoriaDao = Objects.requireNonNull(categoriaDao);
    }

    public LibroDTO create(LibroDTO libro) {
        validateLibro(libro);
        validateReferences(libro);
        return libroDao.create(libro);
    }

    public LibroDTO findById(Long idLibro) {
        validateId(idLibro);
        return libroDao.findById(idLibro);
    }

    public List<LibroDTO> findAll() {
        return libroDao.findAll();
    }

    public LibroDTO update(LibroDTO libro) {
        validateLibro(libro);
        validateId(libro.getIdLibro());
        validateReferences(libro);
        return libroDao.update(libro);
    }

    public void delete(Long idLibro) {
        validateId(idLibro);
        libroDao.delete(idLibro);
    }

    private void validateLibro(LibroDTO libro) {
        if (libro == null || isBlank(libro.getTitulo())) {
            throw new ValidationException("El título del libro es obligatorio");
        }
        if (!isValidIsbn(libro.getIsbn())) {
            throw new ValidationException("El ISBN no tiene un formato válido");
        }
        Integer anio = libro.getAnioPublicacion();
        if (anio == null || anio < 1 || anio > Year.now().getValue()) {
            throw new ValidationException("El año de publicación no es válido");
        }
        if (libro.getIdAutor() == null || libro.getIdAutor() <= 0) {
            throw new ValidationException("El id del autor debe ser positivo");
        }
        if (libro.getIdCategorias() == null) {
            libro.setIdCategorias(List.of());
        }
        if (libro.getIdCategorias().stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ValidationException("Los ids de categoría deben ser positivos");
        }
    }

    private void validateReferences(LibroDTO libro) {
        autorDao.findById(libro.getIdAutor());
        for (Long idCategoria : libro.getIdCategorias()) {
            categoriaDao.findById(idCategoria);
        }
    }

    private boolean isValidIsbn(String isbn) {
        if (isbn == null) {
            return false;
        }
        String normalized = isbn.replaceAll("[-\\s]", "").toUpperCase();
        if (normalized.matches("\\d{13}")) {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = normalized.charAt(i) - '0';
                sum += digit * (i % 2 == 0 ? 1 : 3);
            }
            int checkDigit = (10 - sum % 10) % 10;
            return checkDigit == normalized.charAt(12) - '0';
        }
        if (!normalized.matches("\\d{9}[\\dX]")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char character = normalized.charAt(i);
            int digit = character == 'X' ? 10 : character - '0';
            sum += digit * (10 - i);
        }
        return sum % 11 == 0;
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("El id del libro debe ser positivo");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}