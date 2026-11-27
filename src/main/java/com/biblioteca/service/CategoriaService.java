package com.biblioteca.service;

import com.biblioteca.dao.CategoriaDao;
import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.exception.ValidationException;

import java.util.List;
import java.util.Objects;

public class CategoriaService {
    private final CategoriaDao categoriaDao;

    public CategoriaService(CategoriaDao categoriaDao) {
        this.categoriaDao = Objects.requireNonNull(categoriaDao);
    }

    public CategoriaDTO create(CategoriaDTO categoria) {
        validateCategoria(categoria);
        return categoriaDao.create(categoria);
    }

    public CategoriaDTO findById(Long idCategoria) {
        validateId(idCategoria);
        return categoriaDao.findById(idCategoria);
    }

    public List<CategoriaDTO> findAll() {
        return categoriaDao.findAll();
    }

    public CategoriaDTO update(CategoriaDTO categoria) {
        validateCategoria(categoria);
        validateId(categoria.getIdCategoria());
        return categoriaDao.update(categoria);
    }

    public void delete(Long idCategoria) {
        validateId(idCategoria);
        categoriaDao.delete(idCategoria);
    }

    private void validateCategoria(CategoriaDTO categoria) {
        if (categoria == null || categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new ValidationException("El nombre de la categoría es obligatorio");
        }
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new ValidationException("El id de la categoría debe ser positivo");
        }
    }
}