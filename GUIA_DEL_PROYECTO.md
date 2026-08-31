# Guía del proyecto TallerExpress

## 1. ¿Qué solicita el PDF?

El enunciado solicita desarrollar una aplicación de escritorio para un taller mecánico utilizando:

- Java SE 17 o superior.
- Ventanas modales con `JOptionPane`.
- Persistencia de información mediante JDBC.
- Una base de datos MySQL o PostgreSQL.
- Arquitectura por capas: `controller`, `service`, `dao` y `model`.
- Programación orientada a objetos.
- Interfaces, abstracción, encapsulamiento, herencia y polimorfismo.
- Excepciones personalizadas y validaciones de negocio.
- Transacciones para las órdenes de servicio y el inventario.

La aplicación debe centralizar la información de clientes, vehículos, repuestos, usuarios y órdenes de servicio.

## 2. Estructura general del proyecto

```text
TallerExpress/
├── pom.xml
├── README.md
├── GUIA_DEL_PROYECTO.md
└── src/
    ├── main/
    │   └── java/com/tallerexpress/
    │       ├── Main.java
    │       ├── config/
    │       ├── controller/
    │       ├── dao/
    │       ├── exception/
    │       ├── model/
    │       └── service/
    └── test/
        └── java/com/tallerexpress/
```

### `pom.xml`

Es el archivo de configuración de Maven. Indica:

- La versión de Java que utiliza el proyecto.
- El driver JDBC de PostgreSQL.
- La dependencia JUnit para las pruebas.
- El plugin que permite ejecutar `Main` con `mvn exec:java`.
- El plugin que ejecuta las pruebas.

### `src/main/java`

Contiene todo el código que forma parte de la aplicación.

### `src/test/java`

Contiene pruebas automáticas. Estas pruebas no son obligatorias en el PDF, pero permiten comprobar que las operaciones más importantes funcionan correctamente.

## 3. Explicación de cada paquete

### Paquete principal: `com.tallerexpress`

#### `Main.java`

Es el punto de entrada del programa. Su método `main` realiza tres acciones:

1. Configura la apariencia de las ventanas de acuerdo con el sistema operativo.
2. Llama a `Database.initialize()` para crear las tablas y el usuario administrador.
3. Crea `AppController` e inicia la interfaz gráfica.

`Main` no contiene reglas de negocio ni consultas SQL. Su responsabilidad es únicamente iniciar la aplicación.

### Carpeta `config`

#### `Database.java`

Administra la conexión JDBC y la estructura de la base de datos.

Sus responsabilidades son:

- Guardar la URL, el usuario y la contraseña de conexión.
- Entregar conexiones mediante `getConnection()`.
- Crear las tablas `clients`, `vehicles`, `parts`, `users`, `service_orders` y `order_parts`.
- Crear el usuario administrador con las credenciales definidas localmente en `.env`.
- Declarar llaves primarias, llaves foráneas, restricciones únicas y validaciones de valores no negativos.

El proyecto utiliza PostgreSQL, levantado mediante `docker-compose.yml`. La aplicación se conecta por JDBC y crea automáticamente las tablas la primera vez que se ejecuta.

PostgreSQL se publica en el puerto `5433` del computador para evitar conflictos con otras instalaciones locales. Dentro del contenedor continúa utilizando el puerto estándar `5432`.

### Carpeta `controller`

#### `AppController.java`

Controla la interacción con el usuario. Es la capa que muestra los `JOptionPane` y recibe la información escrita en los formularios.

Contiene:

- Formulario de inicio de sesión.
- Menú principal.
- Menú y formulario de repuestos.
- Menú y formulario de clientes.
- Menú y formulario de vehículos.
- Menú y formulario de usuarios.
- Menú y formulario de órdenes de servicio.
- Selección de clientes, vehículos y repuestos.
- Tablas de texto alineadas para mostrar resultados.
- Mensajes de confirmación, éxito y error.

El controlador no escribe directamente en la base de datos. Envía los datos a `PartService` o `WorkshopService`, respetando la separación por capas.

### Carpeta `model`

Representa las entidades principales del negocio. Cada `record` encapsula los datos de una entidad.

#### `Part.java`

Representa un repuesto. Incluye código, nombre, categoría, proveedor, existencias, precio, estado y fecha de creación.

#### `Client.java`

Representa un cliente. Incluye documento, nombre, teléfono, correo, estado y fecha de creación.

#### `Vehicle.java`

Representa un vehículo asociado a un cliente. Incluye placa, marca, modelo y año.

#### `User.java`

Representa un usuario del sistema. Incluye credenciales, nombre, rol, estado y fecha de creación.

