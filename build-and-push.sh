#!/bin/bash
# Script para compilar, construir imagen Docker y subir a Docker Hub

# Configuración
DOCKER_USER="pablofierro"  # CAMBIAR por tu usuario de Docker Hub
VERSION="1.0.0"
IMAGE_NAME="ms-concentrador-energia"

echo "========================================="
echo "Docker Build & Push - MS Concentrador"
echo "========================================="
echo ""

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado"
    exit 1
fi

echo "Usuario Docker Hub: $DOCKER_USER"
echo "Imagen: $IMAGE_NAME"
echo "Versión: $VERSION"
echo ""

# Paso 1: Compilar proyecto
echo "🔨 [1/4] Compilando proyecto con Maven..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Error en la compilación"
    exit 1
fi

echo "✅ Compilación exitosa"
echo ""

# Paso 2: Login (si es necesario)
echo "🔐 [2/4] Verificando login en Docker Hub..."
if ! docker info | grep -q "Username: $DOCKER_USER"; then
    echo "Por favor ingresa tus credenciales de Docker Hub:"
    docker login
    if [ $? -ne 0 ]; then
        echo "❌ Error en login"
        exit 1
    fi
fi

echo "✅ Autenticado"
echo ""

# Paso 3: Construir imágenes
echo "🐳 [3/4] Construyendo imágenes Docker..."

echo "   Construyendo versión $VERSION..."
docker build -t $DOCKER_USER/$IMAGE_NAME:$VERSION .

if [ $? -ne 0 ]; then
    echo "❌ Error construyendo imagen"
    exit 1
fi

echo "   Construyendo latest..."
docker build -t $DOCKER_USER/$IMAGE_NAME:latest .

echo "✅ Imágenes construidas"
echo ""

# Paso 4: Subir a Docker Hub
echo "☁️ [4/4] Subiendo imágenes a Docker Hub..."

echo "   Subiendo versión $VERSION..."
docker push $DOCKER_USER/$IMAGE_NAME:$VERSION

if [ $? -ne 0 ]; then
    echo "❌ Error subiendo imagen"
    exit 1
fi

echo "   Subiendo latest..."
docker push $DOCKER_USER/$IMAGE_NAME:latest

echo ""
echo "========================================="
echo "✅ Completado exitosamente!"
echo "========================================="
echo ""
echo "📦 Imágenes disponibles en Docker Hub:"
echo "   - $DOCKER_USER/$IMAGE_NAME:$VERSION"
echo "   - $DOCKER_USER/$IMAGE_NAME:latest"
echo ""
echo "🚀 Para usar la imagen:"
echo "   docker pull $DOCKER_USER/$IMAGE_NAME:latest"
echo ""
