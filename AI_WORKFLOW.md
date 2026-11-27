# AI_WORKFLOW

Proyecto: gestión de biblioteca (Java, MySQL y Docker Compose)
Autor: Hugo Bajo Gomez
Módulo: Acceso a Datos (2º DAM)

## 1. Herramienta y forma de trabajo

Agente de codificación: Junie (Powershell).
Modo de uso: modo normal.

Mi papel fue el de arquitecto, revisor de código y responsable del despliegue. Definí la arquitectura y la pila tecnológica antes de empezar, le fui dando a Junie los prompts por capas (uno por cada bloque del proyecto) y después revisé el código generado, tanto con una auditoría pedida al propio agente como con búsquedas manuales en el proyecto.

## 2. Decisiones de arquitectura

- Pila: Maven, Tomcat 10.1 (Jakarta Servlet 6), MySQL 8.4, HikariCP, Gson, JUnit 5, Mockito y H2 para los tests.
- Arquitectura por capas con dependencia en un solo sentido: Servlet -> Service -> DAO -> BD. Entre capas solo se transfieren DTOs, nunca las entidades.
- Capa de acceso a datos con JDBC puro, PreparedStatement, try-with-resources y pool de conexiones HikariCP. Los DAO reciben el DataSource por constructor, lo que permite probarlos con otra base de datos. Las SQLException se traducen a excepciones propias en una clase de apoyo (JdbcDaoSupport).
- Modelo relacional con las tablas autor, libro, categoria y libro_categoria: relación 1:N entre autor y libro, relación N:M entre libro y categoria, con claves foráneas y reglas ON DELETE / ON UPDATE. El script db/init.sql contiene la estructura (DDL) y los datos de prueba (DML).
- Configuración externalizada: la URL, el usuario y la contraseña de la base de datos solo se leen de las variables de entorno DB_URL, DB_USER y DB_PASSWORD. Si falta alguna, la aplicación lanza una excepción al arrancar y no usa valores por defecto. Los valores se definen en .env (fuera del repositorio) a partir de .env.example y los inyecta Docker Compose.
- El DataSource y los servicios se inicializan una sola vez en un listener de contexto (BibliotecaContextListener).
- Tests de DAO con H2 en memoria y modo MySQL, con una base de datos distinta por test, para que no dependan de MySQL ni de variables de entorno. Tests de Service con Mockito.
- Despliegue: Dockerfile multi-stage (Maven para compilar el .war y Tomcat para ejecutarlo) y docker-compose.yml con los servicios app, db (con healthcheck y volumen nombrado) y adminer, con depends_on con service_healthy.
- Se creó .junie/guidelines.md con las reglas del proyecto para que el agente las siguiera en todos los prompts.

## 3. Prompts clave dados a Junie

Los prompts se enviaron uno por uno, en este orden.

1. Proyecto base: proyecto Maven con empaquetado war para Tomcat 10.1, paquete base com.biblioteca, dependencias, paquetes por capas y archivo de reglas .junie/guidelines.md.
2. Base de datos: db/init.sql con DDL y datos de prueba (autor, libro, categoria y libro_categoria), claves foráneas y restricciones UNIQUE y NOT NULL.
3. Modelo, DTOs, excepciones personalizadas y clase DataSourceProvider que lee las credenciales con System.getenv y sin valores por defecto.
4. Capa DAO: interfaces e implementaciones JDBC con CRUD completo, PreparedStatement, try-with-resources, traducción de SQLException y transacción con commit y rollback al guardar un libro con sus categorías.
5. Capa de servicio: validaciones (campos obligatorios, ISBN, año, no borrar un autor con libros) y excepciones de validación, no encontrado y duplicado.
6. Controladores: Servlets REST en /api/autores, /api/libros y /api/categorias con JSON (Gson), códigos HTTP 200, 201, 400, 404, 409 y 500, y listener de inicialización.
7. Frontend: index.html, css/styles.css y js/app.js con fetch, mensaje de carga, mensajes de error claros y diseño responsivo, sin Java ni SQL en la vista.
8. Tests: JUnit 5 con H2 para el DAO (CRUD, clave duplicada, id inexistente, rollback y borrado de autor con libros) y Mockito para el Service.
9. Archivos de despliegue: .env.example, .gitignore, Dockerfile multi-stage y docker-compose.yml.
10. README.md con diagrama entidad-relación, esquema de contenedores, variables de entorno, pasos de despliegue, puertos y credenciales de prueba.
11. Auditoría final: revisión estática de todo el proyecto, sin ejecutar nada, comprobando que las capas, el SQL, las rutas, el frontend, los tests y Docker están conectados. Se le pidió corregir solo los errores reales y entregar un informe de cambios y de lo que no se puede comprobar sin ejecutar.

Resultado de la auditoría de Junie: Junie no pudo hacer los test pedidos asi que comprobo si el codigo deberia de funcionar bien.

## 4. Revisiones que hice yo

Después de generar el código, comprobé a mano con la búsqueda de IntelliJ (Find in Files):

- Búsqueda de "jdbc:" en todo el proyecto: solo aparece en docker-compose.yml (la URL se construye con las variables del .env), en el README (documentación) y en el test de DAO (jdbc:h2:mem con un identificador aleatorio). No hay ninguna URL de conexión escrita en src/main.
- Búsqueda de "password" en src/main/java: todos los resultados están en DataSourceProvider.java. La contraseña se lee con System.getenv("DB_PASSWORD"), se lanza una IllegalStateException si falta alguna de las tres variables y solo se pasa a HikariCP con setPassword. No hay ninguna contraseña escrita en el código.
- Búsqueda de Statement sin PreparedStatement (expresión regular): no se encontro ninguno fuera de lugar.
- Búsqueda de "javax.servlet": no se encontro ninguno fuera de lugar. Los imports de javax.sql.DataSource son correctos porque forman parte de Java SE y no se migran a jakarta.

## 5. Errores corregidos manualmente

No hizo falta la correcion manual de ningun error, el proyecto se fue arreglando y reajustando a medida que se le daban prompts.

## 6. Limitaciones

No he podido ejecutar toda la pila de despliegue en mi equipo, así que la revisión fue estática (lectura del código y búsquedas). Lo que no se puede comprobar sin ejecutar el proyecto es, por ejemplo, el arranque real de los contenedores o el comportamiento del driver de MySQL en Tomcat.