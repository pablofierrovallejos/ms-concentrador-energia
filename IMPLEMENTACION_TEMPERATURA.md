# Implementación de Sistema de Medición de Temperatura

## Resumen
Se ha implementado un sistema completo para medir y almacenar datos de temperatura del nodo T110 (192.168.2.110), separado de las mediciones de energía.

## Cambios Realizados

### 1. Nueva Tabla de Base de Datos
- **Tabla**: `medicion_temperatura`
- **Campos**: id, nombrenodo, temperatura, fechahora, device_ip
- **Stored Procedure**: `sp_insertarMedicionTemperatura`
- **Trigger**: `trigger_websocket_notification_temperatura` (notificaciones WebSocket automáticas)

### 2. Nuevos Componentes Java

#### Entidades y DTOs
- `MedicionTemperatura.java` - Entidad JPA para la tabla
- `TemperaturaDataDTO.java` - DTO para transferencia de datos

#### Servicios
- `IMedicionTemperaturaService.java` - Repositorio JPA con queries personalizadas

#### Controlador
- `EnergyController.java` - Modificado para detectar automáticamente nodos de temperatura

### 3. Servicio de Lectura
- `TasmotaReaderService.readTemperatureNode()` - Lee temperatura desde http://192.168.2.110/api/simple

### 4. Configuración
- `application.properties` - Añadidas propiedades para el nodo T110

## Pasos de Implementación

### Paso 1: Ejecutar Script SQL en la Base de Datos

```bash
# Conectarse a MySQL
mysql -u root -p db_springboot_cloud

# Ejecutar el script
source src/sp/create_tabla_temperatura.sql
```

O manualmente:
```sql
USE db_springboot_cloud;
-- Copiar y ejecutar el contenido completo de src/sp/create_tabla_temperatura.sql
```

### Paso 2: Recompilar y Redesplegar

```bash
# Compilar el proyecto
mvn clean package -DskipTests

# Detener contenedores actuales
docker-compose down

# Reconstruir y iniciar
docker-compose up --build -d
```

### Paso 3: Verificar el Funcionamiento

#### Ver logs del microservicio cloud:
```bash
docker logs -f <container_id_cloud>
```

Deberías ver mensajes como:
```
INFO c.p.e.controllers.EnergyController : Recibiendo 6 mediciones para insertar en BD
INFO c.p.e.controllers.EnergyController : Medición de temperatura insertada: T110 - 32.38°C
```

#### Verificar datos en la BD:
```sql
USE db_springboot_cloud;

-- Ver últimas temperaturas
SELECT * FROM medicion_temperatura ORDER BY fechahora DESC LIMIT 10;

-- Ver temperatura del nodo T110
SELECT * FROM medicion_temperatura WHERE nombrenodo = 'T110' ORDER BY fechahora DESC LIMIT 20;

-- Verificar notificaciones WebSocket
SELECT * FROM websocket_notifications_temperatura WHERE procesado = 0;
```

## Endpoints Disponibles

### POST /api/energia/recibir-mediciones
- Recibe mediciones de energía Y temperatura
- Detecta automáticamente el tipo por el nombre del nodo (T110) o address (110)

### GET /api/energia/temperatura/ultimas/{nombrenodo}?limit=10
- Obtiene las últimas N mediciones de temperatura
- Ejemplo: `/api/energia/temperatura/ultimas/T110?limit=20`

### GET /api/energia/temperatura/{nombrenodo}/{fecha}
- Obtiene temperaturas de un día específico
- Ejemplo: `/api/energia/temperatura/T110/2025-12-28`

### GET /api/energia/health
- Verifica que el servicio está activo

## Configuración del Nodo T110

En `application.properties`:
```properties
# Configuracion del nodo de temperatura T110
temperature.node.t110.enabled=true
temperature.node.t110.ip=192.168.2.110
```

Para desactivar temporalmente:
```properties
temperature.node.t110.enabled=false
```

## Flujo de Datos

```
Nodo T110 (192.168.2.110)
    ↓
    GET /api/simple → {"temp1": 32.38}
    ↓
TasmotaReaderService.readTemperatureNode()
    ↓
EnergyDataDTO (voltage = temperatura, address = 110)
    ↓
CloudSenderService → POST /api/energia/recibir-mediciones
    ↓
EnergyController.insertarMedicionEnBD()
    ↓
    ├─ Detecta T110 o address=110
    ↓
IMedicionTemperaturaService.agregarMedicionTemperatura()
    ↓
sp_insertarMedicionTemperatura()
    ↓
medicion_temperatura (tabla)
    ↓
trigger_websocket_notification_temperatura
    ↓
websocket_notifications_temperatura (notificaciones en tiempo real)
```

## Ventajas de esta Arquitectura

1. **Separación de Responsabilidades**: Temperatura y energía en tablas distintas
2. **Escalabilidad**: Fácil agregar más nodos de temperatura (T111, T112, etc.)
3. **Queries Eficientes**: Índices optimizados para consultas por nodo y fecha
4. **WebSocket Ready**: Sistema de notificaciones en tiempo real incluido
5. **Backward Compatible**: No afecta las mediciones de energía existentes

## Solución de Problemas

### No se insertan temperaturas
1. Verificar que la tabla existe: `SHOW TABLES LIKE 'medicion_temperatura';`
2. Verificar el stored procedure: `SHOW PROCEDURE STATUS WHERE Name = 'sp_insertarMedicionTemperatura';`
3. Ver logs del container cloud para errores de SQL

### El nodo T110 no responde
1. Verificar conectividad: `ping 192.168.2.110`
2. Probar endpoint: `curl http://192.168.2.110/api/simple`
3. Verificar logs del microservicio local

### Temperatura no se muestra en frontend
1. Verificar tabla: `SELECT COUNT(*) FROM medicion_temperatura;`
2. Verificar notificaciones: `SELECT COUNT(*) FROM websocket_notifications_temperatura WHERE procesado = 0;`
3. Verificar conexión WebSocket del cliente

## Próximos Pasos

1. Actualizar el frontend para mostrar gráficos de temperatura
2. Implementar alertas si la temperatura supera umbrales
3. Agregar más nodos de temperatura según sea necesario
4. Crear dashboard específico para monitoreo de temperatura

## Archivos Modificados

```
src/sp/create_tabla_temperatura.sql (NUEVO)
src/main/java/com/pablofierro/energia/
    ├── models/
    │   ├── entity/MedicionTemperatura.java (NUEVO)
    │   ├── dto/TemperaturaDataDTO.java (NUEVO)
    │   └── service/IMedicionTemperaturaService.java (NUEVO)
    ├── controllers/EnergyController.java (MODIFICADO)
    ├── services/TasmotaReaderService.java (MODIFICADO)
    └── MsConcentradorEnergiaApplication.java (MODIFICADO)
src/main/resources/application.properties (MODIFICADO)
```