#### `ServiceOrder.java`

Representa una orden de servicio. Guarda el cliente, vehículo, mecánico, fecha, problema, diagnóstico, estado y costo final.

#### `OrderPart.java`

Representa un repuesto y la cantidad que se utilizará dentro de una orden.

### Carpeta `dao`

DAO significa *Data Access Object*. Esta capa encapsula el acceso a la base de datos.

#### `CrudDao.java`

Es una interfaz genérica que define operaciones comunes:

- Crear un registro.
- Actualizar un registro.
- Consultar todos los registros.

#### `PartDao.java`

Extiende `CrudDao<Part>` y agrega operaciones propias de los repuestos:

- Comprobar si un código ya existe.
- Filtrar por categoría y proveedor.

#### `JdbcPartDao.java`

Implementa `PartDao` mediante JDBC. Contiene consultas `INSERT`, `UPDATE` y `SELECT`, utiliza `PreparedStatement` y cierra recursos con `try-with-resources`.

En la implementación actual, el DAO separado está desarrollado para repuestos. Las consultas de clientes, vehículos, usuarios y órdenes están dentro de `WorkshopService`. Para cumplir de manera completamente estricta la separación DAO solicitada en el PDF, esas consultas también se podrían extraer a `ClientDao`, `VehicleDao`, `UserDao` y `ServiceOrderDao`.

### Carpeta `service`

Contiene las reglas de negocio. Esta capa se encuentra entre el controlador y el acceso a datos.

#### `PartService.java`

Administra los repuestos y valida que:

- El código y el nombre sean obligatorios.
- El código de referencia no se encuentre repetido.
- El stock total y disponible no sean negativos.
- El stock disponible no sea mayor que el total.
- El precio no sea negativo.

También registra trazas como `POST /repuestos`, `PATCH /repuestos` y `GET /repuestos`.

#### `WorkshopService.java`

Agrupa las operaciones de clientes, vehículos, usuarios y órdenes:

- Validar credenciales y roles.
- Registrar, listar, activar, desactivar y eliminar usuarios.
- Registrar y listar clientes.
- Registrar vehículos y consultar vehículos por cliente.
- Validar que una placa sea única.
- Validar que el cliente esté activo.
- Registrar órdenes de servicio.
- Validar que el vehículo pertenezca al cliente.
- Validar la existencia y disponibilidad de repuestos.
- Descontar las cantidades utilizadas del inventario.
- Finalizar órdenes y calcular su costo.
- Consultar el historial de servicios por vehículo.
- Imprimir trazas similares a llamadas HTTP.

### Carpeta `service/user`

Contiene la implementación del patrón de diseño Decorator solicitado para la creación de usuarios.

#### `UserCreator.java`

Interfaz que declara la operación `create`.

#### `BaseUserCreator.java`

Implementación base. Devuelve el usuario sin agregar propiedades.

#### `UserCreatorDecorator.java`

Clase abstracta que envuelve otro `UserCreator`. Sirve como base para construir decoradores.

#### `DefaultPropertiesUserCreator.java`

Agrega al usuario las propiedades predeterminadas pedidas en el PDF:

- Rol: `RECEPCIONISTA`.
- Estado: `ACTIVO`.
- Fecha de creación: fecha y hora actual.

Esto se realiza sin modificar la lógica de `BaseUserCreator`.

### Carpeta `exception`

#### `BusinessException.java`

Representa errores causados por reglas del negocio, por ejemplo código duplicado, placa duplicada, stock insuficiente o credenciales incorrectas.

#### `DataAccessException.java`

Representa errores producidos al conectarse o ejecutar operaciones en la base de datos.

Estas excepciones llegan al controlador, que muestra el mensaje mediante `JOptionPane` y registra los detalles técnicos en consola cuando corresponde.

### Carpeta de pruebas

#### `WorkshopIntegrationTest.java`

Comprueba automáticamente el siguiente flujo:

1. Inicialización de la base de datos.
2. Inicio de sesión del administrador.
3. Registro de un cliente.
4. Registro de un vehículo.
5. Registro de un repuesto.
6. Creación de una orden con repuestos.
7. Descuento correcto del inventario.
8. Finalización de la orden.
9. Cálculo del costo total.

## 4. Cumplimiento de las funcionalidades del PDF

### Gestión de repuestos

El sistema permite registrar, editar, listar y filtrar repuestos. El código se valida como único y las restricciones de inventario se verifican tanto en Java como en la base de datos.

Ubicación principal:

- Interfaz: `AppController.partMenu()` y `partForm()`.
- Reglas: `PartService`.
- JDBC: `JdbcPartDao`.
- Datos: `Part`.

