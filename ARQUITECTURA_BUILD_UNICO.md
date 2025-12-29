# Arquitectura de Build Único con Perfiles

## Problema Anterior
- Se creaban dos builds diferentes: `latest` (cloud) y `localv1` (local)
- Confusión sobre qué build usar en cada servidor
- Duplicación de esfuerzo al mantener dos imágenes

## Solución: Build Único con Perfiles de Spring

### Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│              IMAGEN ÚNICA: latest                            │
│         96552333aa/ms-concentrador-energia:latest           │
└─────────────────────────────────────────────────────────────┘
                           │
                           │
          ┌────────────────┴────────────────┐
          │                                  │
          ▼                                  ▼
┌──────────────────────┐         ┌──────────────────────┐
│   INSTANCIA LOCAL    │         │   INSTANCIA CLOUD    │
│  (Red local 192.x)   │         │  (Servidor GCP)      │
├──────────────────────┤         ├──────────────────────┤
│ Profile: local       │         │ Profile: cloud       │
│ Puerto: 8080         │         │ Puerto: 8002         │
│                      │         │                      │
│ FUNCIÓN:             │         │ FUNCIÓN:             │
│ • Lee sensores       │──HTTP──>│ • Recibe mediciones  │
│ • Lee inversor       │         │ • Inserta en BD      │
│ • Lee temperatura    │         │ • WebSocket          │
│ • Envía al cloud     │         │ • API REST           │
└──────────────────────┘         └──────────────────────┘
```

## Proceso de Build y Deploy

### 1. Build Único

```bash
# Compilar
./mvnw clean package -DskipTests

# Crear imagen única
docker build -t 96552333aa/ms-concentrador-energia:latest .

# Subir a Docker Hub
docker push 96552333aa/ms-concentrador-energia:latest
```

**Una sola imagen sirve para ambos servidores.**

### 2. Deploy en LOCAL (Red 192.168.2.x)

**docker-compose-local.yml**
```yaml
services:
  ms-concentrador-local:
    image: 96552333aa/ms-concentrador-energia:latest  # ← MISMA IMAGEN
    environment:
      - SPRING_PROFILES_ACTIVE=local  # ← PROFILE LOCAL
      - CLOUD_ENDPOINT_URL=http://35.209.63.29:8002/api/energia/recibir-mediciones
      - MODBUS_DEVICES=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163
      - SOLIS_INVERTER_IP=192.168.2.72
      - TEMPERATURE_NODE_T110_IP=192.168.2.110
    ports:
      - "8080:8080"
```

**Comandos:**
```bash
docker-compose -f docker-compose-local.yml pull
docker-compose -f docker-compose-local.yml up -d
docker logs -f ms-concentrador-local
```

### 3. Deploy en CLOUD (GCP)

**docker-compose-cloud.yml**
```yaml
services:
  ms-concentrador-cloud:
    image: 96552333aa/ms-concentrador-energia:latest  # ← MISMA IMAGEN
    environment:
      - SPRING_PROFILES_ACTIVE=cloud  # ← PROFILE CLOUD
      - SPRING_DATASOURCE_URL=jdbc:mysql://10.128.0.3:3306/db_springboot_cloud
      - SERVER_PORT=8002
      - MODBUS_DEVICES=  # Vacío (no lee sensores)
    ports:
      - "8002:8002"
