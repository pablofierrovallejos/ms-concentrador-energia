# Guía para Compilar y Subir a Docker Hub

## 📦 Paso 1: Compilar el proyecto

```bash
# Windows PowerShell
.\mvnw.cmd clean package -DskipTests

# Linux/Mac
./mvnw clean package -DskipTests
```

Esto genera: `target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar`

---

## 🐳 Paso 2: Login en Docker Hub

```bash
docker login

# Ingresa tu usuario y password de Docker Hub
```

---

## 🏗️ Paso 3: Construir imágenes

### Imagen con compilación incluida (Multi-stage)

```bash
# Imagen para LOCAL
docker build -t tuusuario/ms-concentrador-energia:local \
  --build-arg SPRING_PROFILES_ACTIVE=local .

# Imagen para CLOUD
docker build -t tuusuario/ms-concentrador-energia:cloud \
  --build-arg SPRING_PROFILES_ACTIVE=cloud .

# O una imagen genérica
docker build -t tuusuario/ms-concentrador-energia:latest .
```

### Imagen con versión específica

```bash
docker build -t tuusuario/ms-concentrador-energia:1.0.0 .
docker build -t tuusuario/ms-concentrador-energia:latest .
```

---

## ☁️ Paso 4: Subir a Docker Hub

```bash
# Subir imagen local
docker push tuusuario/ms-concentrador-energia:local

# Subir imagen cloud
docker push tuusuario/ms-concentrador-energia:cloud

# Subir latest
docker push tuusuario/ms-concentrador-energia:latest

# Subir versión específica
docker push tuusuario/ms-concentrador-energia:1.0.0
```

---

## 🎯 Ejemplo Completo

Reemplaza `pablofierro` con tu usuario de Docker Hub:

```bash
# 1. Login
docker login

# 2. Compilar proyecto
.\mvnw.cmd clean package -DskipTests

# 3. Construir imágenes
docker build -t pablofierro/ms-concentrador-energia:local .
docker build -t pablofierro/ms-concentrador-energia:cloud .
docker build -t pablofierro/ms-concentrador-energia:latest .

# 4. Subir a Docker Hub
docker push pablofierro/ms-concentrador-energia:local
docker push pablofierro/ms-concentrador-energia:cloud
docker push pablofierro/ms-concentrador-energia:latest
```

---

## 🚀 Uso de la imagen desde Docker Hub

### LOCAL
```bash
docker run -d \
  --name ms-concentrador-local \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e MODBUS_DEVICES=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163 \
  -e CLOUD_ENDPOINT_URL=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones \
  pablofierro/ms-concentrador-energia:latest
```

### CLOUD
```bash
docker run -d \
  --name ms-concentrador-cloud \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=cloud \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/db_springboot_cloud \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=sasa \
  pablofierro/ms-concentrador-energia:latest
```

---

## 📝 Actualizar docker-compose para usar imagen de Docker Hub

### docker-compose-local.yml
```yaml
services:
  ms-concentrador-local:
    image: pablofierro/ms-concentrador-energia:latest
    # Eliminar sección "build"
    container_name: ms-concentrador-local
    environment:
      - SPRING_PROFILES_ACTIVE=local
      ...
```

### docker-compose-cloud.yml
```yaml
services:
  ms-concentrador-cloud:
    image: pablofierro/ms-concentrador-energia:latest
    # Eliminar sección "build"
    container_name: ms-concentrador-cloud
    environment:
      - SPRING_PROFILES_ACTIVE=cloud
      ...
```

---

## 🔄 Workflow completo de actualización

```bash
# 1. Hacer cambios en el código
# 2. Compilar
.\mvnw.cmd clean package -DskipTests

# 3. Incrementar versión y construir
docker build -t pablofierro/ms-concentrador-energia:1.0.1 .
docker build -t pablofierro/ms-concentrador-energia:latest .

# 4. Subir nueva versión
docker push pablofierro/ms-concentrador-energia:1.0.1
docker push pablofierro/ms-concentrador-energia:latest

# 5. En servidor, actualizar
docker pull pablofierro/ms-concentrador-energia:latest
docker-compose -f docker-compose-cloud.yml up -d
```

---

## 🏷️ Tags recomendados

```bash
# Por ambiente
pablofierro/ms-concentrador-energia:local
pablofierro/ms-concentrador-energia:cloud

# Por versión
pablofierro/ms-concentrador-energia:1.0.0
pablofierro/ms-concentrador-energia:1.0.1

# Latest
pablofierro/ms-concentrador-energia:latest

# Por fecha
pablofierro/ms-concentrador-energia:2025-12-09
```

---

## 🛠️ Script automatizado

### build-and-push.sh (Linux/Mac)
```bash
#!/bin/bash
DOCKER_USER="pablofierro"
VERSION="1.0.0"

echo "🔨 Compilando proyecto..."
./mvnw clean package -DskipTests

echo "🐳 Construyendo imágenes Docker..."
docker build -t $DOCKER_USER/ms-concentrador-energia:$VERSION .
docker build -t $DOCKER_USER/ms-concentrador-energia:latest .

echo "☁️ Subiendo a Docker Hub..."
docker push $DOCKER_USER/ms-concentrador-energia:$VERSION
docker push $DOCKER_USER/ms-concentrador-energia:latest

echo "✅ Completado!"
```

### build-and-push.ps1 (Windows)
```powershell
$DOCKER_USER = "pablofierro"
$VERSION = "1.0.0"

Write-Host "🔨 Compilando proyecto..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

Write-Host "🐳 Construyendo imágenes Docker..." -ForegroundColor Cyan
docker build -t ${DOCKER_USER}/ms-concentrador-energia:${VERSION} .
docker build -t ${DOCKER_USER}/ms-concentrador-energia:latest .

Write-Host "☁️ Subiendo a Docker Hub..." -ForegroundColor Cyan
docker push ${DOCKER_USER}/ms-concentrador-energia:${VERSION}
docker push ${DOCKER_USER}/ms-concentrador-energia:latest

Write-Host "✅ Completado!" -ForegroundColor Green
```

**Uso:**
```bash
# Windows
.\build-and-push.ps1

# Linux/Mac
chmod +x build-and-push.sh
./build-and-push.sh
```
