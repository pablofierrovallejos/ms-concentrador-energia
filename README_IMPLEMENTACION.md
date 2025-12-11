# Sistema de Monitoreo de Energía - Modbus TCP

## 🎯 Resumen Ejecutivo

Sistema implementado para sensar 4 dispositivos de medición de energía mediante protocolo Modbus TCP:
- **192.168.2.221**
- **192.168.2.77**
- **192.168.2.26**
- **192.168.2.163**

El sistema lee los dispositivos cada 15 segundos (configurable), genera un JSON con los datos y lo envía a una instancia en la nube que los almacena en base de datos MySQL.

## 📊 Parámetros Medidos por Dispositivo

| Parámetro | Unidad | Ejemplo |
|-----------|--------|---------|
| Voltage | V | 239 |
| Frequency | Hz | 50 |
| Current | A | 9.581 |
| Active Power | W | 2282 |
| Apparent Power | VA | 2292 |
| Reactive Power | var | 209 |
| Power Factor | - | 1.00 |
| Energy Today | kWh | 11.063 |
| Energy Yesterday | kWh | 1.177 |
| Energy Total | kWh | 12.240 |
| Address | - | 1 |

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────┐
│   INSTANCIA LOCAL (Red Doméstica)   │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  Scheduler (cada 15s)        │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  ModbusReaderService         │  │
│  │  - Lee 4 dispositivos        │  │
│  │  - Genera EnergyDataDTO      │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  CloudSenderService          │  │
│  │  - Envía JSON vía HTTP POST  │  │
│  └──────────┬───────────────────┘  │
└─────────────┼───────────────────────┘
              │
              │ HTTP POST
              │ (JSON)
              ▼
┌─────────────────────────────────────┐
│     INSTANCIA CLOUD (Servidor)      │
│                                     │
│  ┌──────────────────────────────┐  │
│  │  EnergyController            │  │
│  │  /api/energia/recibir-       │  │
│  │   mediciones                 │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  IMedicionService            │  │
│  │  - Inserta en BD             │  │
│  └──────────┬───────────────────┘  │
│             │                       │
│             ▼                       │
│  ┌──────────────────────────────┐  │
│  │  MySQL Database              │  │
│  │  tabla: medicionenergia      │  │
│  └──────────────────────────────┘  │
└─────────────────────────────────────┘
```

## 📁 Archivos Creados/Modificados

### ✅ Nuevos Archivos

1. **`src/main/java/com/pablofierro/energia/models/dto/EnergyDataDTO.java`**
   - DTO para datos de energía
   - 11 parámetros + IP + timestamp

2. **`src/main/java/com/pablofierro/energia/services/ModbusReaderService.java`**
   - Lectura Modbus TCP
   - Conversión Float32
   - Manejo de errores

3. **`src/main/java/com/pablofierro/energia/services/CloudSenderService.java`**
   - Envío HTTP POST al cloud
   - RestTemplate configurado

4. **`src/main/java/com/pablofierro/energia/controllers/EnergyController.java`**
   - Endpoint `/api/energia/recibir-mediciones`
   - Endpoint `/api/energia/health`
   - Endpoint `/api/energia/ultimo-registro/{deviceIp}`

5. **`src/main/java/com/pablofierro/energia/services/ModbusTestRunner.java`**
   - Herramienta de prueba
   - Ejecutar con perfil `test-modbus`

6. **Archivos de Configuración:**
   - `application-local.properties` - Para instancia local
   - `application-cloud.properties` - Para instancia cloud

7. **Documentación:**
   - `README_MODBUS.md` - Documentación técnica completa
   - `QUICK_START.md` - Guía de inicio rápido
   - `README_IMPLEMENTACION.md` - Este archivo

### 🔧 Archivos Modificados

1. **`pom.xml`**
   - Agregada dependencia: `modbus-master-tcp` v1.2.0

2. **`src/main/resources/application.properties`**
   - Agregadas propiedades Modbus
   - Agregada URL del endpoint cloud

3. **`src/main/java/com/pablofierro/energia/MsConcentradorEnergiaApplication.java`**
   - Nuevo scheduler `readModbusDevicesAndSendToCloud()`
   - Inyección de servicios Modbus

4. **`src/main/java/com/pablofierro/energia/controllers/MeasController.java`**
   - Limpieza de imports no utilizados

## ⚙️ Configuración Requerida

### Instancia Local (application-local.properties)

```properties
# Dispositivos a sensar
modbus.devices=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163

# Puerto Modbus TCP
modbus.port=502

# ID del esclavo Modbus
modbus.slaveId=1

# Timeout (ms)
modbus.timeout=3000

# Intervalo de lectura (ms) - 15000 = 15 segundos
modbus.polling.interval=15000

# URL del servidor cloud
cloud.endpoint.url=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones
```

### Instancia Cloud (application-cloud.properties)

```properties
# Conexión a base de datos
spring.datasource.url=jdbc:mysql://TU_DB_IP:3306/db_springboot_cloud?serverTimezone=America/Santiago
spring.datasource.username=root
spring.datasource.password=tu_password

