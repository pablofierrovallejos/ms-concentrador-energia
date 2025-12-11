#!/bin/bash
# Script de despliegue para ms-concentrador-energia

echo "========================================="
echo "MS Concentrador Energía - Deployment"
echo "========================================="
echo ""

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor instala Docker primero."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose no está instalado. Por favor instala Docker Compose primero."
    exit 1
fi

echo "✅ Docker y Docker Compose están instalados"
echo ""

# Menú
echo "Selecciona el tipo de despliegue:"
echo "1) LOCAL - Lee dispositivos Tasmota y envía a cloud"
echo "2) CLOUD - Recibe mediciones y guarda en BD"
echo "3) Detener LOCAL"
echo "4) Detener CLOUD"
echo "5) Ver logs LOCAL"
echo "6) Ver logs CLOUD"
echo "7) Reconstruir LOCAL"
echo "8) Reconstruir CLOUD"
echo ""
read -p "Opción (1-8): " option

case $option in
    1)
        echo ""
        echo "📦 Desplegando INSTANCIA LOCAL..."
        read -p "Ingresa la IP del servidor CLOUD (ej: 35.209.63.29): " cloud_ip
        
        if [ -z "$cloud_ip" ]; then
            echo "❌ IP del cloud no puede estar vacía"
            exit 1
        fi
        
        # Actualizar docker-compose-local.yml
        sed -i "s|TU_IP_CLOUD|$cloud_ip|g" docker-compose-local.yml
        
        echo "🚀 Construyendo y ejecutando..."
        docker-compose -f docker-compose-local.yml up -d --build
        
        echo ""
        echo "✅ Instancia LOCAL desplegada"
        echo "📊 Ver logs: docker-compose -f docker-compose-local.yml logs -f"
        echo "🔍 Verificar: docker logs -f ms-concentrador-local"
        ;;
    
    2)
        echo ""
        echo "📦 Desplegando INSTANCIA CLOUD..."
        read -p "¿Usar MySQL en Docker? (s/n): " use_mysql
        
        if [ "$use_mysql" = "s" ] || [ "$use_mysql" = "S" ]; then
            echo "🚀 Construyendo y ejecutando (con MySQL)..."
            docker-compose -f docker-compose-cloud.yml up -d --build
        else
            read -p "Ingresa IP de MySQL externo: " mysql_ip
            read -p "Ingresa usuario MySQL: " mysql_user
            read -p "Ingresa password MySQL: " mysql_pass
            
            echo "🚀 Construyendo y ejecutando (MySQL externo)..."
            docker build -t ms-concentrador-energia:cloud .
            docker run -d \
                --name ms-concentrador-cloud \
                -p 8080:8080 \
                -e SPRING_PROFILES_ACTIVE=cloud \
                -e SPRING_DATASOURCE_URL=jdbc:mysql://$mysql_ip:3306/db_springboot_cloud \
                -e SPRING_DATASOURCE_USERNAME=$mysql_user \
                -e SPRING_DATASOURCE_PASSWORD=$mysql_pass \
                ms-concentrador-energia:cloud
        fi
        
        echo ""
        echo "✅ Instancia CLOUD desplegada"
        echo "📊 Ver logs: docker logs -f ms-concentrador-cloud"
        echo "🔍 Verificar: curl http://localhost:8080/api/energia/health"
        ;;
    
    3)
        echo "🛑 Deteniendo INSTANCIA LOCAL..."
        docker-compose -f docker-compose-local.yml down
        echo "✅ Detenida"
        ;;
    
    4)
        echo "🛑 Deteniendo INSTANCIA CLOUD..."
        docker-compose -f docker-compose-cloud.yml down
        echo "✅ Detenida"
        ;;
    
    5)
        echo "📊 Logs INSTANCIA LOCAL:"
        docker-compose -f docker-compose-local.yml logs -f
        ;;
    
    6)
        echo "📊 Logs INSTANCIA CLOUD:"
        docker-compose -f docker-compose-cloud.yml logs -f
        ;;
    
    7)
        echo "🔄 Reconstruyendo INSTANCIA LOCAL..."
        docker-compose -f docker-compose-local.yml down
        docker-compose -f docker-compose-local.yml up -d --build
        echo "✅ Reconstruida"
        ;;
    
    8)
        echo "🔄 Reconstruyendo INSTANCIA CLOUD..."
        docker-compose -f docker-compose-cloud.yml down
        docker-compose -f docker-compose-cloud.yml up -d --build
        echo "✅ Reconstruida"
        ;;
    
    *)
        echo "❌ Opción inválida"
        exit 1
        ;;
esac

echo ""
echo "========================================="
echo "Operación completada"
echo "========================================="
