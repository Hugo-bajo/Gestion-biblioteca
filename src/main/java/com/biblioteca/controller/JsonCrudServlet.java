package com.biblioteca.controller;

import com.biblioteca.exception.DuplicateException;
import com.biblioteca.exception.NotFoundException;
import com.biblioteca.exception.ValidationException;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public abstract class JsonCrudServlet<T> extends HttpServlet {
    private final Gson gson = new Gson();

    protected abstract Class<T> dtoClass();

    protected abstract List<T> findAll();

    protected abstract T findById(Long id);

    protected abstract T create(T dto);

    protected abstract T update(T dto);

    protected abstract void setId(T dto, Long id);

    protected abstract void delete(Long id);

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        execute(request, response, () -> {
            Long id = getOptionalId(request);
            writeJson(response, HttpServletResponse.SC_OK, id == null ? findAll() : findById(id));
        });
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        execute(request, response, () ->
                writeJson(response, HttpServletResponse.SC_CREATED, create(readDto(request))));
    }

    @Override
    protected final void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        execute(request, response, () -> {
            Long id = getRequiredId(request);
            T dto = readDto(request);
            if (dto == null) {
                throw new ValidationException("El cuerpo JSON es obligatorio");
            }
            setId(dto, id);
            writeJson(response, HttpServletResponse.SC_OK, update(dto));
        });
    }

    @Override
    protected final void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        execute(request, response, () -> {
            Long id = getRequiredId(request);
            T deleted = findById(id);
            delete(id);
            writeJson(response, HttpServletResponse.SC_OK, deleted);
        });
    }

    private void execute(HttpServletRequest request, HttpServletResponse response, RequestAction action)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            action.run();
        } catch (IOException exception) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        } catch (ValidationException | JsonParseException | NumberFormatException exception) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (NotFoundException exception) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        } catch (DuplicateException exception) {
            writeError(response, HttpServletResponse.SC_CONFLICT, exception.getMessage());
        } catch (RuntimeException exception) {
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    private T readDto(HttpServletRequest request) throws IOException {
        return gson.fromJson(request.getReader(), dtoClass());
    }

    private Long getOptionalId(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty() || "/".equals(pathInfo)) {
            return null;
        }
        return parseId(pathInfo);
    }

    private Long getRequiredId(HttpServletRequest request) {
        Long id = getOptionalId(request);
        if (id == null) {
            throw new ValidationException("Se requiere el id del recurso");
        }
        return id;
    }

    private Long parseId(String pathInfo) {
        if (!pathInfo.matches("/[0-9]+")) {
            throw new ValidationException("El id del recurso no es válido");
        }
        return Long.parseLong(pathInfo.substring(1));
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        var writer = response.getWriter();
        gson.toJson(body, writer);
        writer.flush();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        var writer = response.getWriter();
        gson.toJson(new ErrorResponse(message == null ? "Error interno del servidor" : message), writer);
        writer.flush();
    }

    @FunctionalInterface
    private interface RequestAction {
        void run() throws IOException;
    }

    private static class ErrorResponse {
        private final String error;

        private ErrorResponse(String error) {
            this.error = error;
        }
    }
}