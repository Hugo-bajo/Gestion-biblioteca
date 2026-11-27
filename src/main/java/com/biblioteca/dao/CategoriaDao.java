package com.biblioteca.dao;

import com.biblioteca.dto.CategoriaDTO;

import java.util.List;

public interface CategoriaDao {
    CategoriaDTO create(CategoriaDTO categoria);

    CategoriaDTO findById(Long idCategoria);

    List<CategoriaDTO> findAll();

    CategoriaDTO update(CategoriaDTO categoria);

    void delete(Long idCategoria);
}