# Guía de Inicio Rápido - Sistema de Monitoreo Modbus

## Pasos para Configuración

### 1. Compilar el Proyecto

```bash
mvn clean package
```

### 2. Configurar la Instancia Local

Editar `application.properties` o crear `application-local.properties`:

```properties
# Configurar IPs de tus dispositivos Modbus
modbus.devices=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163

# Intervalo de lectura (15 segundos = 15000 ms)
modbus.polling.interval=15000

# URL del servidor cloud donde se enviarán los datos
cloud.endpoint.url=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones
```

### 3. Probar Lectura Modbus (Opcional pero Recomendado)

Antes de ejecutar el sistema completo, prueba que puedes leer los dispositivos:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test-modbus
```

Esto leerá cada dispositivo una vez y mostrará los valores en consola.

**Si la prueba falla:**
- Verifica conectividad de red: `ping 192.168.2.221`
- Verifica que el puerto 502 esté abierto
- Revisa el mapeo de registros Modbus en `ModbusReaderService.java`

### 4. Ejecutar Instancia Local

**Opción A: Desde Maven**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Opción B: Desde JAR**
```bash
java -jar -Dspring.profiles.active=local target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

### 5. Configurar y Ejecutar Instancia Cloud

En el servidor cloud, editar `application.properties` o usar `application-cloud.properties`:

```properties
# Configurar conexión a base de datos
spring.datasource.url=jdbc:mysql://TU_DB_IP:3306/db_springboot_cloud?serverTimezone=America/Santiago
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password

# Desactivar lectura Modbus en cloud
modbus.polling.interval=999999999
modbus.devices=
```

**Ejecutar:**
```bash
java -jar -Dspring.profiles.active=cloud target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

## Verificación del Sistema

### 1. Verificar Health de la Instancia Cloud

```bash
curl http://TU_IP_CLOUD:8080/api/energia/health
```

Deberías recibir:
```json
{
  "status": "UP",
  "timestamp": "..."
}
```

### 2. Monitorear Logs de la Instancia Local

Busca en los logs:
```
=== Iniciando lectura de 4 dispositivos Modbus ===
Datos leídos exitosamente de dispositivo: 192.168.2.221
Datos leídos de 4 dispositivos
Mediciones enviadas exitosamente al cloud
```

### 3. Verificar Inserción en Base de Datos

```bash
curl http://TU_IP_CLOUD:8080/api/energia/ultimo-registro/192.168.2.221
```

## Ajustar Registros Modbus

Si los valores leídos son incorrectos o cero, necesitas ajustar el mapeo de registros en `ModbusReaderService.java`.

### Pasos para identificar registros correctos:

1. Consulta el manual de tu dispositivo
2. Busca la tabla de registros Modbus
3. Actualiza las direcciones en el método `readDeviceData()`:

```java
// Ejemplo: Si voltage está en registro 100
data.setVoltage(readFloat32(master, slaveId, 100));
```

### Registros comunes por fabricante:

**SDM120/SDM630:**
- Voltage: registro 0
- Current: registro 6
- Power: registro 12

**PZEM-016:**
- Voltage: registro 0
- Current: registro 1
- Power: registro 3
- Energy: registro 5

**Eastron/Peacefair:**
- Voltage: registro 0
- Current: registro 6
- Active Power: registro 12
- Reactive Power: registro 24

## Formato JSON Enviado al Cloud

```json
[
  {
    "deviceIp": "192.168.2.221",
    "voltage": 239.0,
    "frequency": 50.0,
    "current": 9.581,
    "activePower": 2282.0,
    "apparentPower": 2292.0,
    "reactivePower": 209.0,
    "powerFactor": 1.00,
    "energyToday": 11.063,
    "energyYesterday": 1.177,
    "energyTotal": 12.240,
    "address": 1,
    "timestamp": "2025-12-09T10:30:15"
  },
  ...
]
```

## Troubleshooting Rápido

| Problema | Solución |
|----------|----------|
| Error de conexión Modbus | Verificar IP, puerto, firewall |
| Valores en cero | Ajustar registros Modbus |
| Error al enviar a cloud | Verificar URL y puerto cloud |
| Cloud no recibe datos | Verificar firewall, logs cloud |
| BD no se actualiza | Verificar conexión BD, logs |

## Configuración Avanzada

### Cambiar intervalo de lectura dinámicamente

Editar `application.properties` y reiniciar:
```properties
# Cada 30 segundos
modbus.polling.interval=30000

# Cada 5 segundos
modbus.polling.interval=5000
```

### Agregar/Quitar dispositivos

Editar `application.properties` y reiniciar:
```properties
# Agregar dispositivo 192.168.2.200
modbus.devices=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163,192.168.2.200
```

### Deshabilitar scheduler de EweLink

Si solo quieres usar Modbus, comenta el método en `MsConcentradorEnergiaApplication.java`:
```java
// @Scheduled(fixedRate = 15000)
// public void getProductObjects() {
//     ...
// }
```

## Soporte

Para más detalles, consulta `README_MODBUS.md`
