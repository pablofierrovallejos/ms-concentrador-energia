# ✅ RESUMEN DE DESPLIEGUE

## 📦 Archivos Creados

### Docker
- `Dockerfile` - Imagen multi-stage optimizada
- `docker-compose-local.yml` - Despliegue LOCAL (lee Tasmota)
- `docker-compose-cloud.yml` - Despliegue CLOUD (recibe y guarda)
- `.dockerignore` - Optimización de build

### Configuración
- `src/main/resources/application-local.properties` - Config LOCAL
- `src/main/resources/application-cloud.properties` - Config CLOUD

### Scripts
- `deploy.sh` - Script de despliegue para Linux/Mac
- `deploy.ps1` - Script de despliegue para Windows
- `DEPLOY.md` - Documentación completa de despliegue

---

## 🚀 Inicio Rápido

### INSTANCIA LOCAL (En tu casa)

```bash
# Windows PowerShell
.\deploy.ps1

# Linux/Mac
chmod +x deploy.sh
./deploy.sh

# O manualmente:
docker-compose -f docker-compose-local.yml up -d --build
```

**IMPORTANTE:** Antes de ejecutar, edita `docker-compose-local.yml` línea 12:
```yaml
- CLOUD_ENDPOINT_URL=http://TU_IP_CLOUD:8080/api/energia/recibir-mediciones
```

### INSTANCIA CLOUD (En servidor remoto)

```bash
# Windows PowerShell
.\deploy.ps1

# Linux/Mac
./deploy.sh

# O manualmente:
docker-compose -f docker-compose-cloud.yml up -d --build
```

---

## 🔍 Verificación

### LOCAL
```bash
docker logs -f ms-concentrador-local

# Deberías ver cada 15 segundos:
# === Iniciando lectura de 4 dispositivos Tasmota ===
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.221
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.77
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.26
# Datos leídos exitosamente de dispositivo Tasmota: 192.168.2.163
# Mediciones enviadas exitosamente al cloud
```

### CLOUD
```bash
docker logs -f ms-concentrador-cloud

# Probar endpoint:
curl http://localhost:8080/api/energia/health

# Resultado esperado:
# {"status":"UP","timestamp":"2025-12-09T..."}
```

---

## 🏗️ Diferencias Entre Instancias

| Característica | LOCAL | CLOUD |
|----------------|-------|-------|
| **Lee dispositivos Tasmota** | ✅ Sí, cada 15s | ❌ No |
| **Envía datos vía HTTP** | ✅ Sí, al cloud | ❌ No |
| **Recibe datos vía HTTP** | ⚠️ Opcional | ✅ Sí |
| **Guarda en MySQL** | ⚠️ Opcional | ✅ Sí |
| **Perfil Spring** | `local` | `cloud` |
| **Puerto** | 8080 | 8080 |

---

## 🔧 Personalización

### Cambiar IPs de Tasmota (LOCAL)

Edita `docker-compose-local.yml`:
```yaml
- MODBUS_DEVICES=192.168.2.221,192.168.2.77,192.168.2.26,192.168.2.163,192.168.2.NEW
```

### Cambiar intervalo de lectura (LOCAL)

Edita `docker-compose-local.yml`:
```yaml
- MODBUS_POLLING_INTERVAL=30000  # 30 segundos
```

### Usar MySQL externo (CLOUD)

Edita `docker-compose-cloud.yml`:
```yaml
- SPRING_DATASOURCE_URL=jdbc:mysql://TU_IP:3306/db_springboot_cloud
```

---

## 📊 Comandos Útiles

```bash
# Ver logs en tiempo real
docker-compose -f docker-compose-local.yml logs -f
docker-compose -f docker-compose-cloud.yml logs -f

# Reiniciar
docker-compose -f docker-compose-local.yml restart
docker-compose -f docker-compose-cloud.yml restart

# Detener
docker-compose -f docker-compose-local.yml down
docker-compose -f docker-compose-cloud.yml down

# Reconstruir después de cambios
docker-compose -f docker-compose-local.yml up -d --build
docker-compose -f docker-compose-cloud.yml up -d --build
```

---

## 🎯 Flujo Completo

```
1. INSTANCIA LOCAL lee Tasmota cada 15s
   ↓
2. Genera JSON con 11 parámetros por dispositivo
   ↓
3. POST http://CLOUD_IP:8080/api/energia/recibir-mediciones
   ↓
4. INSTANCIA CLOUD recibe JSON
   ↓
5. Inserta en MySQL cada medición
```

---

## 📝 Siguientes Pasos

1. ✅ Editar `docker-compose-local.yml` con IP del cloud
2. ✅ Ejecutar instancia CLOUD primero
3. ✅ Verificar que cloud esté funcionando
4. ✅ Ejecutar instancia LOCAL
5. ✅ Monitorear logs de ambas instancias

---

## 🆘 Troubleshooting

### LOCAL no conecta con Tasmota
- Verificar que los dispositivos estén en la misma red
- Probar ping a las IPs
- Considerar usar `network_mode: "host"` en docker-compose-local.yml

### LOCAL no envía a CLOUD
- Verificar que la IP/URL del cloud sea correcta
- Verificar firewall del servidor cloud (puerto 8080 abierto)
- Probar: `curl http://CLOUD_IP:8080/api/energia/health`

### CLOUD no guarda en MySQL
- Verificar logs: `docker logs -f ms-concentrador-cloud`
- Verificar MySQL: `docker logs -f mysql-concentrador-cloud`
- Probar conexión: `docker exec ms-concentrador-cloud mysql -h mysql -u root -psasa`

---

Para más detalles, ver **DEPLOY.md**
