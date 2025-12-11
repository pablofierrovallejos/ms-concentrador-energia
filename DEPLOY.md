# Guía de Despliegue - Instancia Local y Cloud

## Arquitectura

Este microservicio se ejecuta en **dos modalidades**:

### 1. **Instancia LOCAL** (En tu red doméstica)
- Lee datos de 4 dispositivos Tasmota cada 15 segundos
- Genera JSON con las mediciones
- Envía datos al endpoint cloud vía HTTP POST

### 2. **Instancia CLOUD** (En servidor remoto)
- Recibe mediciones desde la instancia local
- Inserta registros en base de datos MySQL
- Expone endpoints REST para consultas

---

## Requisitos Previos

- Docker y Docker Compose instalados
- Java 17 (solo para desarrollo local sin Docker)
- Maven (solo para desarrollo local sin Docker)

---

## Despliegue INSTANCIA LOCAL

### Opción 1: Con Docker Compose (Recomendado)

1. **Editar configuración:**

Abrir `docker-compose-local.yml` y cambiar:

```yaml
- CLOUD_ENDPOINT_URL=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones
```

Por la IP real de tu servidor cloud, ejemplo:
```yaml
- CLOUD_ENDPOINT_URL=http://35.209.63.29:8080/api/energia/recibir-mediciones
```

2. **Construir y ejecutar:**

```bash
# Construir imagen
docker-compose -f docker-compose-local.yml build

# Ejecutar
docker-compose -f docker-compose-local.yml up -d

# Ver logs
docker-compose -f docker-compose-local.yml logs -f
```

3. **Verificar funcionamiento:**

```bash
# Ver logs en tiempo real
docker logs -f ms-concentrador-local

# Deberías ver:
# === Iniciando lectura de 4 dispositivos Tasmota ===
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.221
# ...
# Mediciones enviadas exitosamente al cloud
```

### Opción 2: Con Docker manual

```bash
# Construir imagen
docker build -t ms-concentrador-energia:local .

# Ejecutar
docker run -d \
  --name ms-concentrador-local \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e MODBUS_DEVICES=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163 \
  -e CLOUD_ENDPOINT_URL=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones \
  ms-concentrador-energia:local
```

### Opción 3: Sin Docker (JAR directo)

```bash
# Compilar
./mvnw clean package

# Ejecutar con perfil local
java -jar -Dspring.profiles.active=local target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

**Nota:** Antes de ejecutar, editar `src/main/resources/application-local.properties` con la IP del cloud.

---

## Despliegue INSTANCIA CLOUD

### Opción 1: Con Docker Compose (Recomendado)

1. **Subir archivos al servidor cloud:**

```bash
scp docker-compose-cloud.yml Dockerfile pom.xml src/ usuario@servidor-cloud:/ruta/
```

2. **En el servidor cloud, ejecutar:**

```bash
# Construir y ejecutar (incluye MySQL)
docker-compose -f docker-compose-cloud.yml up -d

# Ver logs
docker-compose -f docker-compose-cloud.yml logs -f
```

3. **Verificar funcionamiento:**

```bash
# Probar endpoint health
curl http://localhost:8080/api/energia/health

# Ver logs
docker logs -f ms-concentrador-cloud

# Deberías ver:
# Started MsConcentradorEnergiaApplication
# (NO verá logs de lectura de Tasmota, solo recibe datos)
```

### Opción 2: Con Docker manual + MySQL externo

Si ya tienes MySQL configurado:

```bash
# Construir imagen
docker build -t ms-concentrador-energia:cloud .

# Ejecutar
docker run -d \
  --name ms-concentrador-cloud \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://TU_DB_IP:3306/db_springboot_cloud \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=tu_password \
  ms-concentrador-energia:cloud
```

### Opción 3: Sin Docker (JAR directo)

```bash
# Compilar
./mvnw clean package

# Ejecutar con perfil cloud
java -jar -Dspring.profiles.active=cloud target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar
```

---

## Configuración de Variables de Entorno

### Instancia LOCAL

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `local` |
| `MODBUS_DEVICES` | IPs de dispositivos Tasmota | `192.168.2.221,192.168.2.77,...` |
| `MODBUS_POLLING_INTERVAL` | Intervalo de lectura (ms) | `15000` |
| `TASMOTA_TIMEOUT` | Timeout HTTP (ms) | `5000` |
| `CLOUD_ENDPOINT_URL` | URL del servidor cloud | `http://35.209.63.29:8080/api/energia/recibir-mediciones` |

### Instancia CLOUD

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `cloud` |
| `SPRING_DATASOURCE_URL` | URL de MySQL | `jdbc:mysql://mysql:3306/db_springboot_cloud` |
| `SPRING_DATASOURCE_USERNAME` | Usuario MySQL | `root` |
| `SPRING_DATASOURCE_PASSWORD` | Password MySQL | `sasa` |
| `MODBUS_DEVICES` | Vacío (no lee dispositivos) | `` |
| `MODBUS_POLLING_INTERVAL` | Muy alto (desactivado) | `999999999` |

---

## Comandos Útiles

