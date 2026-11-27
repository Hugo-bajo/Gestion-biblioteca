# Reglas del proyecto

- Mantén la arquitectura por capas: Servlet → Service → DAO → base de datos.
- Usa DTOs obligatoriamente entre capas; nunca devuelvas entidades.
- Accede a la base de datos con JDBC puro, `PreparedStatement`, HikariCP y `try-with-resources`.
- Obtén la URL y las credenciales de la base de datos exclusivamente de `DB_URL`, `DB_USER` y `DB_PASSWORD`.
- Usa Gson para JSON.
- Usa los códigos HTTP 200, 201, 400, 404 y 500 según corresponda.
- Escribe código simple, implementa solo lo solicitado y no añadas comentarios.