package com.biblioteca.controller;

import com.biblioteca.dto.AutorDTO;
import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JsonCrudServletTest {
    @Test
    void getByIdReturnsDtoAsUtf8Json() throws Exception {
        TestServlet servlet = new TestServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getPathInfo()).thenReturn("/7");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(request, response);

        verify(request).setCharacterEncoding("UTF-8");
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/json;charset=UTF-8");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertEquals("{\"idAutor\":7,\"nombre\":\"Ada\",\"apellido\":\"Lovelace\"}", body.toString());
    }

    @Test
    void invalidPathReturnsBadRequestJson() throws Exception {
        TestServlet servlet = new TestServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getPathInfo()).thenReturn("/abc");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("{\"error\":\"El id del recurso no es válido\"}", body.toString());
    }

    @Test
    void postReturnsCreatedDto() throws Exception {
        TestServlet servlet = new TestServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        assertEquals("{\"idAutor\":8,\"nombre\":\"Ada\",\"apellido\":\"Lovelace\"}", body.toString());
    }

    @Test
    void duplicateReturnsConflictJson() throws Exception {
        TestServlet servlet = new TestServlet();
        servlet.duplicateOnCreate = true;
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
        assertEquals("{\"error\":\"Autor duplicado\"}", body.toString());
    }

    @Test
    void missingResourceReturnsNotFoundJson() throws Exception {
        TestServlet servlet = new TestServlet();
        servlet.notFoundOnFind = true;
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getPathInfo()).thenReturn("/7");
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("{\"error\":\"Autor no encontrado\"}", body.toString());
    }

    @Test
    void unexpectedErrorReturnsInternalServerErrorJson() throws Exception {
        TestServlet servlet = new TestServlet();
        servlet.errorOnCreate = true;
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("{}")));
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        servlet.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        assertEquals("{\"error\":\"Error interno del servidor\"}", body.toString());
    }

    private static class TestServlet extends JsonCrudServlet<AutorDTO> {
        private boolean duplicateOnCreate;
        private boolean notFoundOnFind;
        private boolean errorOnCreate;

        @Override
        protected Class<AutorDTO> dtoClass() {
            return AutorDTO.class;
        }

        @Override
        protected List<AutorDTO> findAll() {
            return List.of();
        }

        @Override
        protected AutorDTO findById(Long id) {
            if (notFoundOnFind) {
                throw new NotFoundException("Autor no encontrado");
            }
            return new AutorDTO(id, "Ada", "Lovelace");
        }

        @Override
        protected AutorDTO create(AutorDTO dto) {
            if (duplicateOnCreate) {
                throw new DuplicateException("Autor duplicado");
            }
            if (errorOnCreate) {
                throw new IllegalStateException("Error interno");
            }
            return new AutorDTO(8L, "Ada", "Lovelace");
        }

        @Override
        protected AutorDTO update(AutorDTO dto) {
            return dto;
        }

        @Override
        protected void setId(AutorDTO dto, Long id) {
            dto.setIdAutor(id);
        }

        @Override
        protected void delete(Long id) {
        }
    }
}