### Docker Compose

```bash
# Ver logs en tiempo real
docker-compose -f docker-compose-local.yml logs -f
docker-compose -f docker-compose-cloud.yml logs -f

# Reiniciar servicios
docker-compose -f docker-compose-local.yml restart
docker-compose -f docker-compose-cloud.yml restart

# Detener y eliminar
docker-compose -f docker-compose-local.yml down
docker-compose -f docker-compose-cloud.yml down

# Reconstruir después de cambios
docker-compose -f docker-compose-local.yml up -d --build
docker-compose -f docker-compose-cloud.yml up -d --build
```

### Docker manual

```bash
# Ver logs
docker logs -f ms-concentrador-local
docker logs -f ms-concentrador-cloud

# Reiniciar
docker restart ms-concentrador-local
docker restart ms-concentrador-cloud

# Detener
docker stop ms-concentrador-local
docker stop ms-concentrador-cloud

# Eliminar
docker rm ms-concentrador-local
docker rm ms-concentrador-cloud
```

---

## Verificación de Funcionamiento

### Instancia LOCAL

```bash
# Ver si está leyendo dispositivos
docker logs -f ms-concentrador-local | grep "Tasmota"

# Debería mostrar:
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.221
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.77
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.26
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.163
```

### Instancia CLOUD

```bash
# Probar endpoint health
curl http://TU_IP_CLOUD:8080/api/energia/health

# Respuesta esperada:
# {"status":"UP","timestamp":"..."}

# Probar inserción manual
curl -X POST http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones \
  -H "Content-Type: application/json" \
  -d '[{"deviceIp":"192.168.2.221","voltage":239.0,"current":9.5,"activePower":2282.0}]'

# Ver logs de inserción
docker logs -f ms-concentrador-cloud | grep "Recibiendo"
```

---

## Troubleshooting

### Local no puede leer Tasmota

```bash
# Verificar red del contenedor
docker exec ms-concentrador-local ping -c 3 192.168.2.221

# Si no funciona, usar network_mode: "host" en docker-compose
```

### Local no puede enviar a Cloud

```bash
# Verificar conectividad
docker exec ms-concentrador-local curl http://TU_IP_CLOUD:8080/api/energia/health

# Verificar URL configurada
docker exec ms-concentrador-local env | grep CLOUD_ENDPOINT_URL
```

### Cloud no inserta en BD

```bash
# Verificar conexión MySQL
docker exec ms-concentrador-cloud mysql -h mysql -u root -psasa -e "SHOW DATABASES;"

# Ver logs de Hibernate
docker logs -f ms-concentrador-cloud | grep "HHH"
```

---

## Actualización de Código

Cuando hagas cambios en el código:

```bash
# Local
docker-compose -f docker-compose-local.yml down
docker-compose -f docker-compose-local.yml up -d --build

# Cloud
docker-compose -f docker-compose-cloud.yml down
docker-compose -f docker-compose-cloud.yml up -d --build
```

---

## Seguridad en Producción

### Recomendaciones:

1. **Cambiar contraseñas por defecto**
2. **Usar HTTPS** en el endpoint cloud
3. **Implementar autenticación** (API Key o JWT)
4. **Configurar firewall** para limitar acceso
5. **Usar secrets de Docker** para credenciales:

```yaml
secrets:
  db_password:
    file: ./db_password.txt

services:
  ms-concentrador-cloud:
    secrets:
      - db_password
    environment:
      - SPRING_DATASOURCE_PASSWORD_FILE=/run/secrets/db_password
```

---

## Arquitectura Final

```
┌─────────────────────────────────────┐
│  RED LOCAL (Tu casa)                │
│                                     │
│  ┌─────────────────────────────┐   │
│  │  Dispositivos Tasmota       │   │
│  │  - 192.168.2.221            │   │
│  │  - 192.168.2.77             │   │
│  │  - 192.168.2.26             │   │
│  │  - 192.168.2.163            │   │
│  └──────────▲───────────────────┘   │
│             │ HTTP                  │
│             │                       │
│  ┌──────────┴───────────────────┐   │
│  │  Instancia LOCAL             │   │
│  │  (Docker Container)          │   │
│  │  - Lee Tasmota cada 15s      │   │
│  │  - Genera JSON               │   │
│  └──────────┬───────────────────┘   │
│             │                       │
└─────────────┼───────────────────────┘
              │ HTTP POST (JSON)
              │ Internet
              ▼
┌─────────────────────────────────────┐
│  SERVIDOR CLOUD                     │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  Instancia CLOUD             │   │
│  │  (Docker Container)          │   │
│  │  - Recibe JSON               │   │
│  │  - Inserta en MySQL          │   │
│  └──────────┬───────────────────┘   │
│             │                       │
│  ┌──────────▼───────────────────┐   │
│  │  MySQL Database              │   │
│  │  (Docker Container)          │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## Soporte

Para más detalles:
- `README_MODBUS.md` - Documentación técnica
- `QUICK_START.md` - Guía rápida
- `IMPLEMENTACION_COMPLETADA.md` - Resumen de implementación
