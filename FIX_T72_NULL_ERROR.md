# Fix: Error al Insertar Datos del Inversor Solar T72

## Problema Identificado

El nodo T72 (inversor solar en 192.168.2.72) leía datos correctamente pero fallaba al insertarlos en la base de datos con error `null`.

### Análisis del Log
```
Local:
✓ Current power encontrado: 3190 W
✓ Yield today encontrado: 8.50 kWh  
✓ Total yield encontrado: 12372.0 kWh
✓ Datos del inversor Solis agregados a la lista

Cloud:
✗ Error insertando medición de 192.168.2.72: null
```

### Causa Raíz
El inversor solar solo proporciona:
- `activePower` (3190 W)
- `energyToday` (8.5 kWh)
- `energyTotal` (12372 kWh)

Pero NO tiene:
- `voltage` → null
- `current` → null

Cuando el código hacía `String.valueOf(null)`, producía la cadena `"null"`, causando error en el stored procedure `sp_insertarMedicionEnergia`.

## Solución Implementada

### 1. Validación de Valores Null en EnergyController

**Archivo**: `EnergyController.java`

**Cambio**: Validar campos null antes de convertir a String, usando "0.0" como valor por defecto:

```java
// Antes (causaba error):
String volts = String.valueOf(dto.getVoltage());    // Si null → "null"
String current = String.valueOf(dto.getCurrent());  // Si null → "null"

// Después (fix aplicado):
String volts = dto.getVoltage() != null ? String.valueOf(dto.getVoltage()) : "0.0";
String current = dto.getCurrent() != null ? String.valueOf(dto.getCurrent()) : "0.0";
String power = dto.getActivePower() != null ? String.valueOf(dto.getActivePower()) : "0.0";
String energy = dto.getEnergyTotal() != null ? String.valueOf(dto.getEnergyTotal()) : "0.0";
```

**Beneficio**: El inversor solar ahora insertará datos con `voltage=0.0` y `current=0.0`, pero con `power` y `energy` correctos.

### 2. Retornar null en Lecturas Fallidas

**Archivo**: `TasmotaReaderService.java`

**Cambio**: `readDeviceData()` ahora retorna `null` cuando falla la lectura, en lugar de un objeto con valores por defecto:

```java
// Antes:
return data; // Siempre retornaba objeto (incluso con error)

// Después:
if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
    // ... parsear datos ...
    return data; // Solo retorna si fue exitoso
}
return null; // Retorna null si hubo error
```

**Beneficio**: El nodo 192.168.2.26 (Connection refused) ya no se agregará a la lista de mediciones, evitando intentos de inserción inválidos.

### 3. Mejor Logging

Agregado logging más detallado para diagnóstico:

```java
logger.info("Medición de energía insertada: {} - Power: {}W, Energy: {}kWh", 
    nombrenodo, power, energy);
```

## Resultado Esperado

### Antes del Fix
```
Cloud Log:
✗ Error insertando medición de 192.168.2.26: null
✗ Error insertando medición de 192.168.2.72: null
```

### Después del Fix
```
Cloud Log:
✓ Medición de energía insertada: T72 - Power: 3190.0W, Energy: 12372.0kWh
(192.168.2.26 no se enviará porque la lectura falló)
```

## Pasos para Aplicar el Fix

### Opción 1: Recompilar y Redesplegar (Recomendado)

```bash
# 1. Compilar
./mvnw clean package -DskipTests

# 2. Construir imagen Docker
docker build -t 96552333aa/ms-concentrador-energia:latest .

# 3. Subir a Docker Hub
docker push 96552333aa/ms-concentrador-energia:latest

# 4. En el servidor cloud, actualizar
docker-compose down
docker-compose pull
docker-compose up -d

# 5. Ver logs para confirmar
docker logs -f <container_id>
```

### Opción 2: Hot Reload (Solo para pruebas rápidas)

Si tienes hot reload habilitado en desarrollo:
```bash
# Solo recompilar
./mvnw compile
```

## Verificación Post-Deploy

### 1. Verificar Logs del Microservicio Cloud

Buscar estos mensajes:
```
INFO c.p.e.controllers.EnergyController : Recibiendo 5 mediciones para insertar en BD
INFO c.p.e.controllers.EnergyController : Medición de energía insertada: T72 - Power: 3190.0W, Energy: 12372.0kWh
```

### 2. Verificar Base de Datos

```sql
-- Ver últimas mediciones del inversor
SELECT * FROM medicionenergia 
WHERE nombrenodo = 'T72' 
ORDER BY fechameas DESC 
LIMIT 10;

-- Debería mostrar registros con:
-- volts = 0.0
-- current = 0.0
-- power = 3190 (o valor actual)
-- energy = 12372 (o valor actual)
```

### 3. Verificar WebSocket

```sql
-- Ver notificaciones del nodo T72
SELECT * FROM websocket_notifications 
WHERE nombrenodo = 'T72' 
ORDER BY fecha_creacion DESC 
LIMIT 5;
```

## Casos de Uso Cubiertos

| Nodo | Tipo | Voltage | Current | Power | Energy | Estado |
|------|------|---------|---------|-------|--------|--------|
| T221 | Tasmota | ✓ | ✓ | ✓ | ✓ | ✅ OK |
| T77 | Tasmota | ✓ | ✓ | ✓ | ✓ | ✅ OK |
| T163 | Tasmota | ✓ | ✓ | ✓ | ✓ | ✅ OK |
| T72 | Solar | null→0.0 | null→0.0 | ✓ | ✓ | ✅ FIXED |
| T110 | Temp | temp | - | - | - | ✅ OK |
| T26 | Offline | - | - | - | - | ✅ Omitido |

## Notas Adicionales

### Por qué no crear tabla separada para T72

A diferencia de T110 (temperatura), el inversor T72 **sí mide energía**, solo que no mide voltage/current porque:
- Es un datalogger que reporta producción solar
- Los valores que importan son power y energy total
- El voltage/current del inversor no es relevante para el monitoreo

Por eso es correcto usar la tabla `medicionenergia` existente, simplemente con voltage=0.0 y current=0.0.

### Mejoras Futuras

Si se requiere distinguir visualmente entre:
- Medidores Tasmota (con voltage/current)
- Inversores solares (solo power/energy)

Se podría:
1. Agregar un campo `tipo_dispositivo` a la tabla
2. O usar un flag en el nombre del nodo (ej: "S72" para Solar)
3. O filtrar en el frontend basándose en voltage=0

## Archivos Modificados

```
src/main/java/com/pablofierro/energia/
    ├── controllers/EnergyController.java
    └── services/TasmotaReaderService.java
```

## Testing

Para probar el fix sin esperar al ciclo automático:

```bash
# En el microservicio local, revisar log cada 15 segundos
docker logs -f <local_container_id>

# Deberías ver:
# "Datos leídos del inversor solar: 192.168.2.72 - Power: XXXXw"
# "Mediciones enviadas exitosamente al cloud"

# En el microservicio cloud:
docker logs -f <cloud_container_id>

# Deberías ver:
# "Medición de energía insertada: T72 - Power: XXXXW, Energy: XXXXkWh"
```
