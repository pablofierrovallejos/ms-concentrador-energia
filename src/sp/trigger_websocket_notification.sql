-- ================================================
-- Sistema de Notificaciones WebSocket
-- ================================================
-- Este archivo implementa un sistema completo para notificar cambios
-- en la tabla medicionenergia_actual al frontend vía WebSocket

-- ================================================
-- 1. Crear tabla de notificaciones pendientes
-- ================================================
CREATE TABLE IF NOT EXISTS websocket_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombrenodo VARCHAR(50) NOT NULL,
    accion VARCHAR(20) NOT NULL,  -- 'INSERT' o 'UPDATE'
    idregistro BIGINT,
    volts VARCHAR(50),
    current VARCHAR(50),
    power VARCHAR(50),
    energy VARCHAR(50),
    fechameas DATETIME,
    procesado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_procesado TIMESTAMP NULL,
    INDEX idx_procesado (procesado),
    INDEX idx_nombrenodo (nombrenodo),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================
-- 2. Trigger AFTER INSERT - Registra nuevas mediciones
-- ================================================
DROP TRIGGER IF EXISTS trg_after_insert_medicionenergia_actual$$

DELIMITER $$

CREATE TRIGGER trg_after_insert_medicionenergia_actual
AFTER INSERT ON medicionenergia_actual
FOR EACH ROW
BEGIN
    -- Insertar notificación pendiente
    INSERT INTO websocket_notifications 
        (nombrenodo, accion, idregistro, volts, current, power, energy, fechameas, procesado)
    VALUES 
        (NEW.nombrenodo, 'INSERT', NEW.idregistro, NEW.volts, NEW.current, 
         NEW.power, NEW.energy, NEW.fechameas, FALSE);
END$$

-- ================================================
-- 3. Trigger AFTER UPDATE - Registra actualizaciones
-- ================================================
DROP TRIGGER IF EXISTS trg_after_update_medicionenergia_actual$$

CREATE TRIGGER trg_after_update_medicionenergia_actual
AFTER UPDATE ON medicionenergia_actual
FOR EACH ROW
BEGIN
    -- Solo crear notificación si hubo cambios significativos en los datos
    IF (OLD.volts <> NEW.volts OR 
        OLD.current <> NEW.current OR 
        OLD.power <> NEW.power OR 
        OLD.energy <> NEW.energy) THEN
        
        -- Insertar notificación pendiente
        INSERT INTO websocket_notifications 
            (nombrenodo, accion, idregistro, volts, current, power, energy, fechameas, procesado)
        VALUES 
            (NEW.nombrenodo, 'UPDATE', NEW.idregistro, NEW.volts, NEW.current, 
             NEW.power, NEW.energy, NEW.fechameas, FALSE);
    END IF;
END$$

DELIMITER ;

-- ================================================
-- Notas de implementación:
-- ================================================
-- 1. Los triggers INSERT/UPDATE crean registros en websocket_notifications
-- 2. El servicio WebSocketNotificationProcessor (Java) ejecuta cada 1 segundo
-- 3. El procesador lee notificaciones pendientes (procesado = FALSE)
-- 4. Envía notificación WebSocket al frontend
-- 5. Marca como procesado (procesado = TRUE, fecha_procesado = NOW())
-- 6. Limpieza automática de registros procesados >24h cada hora
--
-- Flujo completo:
-- INSERT/UPDATE en medicionenergia_actual
--   -> Trigger SQL inserta en websocket_notifications (procesado=FALSE)
--   -> WebSocketNotificationProcessor detecta registro pendiente (cada 1 seg)
--   -> WebSocketNotificationService.notificarNuevaMedicion()
--   -> Frontend recibe en /topic/estadistica/{nodo}
--   -> Marca procesado=TRUE en websocket_notifications
--
-- Ventajas de este enfoque:
-- ✅ Funciona incluso si los cambios se hacen directamente en SQL
-- ✅ No requiere que JPA haga los cambios
-- ✅ Desacoplado: la BD no necesita llamar APIs HTTP
-- ✅ Resiliente: si Java se cae, las notificaciones quedan pendientes
-- ✅ Auditable: se puede ver historial de notificaciones
-- ✅ Escalable: múltiples instancias de Java pueden procesar
--
-- Monitoreo:
-- SELECT COUNT(*) FROM websocket_notifications WHERE procesado = FALSE;
-- SELECT * FROM websocket_notifications ORDER BY fecha_creacion DESC LIMIT 10;

-- ================================================
-- Comandos útiles para pruebas:
-- ================================================
-- Ver notificaciones pendientes:
-- SELECT * FROM websocket_notifications WHERE procesado = FALSE;
--
-- Simular actualización para generar notificación:
-- UPDATE medicionenergia_actual 
-- SET power = '1250.5', volts = '220.3' 
-- WHERE nombrenodo = 'T26';
--
-- Limpiar tabla de notificaciones (testing):
-- TRUNCATE TABLE websocket_notifications;