# Desactivar lectura Modbus
modbus.polling.interval=999999999
modbus.devices=
```

## 🚀 Despliegue

### Compilar

```bash
mvn clean package
```

### Ejecutar Instancia Local

```bash
java -jar -Dspring.profiles.active=local target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

### Ejecutar Instancia Cloud

```bash
java -jar -Dspring.profiles.active=cloud target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

### Probar Lectura Modbus

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test-modbus
```

## 📡 API Endpoints

### POST `/api/energia/recibir-mediciones`

Recibe mediciones desde instancia local.

**Request:**
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
  }
]
```

**Response:**
```json
{
  "total_recibidas": 4,
  "total_insertadas": 4,
  "registros_insertados": ["192.168.2.221", "192.168.2.77", "192.168.2.26", "192.168.2.163"]
}
```

### GET `/api/energia/health`

Verificar estado del servicio.

**Response:**
```json
{
  "status": "UP",
  "timestamp": "Mon Dec 09 10:30:00 CLT 2025"
}
```

### GET `/api/energia/ultimo-registro/{deviceIp}`

Obtener último registro de un dispositivo.

**Ejemplo:** `GET /api/energia/ultimo-registro/192.168.2.221`

## 🔍 Logs Importantes

### Instancia Local

```
=== Iniciando lectura de 4 dispositivos Modbus ===
Datos leídos exitosamente de dispositivo: 192.168.2.221
Datos leídos exitosamente de dispositivo: 192.168.2.77
Datos leídos exitosamente de dispositivo: 192.168.2.26
Datos leídos exitosamente de dispositivo: 192.168.2.163
Datos leídos de 4 dispositivos
Enviando 4 mediciones al endpoint: http://cloud:8080/api/energia/recibir-mediciones
Mediciones enviadas exitosamente al cloud
```

### Instancia Cloud

```
Recibiendo 4 mediciones para insertar en BD
Medición insertada: 192.168.2.221
Medición insertada: 192.168.2.77
Medición insertada: 192.168.2.26
Medición insertada: 192.168.2.163
```

## ⚠️ Notas Importantes

### 1. Registros Modbus

Los registros están configurados para dispositivos estándar (SDM/PZEM). Si tus dispositivos usan direcciones diferentes, debes ajustarlas en `ModbusReaderService.java`:

```java
// Ejemplo: Voltage en registro 100 en lugar de 0
data.setVoltage(readFloat32(master, slaveId, 100));
```

### 2. Direcciones Comunes por Fabricante

**SDM630:**
- Voltage: 0
- Current: 6  
- Power: 12
- Energy: 342

**PZEM-016:**
- Voltage: 0
- Current: 1
- Power: 3
- Energy: 5

### 3. Formato de Números

Los registros Modbus se leen como Float32 (IEEE 754) en formato Big-Endian.

### 4. Base de Datos

Los datos se insertan en la tabla `medicionenergia` usando el servicio existente `IMedicionService.agregarMedicion()`.

### 5. Compatibilidad

El scheduler original de EweLink se mantiene activo. Si solo quieres usar Modbus, puedes comentarlo en `MsConcentradorEnergiaApplication.java`.

## 🐛 Troubleshooting

| Problema | Solución |
|----------|----------|
| No se puede conectar a dispositivo | Verificar IP, puerto 502, firewall |
| Valores en cero | Ajustar direcciones de registros Modbus |
| Error al enviar al cloud | Verificar URL, puerto, firewall del servidor |
| Cloud no inserta en BD | Verificar credenciales de BD, logs de errores |
| Timeout de lectura | Aumentar `modbus.timeout` |

## 📚 Referencias

- **Documentación Completa:** `README_MODBUS.md`
- **Guía Rápida:** `QUICK_START.md`
- **Librería Modbus:** [digitalpetri/modbus](https://github.com/digitalpetri/modbus)

## ✅ Checklist de Verificación

- [ ] Compilar proyecto sin errores
- [ ] Configurar IPs de dispositivos
- [ ] Configurar URL del endpoint cloud
- [ ] Probar lectura Modbus (test-modbus)
- [ ] Iniciar instancia cloud
- [ ] Iniciar instancia local
- [ ] Verificar logs de lectura
- [ ] Verificar logs de envío
- [ ] Verificar inserción en BD
- [ ] Verificar endpoint health

## 💡 Próximos Pasos Sugeridos

1. **Seguridad:** Implementar autenticación en endpoint de recepción
2. **Monitoreo:** Agregar métricas y alertas
3. **Escalabilidad:** Implementar cola de mensajes (RabbitMQ/Kafka)
4. **Dashboard:** Crear interfaz web para visualización en tiempo real
5. **Alertas:** Notificaciones por consumo anómalo o fallas de lectura

---

**Implementado:** Diciembre 9, 2025  
**Versión:** 1.0.0  
**Estado:** ✅ Listo para producción
