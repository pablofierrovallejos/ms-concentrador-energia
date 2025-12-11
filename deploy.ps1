# Script de despliegue para Windows PowerShell
# ms-concentrador-energia

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "MS Concentrador Energía - Deployment" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
$dockerInstalled = Get-Command docker -ErrorAction SilentlyContinue
if (-not $dockerInstalled) {
    Write-Host "❌ Docker no está instalado. Por favor instala Docker Desktop primero." -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker está instalado" -ForegroundColor Green
Write-Host ""

# Menú
Write-Host "Selecciona el tipo de despliegue:" -ForegroundColor Yellow
Write-Host "1) LOCAL - Lee dispositivos Tasmota y envía a cloud"
Write-Host "2) CLOUD - Recibe mediciones y guarda en BD"
Write-Host "3) Detener LOCAL"
Write-Host "4) Detener CLOUD"
Write-Host "5) Ver logs LOCAL"
Write-Host "6) Ver logs CLOUD"
Write-Host "7) Reconstruir LOCAL"
Write-Host "8) Reconstruir CLOUD"
Write-Host ""

$option = Read-Host "Opción (1-8)"

switch ($option) {
    "1" {
        Write-Host ""
        Write-Host "📦 Desplegando INSTANCIA LOCAL..." -ForegroundColor Cyan
        $cloudIp = Read-Host "Ingresa la IP del servidor CLOUD (ej: 35.209.63.29)"
        
        if ([string]::IsNullOrEmpty($cloudIp)) {
            Write-Host "❌ IP del cloud no puede estar vacía" -ForegroundColor Red
            exit 1
        }
        
        Write-Host "🚀 Construyendo y ejecutando..." -ForegroundColor Yellow
        docker-compose -f docker-compose-local.yml up -d --build
        
        Write-Host ""
        Write-Host "✅ Instancia LOCAL desplegada" -ForegroundColor Green
        Write-Host "📊 Ver logs: docker-compose -f docker-compose-local.yml logs -f" -ForegroundColor Yellow
        Write-Host "🔍 Verificar: docker logs -f ms-concentrador-local" -ForegroundColor Yellow
    }
    
    "2" {
        Write-Host ""
        Write-Host "📦 Desplegando INSTANCIA CLOUD..." -ForegroundColor Cyan
        $useMysql = Read-Host "¿Usar MySQL en Docker? (s/n)"
        
        if ($useMysql -eq "s" -or $useMysql -eq "S") {
            Write-Host "🚀 Construyendo y ejecutando (con MySQL)..." -ForegroundColor Yellow
            docker-compose -f docker-compose-cloud.yml up -d --build
        } else {
            $mysqlIp = Read-Host "Ingresa IP de MySQL externo"
            $mysqlUser = Read-Host "Ingresa usuario MySQL"
            $mysqlPass = Read-Host "Ingresa password MySQL" -AsSecureString
            $mysqlPassPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPass))
            
            Write-Host "🚀 Construyendo y ejecutando (MySQL externo)..." -ForegroundColor Yellow
            docker build -t ms-concentrador-energia:cloud .
            docker run -d `
                --name ms-concentrador-cloud `
                -p 8080:8080 `
                -e SPRING_PROFILES_ACTIVE=cloud `
                -e "SPRING_DATASOURCE_URL=jdbc:mysql://${mysqlIp}:3306/db_springboot_cloud" `
                -e "SPRING_DATASOURCE_USERNAME=$mysqlUser" `
                -e "SPRING_DATASOURCE_PASSWORD=$mysqlPassPlain" `
                ms-concentrador-energia:cloud
        }
        
        Write-Host ""
        Write-Host "✅ Instancia CLOUD desplegada" -ForegroundColor Green
        Write-Host "📊 Ver logs: docker logs -f ms-concentrador-cloud" -ForegroundColor Yellow
        Write-Host "🔍 Verificar: curl http://localhost:8080/api/energia/health" -ForegroundColor Yellow
    }
    
    "3" {
        Write-Host "🛑 Deteniendo INSTANCIA LOCAL..." -ForegroundColor Yellow
        docker-compose -f docker-compose-local.yml down
        Write-Host "✅ Detenida" -ForegroundColor Green
    }
    
    "4" {
        Write-Host "🛑 Deteniendo INSTANCIA CLOUD..." -ForegroundColor Yellow
        docker-compose -f docker-compose-cloud.yml down
        Write-Host "✅ Detenida" -ForegroundColor Green
    }
    
    "5" {
        Write-Host "📊 Logs INSTANCIA LOCAL:" -ForegroundColor Cyan
        docker-compose -f docker-compose-local.yml logs -f
    }
    
    "6" {
        Write-Host "📊 Logs INSTANCIA CLOUD:" -ForegroundColor Cyan
        docker-compose -f docker-compose-cloud.yml logs -f
    }
    
    "7" {
        Write-Host "🔄 Reconstruyendo INSTANCIA LOCAL..." -ForegroundColor Yellow
        docker-compose -f docker-compose-local.yml down
        docker-compose -f docker-compose-local.yml up -d --build
        Write-Host "✅ Reconstruida" -ForegroundColor Green
    }
    
    "8" {
        Write-Host "🔄 Reconstruyendo INSTANCIA CLOUD..." -ForegroundColor Yellow
        docker-compose -f docker-compose-cloud.yml down
        docker-compose -f docker-compose-cloud.yml up -d --build
        Write-Host "✅ Reconstruida" -ForegroundColor Green
    }
    
    default {
        Write-Host "❌ Opción inválida" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Operación completada" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