```

**Comandos:**
```bash
docker-compose -f docker-compose-cloud.yml pull
docker-compose -f docker-compose-cloud.yml up -d
docker logs -f ms-concentrador-energia
```

## Diferencias entre Perfiles

| Característica | LOCAL | CLOUD |
|---|---|---|
| **Spring Profile** | `local` | `cloud` |
| **Puerto** | 8080 | 8002 |
| **Base de Datos** | ❌ Deshabilitada | ✅ MySQL en GCP |
| **Lee Sensores** | ✅ Sí (cada 30s) | ❌ No |
| **Lee Inversor** | ✅ Sí (192.168.2.72) | ❌ No |
| **Lee Temperatura** | ✅ Sí (192.168.2.110) | ❌ No |
| **Envía a Cloud** | ✅ Sí | ❌ No aplica |
| **Recibe HTTP** | ❌ No | ✅ Sí (`/recibir-mediciones`) |
| **WebSocket** | ❌ No | ✅ Sí |
| **Controller activo** | ❌ No | ✅ `EnergyController` |

## Configuración por Variables de Entorno

### Variables Obligatorias en LOCAL

```bash
SPRING_PROFILES_ACTIVE=local
CLOUD_ENDPOINT_URL=http://35.209.63.29:8002/api/energia/recibir-mediciones
MODBUS_DEVICES=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163
SOLIS_INVERTER_IP=192.168.2.72
TEMPERATURE_NODE_T110_IP=192.168.2.110
TEMPERATURE_NODE_T110_ENABLED=true
```

### Variables Obligatorias en CLOUD

```bash
SPRING_PROFILES_ACTIVE=cloud
SPRING_DATASOURCE_URL=jdbc:mysql://10.128.0.3:3306/db_springboot_cloud
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=sasa
SERVER_PORT=8002
MODBUS_DEVICES=  # Vacío para desactivar lectura
```

## Flujo de Datos

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Sensores    │     │   Inversor   │     │ Nodo Temp.   │
│  Tasmota     │     │   Solar      │     │   T110       │
│  192.168.2.x │     │  192.168.2.72│     │ 192.168.2.110│
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                     │
       │ HTTP GET           │ HTTP GET            │ HTTP GET
       │ /cm?cmnd=Status+8  │ /status.html        │ /api/simple
       │                    │                     │
       ▼                    ▼                     ▼
┌─────────────────────────────────────────────────────────┐
│         INSTANCIA LOCAL (Profile: local)                │
│         TasmotaReaderService                            │
│         - readDeviceData()                              │
│         - readSolarInverterData()                       │
│         - readTemperatureNode()                         │
└─────────────────────┬───────────────────────────────────┘
                      │
                      │ HTTP POST (cada 30s)
                      │ /api/energia/recibir-mediciones
                      │ Body: [EnergyDataDTO, EnergyDataDTO, ...]
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│         INSTANCIA CLOUD (Profile: cloud)                │
│         EnergyController.recibirMediciones()            │
│         - Detecta tipo (energía/temperatura)            │
│         - Inserta en BD correspondiente                 │
│         - Trigger WebSocket notification                │
└─────────────────────┬───────────────────────────────────┘
                      │
           ┌──────────┴──────────┐
           │                     │
           ▼                     ▼
┌──────────────────┐   ┌──────────────────┐
│ medicionenergia  │   │medicion_temperatura│
│ (energía)        │   │ (temperatura)    │
└──────────────────┘   └──────────────────┘
           │                     │
           └──────────┬──────────┘
                      │
                      ▼
         ┌────────────────────────┐
         │ WebSocket Notifications│
         │ /topic/estadistica/T*  │
         └────────────────────────┘
                      │
                      ▼
                  Frontend
```

## Checklist de Deploy

### Cuando Haces Cambios en el Código

1. ✅ Compilar y construir imagen única:
   ```bash
   ./mvnw clean package -DskipTests
   docker build -t 96552333aa/ms-concentrador-energia:latest .
   docker push 96552333aa/ms-concentrador-energia:latest
   ```

2. ✅ Actualizar AMBOS servidores (usan la misma imagen):
   
   **En servidor LOCAL:**
   ```bash
   ssh usuario@servidor-local
   cd /ruta/proyecto
   docker-compose -f docker-compose-local.yml pull
   docker-compose -f docker-compose-local.yml up -d
   docker logs -f ms-concentrador-local
   ```
   
   **En servidor CLOUD:**
   ```bash
   ssh usuario@servidor-cloud
   cd /ruta/proyecto
   docker-compose -f docker-compose-cloud.yml pull
   docker-compose -f docker-compose-cloud.yml up -d
   docker logs -f ms-concentrador-energia
   ```

## Verificación

### En LOCAL debe mostrar:
```
INFO: === Iniciando lectura de 4 dispositivos Tasmota ===
INFO: Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.221
INFO: Leyendo datos del inversor Solis Datalogger en 192.168.2.72
INFO: Leyendo datos de temperatura del nodo T110 en 192.168.2.110
INFO: Enviando 6 mediciones al endpoint: http://35.209.63.29:8002/api/energia/recibir-mediciones
INFO: Mediciones enviadas exitosamente al cloud
```

### En CLOUD debe mostrar:
```
INFO: Recibiendo 6 mediciones para insertar en BD
INFO: Medición de energía insertada: T221 - Power: 2166.0W, Energy: 343.612kWh
INFO: Medición de energía insertada: T72 - Power: 3190.0W, Energy: 12372.0kWh
INFO: Medición de temperatura insertada: T110 - 28.25°C
```

## Solución de Problemas

### "Connection refused" en LOCAL
- ❌ **Incorrecto**: `CLOUD_ENDPOINT_URL=http://localhost:8080`
- ✅ **Correcto**: `CLOUD_ENDPOINT_URL=http://35.209.63.29:8002/api/energia/recibir-mediciones`

### "EnergyController" activo en LOCAL
- Verificar: `SPRING_PROFILES_ACTIVE=local` en docker-compose-local.yml
- El controller tiene `@Profile("cloud")` y NO debe activarse en local

### Error de BD en LOCAL
- Verificar que application-local.properties tiene:
  ```properties
  spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
  ```

### No se insertan datos en CLOUD
- Verificar BD: `SELECT * FROM medicionenergia ORDER BY fechameas DESC LIMIT 10;`
- Ver logs: `docker logs -f ms-concentrador-energia`
- Verificar stored procedures existen

## Resumen

✅ **Una sola imagen**: `96552333aa/ms-concentrador-energia:latest`  
✅ **Dos perfiles**: `local` y `cloud`  
✅ **Configuración por entorno**: Variables en docker-compose  
✅ **Deploy simple**: Pull y up en ambos servidores  
✅ **Sin confusión**: Mismo proceso para todos los cambios
