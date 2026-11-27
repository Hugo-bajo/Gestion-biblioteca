package com.biblioteca.dao;

import com.biblioteca.exception.DaoException;
import com.biblioteca.exception.DuplicateException;

import java.sql.Connection;
import java.sql.SQLException;

final class JdbcDaoSupport {
    private JdbcDaoSupport() {
    }

    static RuntimeException translate(String message, SQLException exception) {
        for (SQLException current = exception; current != null; current = current.getNextException()) {
            if (current.getErrorCode() == 1062 || "23505".equals(current.getSQLState())) {
                return new DuplicateException(message, exception);
            }
        }
        return new DaoException(message, exception);
    }

    static void rollback(Connection connection, Throwable exception) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            exception.addSuppressed(rollbackException);
        }
    }
}