### Gestión de clientes y vehículos

Se pueden registrar clientes y asociar uno o varios vehículos. La placa tiene una restricción única y se puede consultar la lista de vehículos de un cliente.

Ubicación principal:

- Interfaz: `clientMenu()` y `vehicleMenu()`.
- Reglas y JDBC: `WorkshopService`.
- Datos: `Client` y `Vehicle`.

### Usuarios y autenticación

El programa comienza con un login. Solamente un usuario con rol `ADMIN` puede abrir el menú de usuarios. Los usuarios nuevos reciben automáticamente el rol `RECEPCIONISTA`, estado `ACTIVO` y fecha actual mediante un decorador.

Ubicación principal:

- Login y menú: `AppController.login()` y `userMenu()`.
- Reglas y JDBC: `WorkshopService`.
- Decorador: `service/user`.

La contraseña se guarda actualmente como texto para mantener el alcance académico del ejercicio. En un sistema real debe almacenarse con un algoritmo de hash seguro como BCrypt o Argon2.

### Órdenes de servicio

Al registrar una orden se seleccionan cliente, vehículo, mecánico, problema, diagnóstico y repuestos. El servicio valida la relación entre cliente y vehículo y comprueba el inventario.

La creación se procesa de esta forma:

```text
setAutoCommit(false)
        ↓
insertar orden
        ↓
insertar repuestos de la orden
        ↓
descontar inventario
        ↓
commit si todo funciona
rollback si ocurre un error
```

Al finalizar, se suman `cantidad × precio unitario`, se guarda el costo final y se cambia el estado a `FINALIZADA` dentro de una transacción.

Ubicación principal:

- Interfaz: `orderMenu()` y `createOrder()`.
- Transacciones: `WorkshopService.createOrder()` y `finishOrder()`.
- Datos: `ServiceOrder` y `OrderPart`.

### Interfaz gráfica

Toda la interacción utiliza `JOptionPane`. Los listados se construyen con fuente monoespaciada para mantener las columnas alineadas y los estados se muestran como `[ACTIVO]` o `[INACTIVO]`.

### Trazas similares a HTTP

Después de las operaciones se escriben mensajes en consola, por ejemplo:

```text
[HTTP] POST /clientes
[HTTP] GET /repuestos
[HTTP] PATCH /ordenes/1
[HTTP] DELETE /usuarios/2
```

Estas trazas son una simulación solicitada por el enunciado; la aplicación no es una API web.

## 5. Cómo ejecutar y probar el sistema

Primero se debe levantar PostgreSQL:

```bash
docker --context default compose up -d
```

Para comprobar que el contenedor está funcionando:

```bash
docker --context default compose ps
```

En un equipo donde Docker Desktop esté iniciado se puede omitir `--context default`.

Crear las tablas y verificar la conexión sin abrir la interfaz gráfica:

```bash
mvn compile exec:java -Dexec.mainClass=com.tallerexpress.config.DatabaseSetup
```

Compilar el proyecto:

```bash
mvn clean compile
```

Ejecutar la aplicación:

```bash
mvn exec:java
```

Las credenciales iniciales se encuentran en el archivo local `.env`, que Git ignora. Para
ejecutar Java hay que cargar primero sus variables:

```bash
set -a
source .env
set +a
mvn exec:java
```

Orden recomendado para probar:

1. Iniciar sesión.
2. Registrar uno o varios repuestos.
3. Registrar un cliente.
4. Registrar un vehículo para ese cliente.
5. Registrar una orden utilizando el vehículo y los repuestos.
6. Consultar el inventario para comprobar el descuento.
7. Finalizar la orden.
8. Consultar el historial del vehículo.

## 6. Elementos pendientes para la entrega académica

Antes de entregar el proyecto se debe:

- Completar en `README.md` el clan, correo y documento del coder.
- Ejecutar la aplicación y agregar capturas reales de los `JOptionPane`.
- Publicar el proyecto en un repositorio público de GitHub.
- Generar el archivo comprimido solicitado.
- Si se evalúa estrictamente un DAO por entidad, extraer de `WorkshopService` las consultas de clientes, vehículos, usuarios y órdenes a sus respectivos DAO.

## 7. Resumen de la separación por capas

```text
Usuario
   ↓
AppController       muestra ventanas y recibe datos
   ↓
Service             valida las reglas del negocio
   ↓
DAO / JDBC          ejecuta las consultas SQL
   ↓
Database            crea y entrega conexiones
   ↓
Base de datos       conserva la información
```

Esta separación facilita comprender, mantener y ampliar el programa sin mezclar la interfaz gráfica con las reglas del negocio y las consultas SQL.
