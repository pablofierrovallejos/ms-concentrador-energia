# 🚀 BUILD RÁPIDO - Solo 2 minutos

## Paso 1: Compilar JAR (1-2 minutos)
```bash
.\mvnw.cmd clean package -DskipTests
```

## Paso 2: Crear imagen con Dockerfile simple (30 segundos)
```bash
# Renombrar Dockerfile actual (es muy lento)
Move-Item Dockerfile Dockerfile.multistage

# Usar Dockerfile simple
Move-Item Dockerfile.simple Dockerfile

# Construir imagen LOCAL
docker build -t pablofierrovallejos/ms-concentrador-energia:local .

# Construir imagen CLOUD
docker build -t pablofierrovallejos/ms-concentrador-energia:cloud .
```

## Paso 3: Subir a Docker Hub (opcional)
```bash
# Login
docker login

# Push LOCAL
docker push pablofierrovallejos/ms-concentrador-energia:local

# Push CLOUD
docker push pablofierrovallejos/ms-concentrador-energia:cloud
```

## Paso 4: Ejecutar
```bash
# LOCAL
docker-compose -f docker-compose-local.yml up -d

# CLOUD
docker-compose -f docker-compose-cloud.yml up -d
```

---

## ⚡ Script automatizado (Windows):

```powershell
# Compilar
.\mvnw.cmd clean package -DskipTests

# Construir imágenes
docker build -t pablofierrovallejos/ms-concentrador-energia:local .
docker build -t pablofierrovallejos/ms-concentrador-energia:cloud .

# Subir a Docker Hub
docker login
docker push pablofierrovallejos/ms-concentrador-energia:local
docker push pablofierrovallejos/ms-concentrador-energia:cloud

Write-Host "✅ Imágenes creadas y subidas a Docker Hub" -ForegroundColor Green
```
