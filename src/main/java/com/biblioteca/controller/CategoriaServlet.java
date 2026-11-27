package com.biblioteca.controller;

import com.biblioteca.dto.CategoriaDTO;
import com.biblioteca.service.CategoriaService;
import jakarta.servlet.annotation.WebServlet;

import java.util.List;

@WebServlet(urlPatterns = "/api/categorias/*")
public class CategoriaServlet extends JsonCrudServlet<CategoriaDTO> {
    private CategoriaService service;

    @Override
    public void init() {
        service = (CategoriaService) getServletContext().getAttribute(CategoriaService.class.getName());
    }

    @Override
    protected Class<CategoriaDTO> dtoClass() {
        return CategoriaDTO.class;
    }

    @Override
    protected List<CategoriaDTO> findAll() {
        return service.findAll();
    }

    @Override
    protected CategoriaDTO findById(Long id) {
        return service.findById(id);
    }

    @Override
    protected CategoriaDTO create(CategoriaDTO dto) {
        return service.create(dto);
    }

    @Override
    protected CategoriaDTO update(CategoriaDTO dto) {
        return service.update(dto);
    }

    @Override
    protected void setId(CategoriaDTO dto, Long id) {
        dto.setIdCategoria(id);
    }

    @Override
    protected void delete(Long id) {
        service.delete(id);
    }
}