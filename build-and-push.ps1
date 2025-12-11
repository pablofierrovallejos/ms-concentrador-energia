# Script para compilar, construir imagen Docker y subir a Docker Hub
# Windows PowerShell

# Configuración
$DOCKER_USER = "pablofierro"  # CAMBIAR por tu usuario de Docker Hub
$VERSION = "1.0.0"
$IMAGE_NAME = "ms-concentrador-energia"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Docker Build & Push - MS Concentrador" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue
if (-not $dockerInstalled) {
    Write-Host "❌ Docker no está instalado" -ForegroundColor Red
    exit 1
}

Write-Host "Usuario Docker Hub: $DOCKER_USER" -ForegroundColor Yellow
Write-Host "Imagen: $IMAGE_NAME" -ForegroundColor Yellow
Write-Host "Versión: $VERSION" -ForegroundColor Yellow
Write-Host ""

# Paso 1: Compilar proyecto
Write-Host "🔨 [1/4] Compilando proyecto con Maven..." -ForegroundColor Cyan
.\mvnw.cmd clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error en la compilación" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Compilación exitosa" -ForegroundColor Green
Write-Host ""

# Paso 2: Login (si es necesario)
Write-Host "🔐 [2/4] Verificando login en Docker Hub..." -ForegroundColor Cyan
$dockerInfo = docker info 2>&1
if ($dockerInfo -notmatch "Username: $DOCKER_USER") {
    Write-Host "Por favor ingresa tus credenciales de Docker Hub:" -ForegroundColor Yellow
    docker login
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Error en login" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✅ Autenticado" -ForegroundColor Green
Write-Host ""

# Paso 3: Construir imágenes
Write-Host "🐳 [3/4] Construyendo imágenes Docker..." -ForegroundColor Cyan

Write-Host "   Construyendo versión $VERSION..." -ForegroundColor Yellow
docker build -t ${DOCKER_USER}/${IMAGE_NAME}:${VERSION} .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error construyendo imagen" -ForegroundColor Red
    exit 1
}

Write-Host "   Construyendo latest..." -ForegroundColor Yellow
docker build -t ${DOCKER_USER}/${IMAGE_NAME}:latest .

Write-Host "✅ Imágenes construidas" -ForegroundColor Green
Write-Host ""

# Paso 4: Subir a Docker Hub
Write-Host "☁️ [4/4] Subiendo imágenes a Docker Hub..." -ForegroundColor Cyan

Write-Host "   Subiendo versión $VERSION..." -ForegroundColor Yellow
docker push ${DOCKER_USER}/${IMAGE_NAME}:${VERSION}

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Error subiendo imagen" -ForegroundColor Red
    exit 1
}

Write-Host "   Subiendo latest..." -ForegroundColor Yellow
docker push ${DOCKER_USER}/${IMAGE_NAME}:${VERSION}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "✅ Completado exitosamente!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📦 Imágenes disponibles en Docker Hub:" -ForegroundColor Yellow
Write-Host "   - ${DOCKER_USER}/${IMAGE_NAME}:${VERSION}"
Write-Host "   - ${DOCKER_USER}/${IMAGE_NAME}:latest"
Write-Host ""
Write-Host "🚀 Para usar la imagen:" -ForegroundColor Yellow
Write-Host "   docker pull ${DOCKER_USER}/${IMAGE_NAME}:latest"
Write-Host ""
