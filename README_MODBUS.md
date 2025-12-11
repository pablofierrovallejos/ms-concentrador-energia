# Sistema de Monitoreo de Energía con Dispositivos Modbus

## Descripción General

Este sistema permite sensar dispositivos de medición de energía mediante protocolo Modbus TCP, procesar los datos y enviarlos a una instancia en la nube para su almacenamiento en base de datos.

## Arquitectura

### Instancia Local (En red doméstica)
- Lee datos de 4 dispositivos Modbus cada 15 segundos (configurable)
- Dispositivos configurados:
  - 192.168.2.221
  - 192.168.2.77
  - 192.168.2.26
  - 192.168.2.163
- Genera JSON con los datos recopilados
- Envía el JSON al endpoint de la instancia cloud

### Instancia Cloud (En servidor remoto)
- Recibe los datos JSON desde la instancia local
- Inserta los registros en la base de datos MySQL
- Proporciona endpoints para consultas

## Parámetros Medidos

Cada dispositivo proporciona los siguientes parámetros:
- **Voltage**: Voltaje (V)
- **Frequency**: Frecuencia (Hz)
- **Current**: Corriente (A)
- **Active Power**: Potencia Activa (W)
- **Apparent Power**: Potencia Aparente (VA)
- **Reactive Power**: Potencia Reactiva (var)
- **Power Factor**: Factor de Potencia
- **Energy Today**: Energía del día (kWh)
- **Energy Yesterday**: Energía de ayer (kWh)
- **Energy Total**: Energía total acumulada (kWh)
- **Address**: Dirección Modbus del dispositivo

## Configuración

### application.properties

```properties
# Configuración de dispositivos Modbus
modbus.devices=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163
modbus.port=502
modbus.slaveId=1
modbus.timeout=3000
modbus.polling.interval=15000

# Endpoint para enviar mediciones (configurar según instancia cloud)
cloud.endpoint.url=http://cloud-server-ip:port/api/energia/recibir-mediciones
```

### Parámetros Configurables

- **modbus.devices**: Lista de IPs separadas por comas de los dispositivos a monitorear
- **modbus.port**: Puerto Modbus TCP (default: 502)
- **modbus.slaveId**: ID del esclavo Modbus (default: 1)
- **modbus.timeout**: Timeout de conexión en milisegundos (default: 3000)
- **modbus.polling.interval**: Intervalo de lectura en milisegundos (default: 15000 = 15 segundos)
- **cloud.endpoint.url**: URL del endpoint en la instancia cloud

## Componentes Implementados

### 1. EnergyDataDTO
DTO (Data Transfer Object) que representa los datos de energía de un dispositivo.

### 2. ModbusReaderService
Servicio encargado de:
- Establecer conexión Modbus TCP con los dispositivos
- Leer registros Holding (función 03)
- Convertir valores Float32 desde registros
- Gestionar errores y timeouts

**Nota**: Los registros Modbus (direcciones) están configurados para dispositivos típicos de medición de energía (SDM/PZEM). Si tus dispositivos usan diferentes direcciones, ajusta los valores en el método `readDeviceData()`.

### 3. CloudSenderService
Servicio que:
- Envía los datos al endpoint cloud mediante HTTP POST
- Maneja errores de comunicación
- Proporciona logging de transacciones

### 4. EnergyController
Controlador REST con los siguientes endpoints:

#### POST `/api/energia/recibir-mediciones`
Recibe mediciones desde la instancia local e inserta en BD.

**Request Body** (ejemplo):
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
    "timestamp": "2025-12-09T10:30:00"
  }
]
```

**Response** (ejemplo):
```json
{
  "total_recibidas": 4,
  "total_insertadas": 4,
  "registros_insertados": ["192.168.2.221", "192.168.2.77", "192.168.2.26", "192.168.2.163"]
}
```

#### GET `/api/energia/health`
Verifica el estado del servicio.

#### GET `/api/energia/ultimo-registro/{deviceIp}`
Obtiene el último registro de un dispositivo específico.

### 5. MsConcentradorEnergiaApplication
Clase principal con:
- Scheduler que ejecuta cada 15 segundos (configurable)
- Lee todos los dispositivos configurados
- Envía datos al cloud

## Flujo de Operación

1. **Inicio del Scheduler** (cada 15 segundos por defecto)
2. **Lectura de Dispositivos**:
   - Se obtiene la lista de IPs desde configuración
   - Se conecta a cada dispositivo vía Modbus TCP
   - Se leen los registros de energía
3. **Generación de JSON**:
   - Los datos se empaquetan en objetos EnergyDataDTO
   - Se genera timestamp automático
4. **Envío al Cloud**:
   - Se hace POST al endpoint configurado
   - Se registra el resultado en logs
5. **Recepción en Cloud**:
   - El endpoint recibe el JSON
   - Convierte cada medición a entidad de BD
   - Inserta en tabla `medicionenergia`

## Despliegue

### Instancia Local

1. Configurar `application.properties` con:
   - IPs de los dispositivos Modbus locales
   - URL del endpoint cloud
2. Compilar: `mvn clean package`
3. Ejecutar: `java -jar ms-concentrador-energia-0.0.1-SNAPSHOT.jar`

### Instancia Cloud

1. Configurar `application.properties` con:
   - Datos de conexión a base de datos cloud
   - Dejar `cloud.endpoint.url` apuntando a localhost (no se usa en cloud)
2. Desplegar en servidor cloud
3. Asegurar que el puerto esté abierto para recibir conexiones desde la red local

## Endpoints Existentes (Mantenidos)

Los endpoints originales se mantienen funcionales:
- `/consultar-measures/{snodo}/{sfecha}`
- `/consultar-estadistica/{snodo}/{sfecha}`
- `/consultar-consumo-mes/{snodo}/{sfecha}`
- `/consultar-consumo-mes2/{snodo}/{sfecha}`

## Dependencias Agregadas

```xml
<dependency>
    <groupId>com.infiniteautomation</groupId>
    <artifactId>modbus4j</artifactId>
    <version>3.0.3</version>
</dependency>
```

## Logs

El sistema genera logs detallados:
- Inicio de lectura de dispositivos
- Éxito/error en lectura de cada dispositivo
- Envío de datos al cloud
- Recepción e inserción en BD

## Troubleshooting

### Los dispositivos no responden
- Verificar conectividad de red con ping
- Verificar puerto Modbus (default 502) esté abierto
- Aumentar timeout en configuration

### Error al enviar al cloud
- Verificar URL del endpoint cloud
- Verificar firewall/puerto abierto en cloud
- Revisar logs para detalles del error

### Registros incorrectos en BD
- Verificar direcciones de registros Modbus en `ModbusReaderService`
- Consultar documentación del fabricante del dispositivo
- Ajustar mappings de registros según dispositivo

## Notas Importantes

⚠️ **Direcciones de Registros Modbus**: Las direcciones configuradas son para dispositivos estándar SDM/PZEM. Si tus dispositivos usan un mapa de registros diferente, debes ajustarlos en `ModbusReaderService.java`.

⚠️ **Seguridad**: Considera implementar autenticación en el endpoint `/api/energia/recibir-mediciones` para producción.

⚠️ **Escalabilidad**: Para más de 10-20 dispositivos, considera implementar un sistema de cola (RabbitMQ/Kafka) para el envío de datos.
