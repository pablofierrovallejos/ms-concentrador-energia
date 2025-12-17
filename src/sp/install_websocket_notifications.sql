-- ================================================
-- INSTALACIÓN COMPLETA: WebSocket Notifications
-- ================================================
-- Base de datos: db_springboot_cloud
-- Este script crea todo lo necesario para notificaciones WebSocket en tiempo real

USE db_springboot_cloud;

-- ================================================
-- 1. Crear tabla de notificaciones pendientes
-- ================================================
DROP TABLE IF EXISTS websocket_notifications;

CREATE TABLE websocket_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombrenodo VARCHAR(50) NOT NULL,
    accion VARCHAR(20) NOT NULL COMMENT 'INSERT o UPDATE',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Cola de notificaciones WebSocket pendientes de enviar';

-- ================================================
-- 2. Eliminar triggers anteriores si existen
-- ================================================
DROP TRIGGER IF EXISTS trg_after_insert_medicionenergia_actual;
DROP TRIGGER IF EXISTS trg_after_update_medicionenergia_actual;

-- ================================================
-- 3. Trigger AFTER INSERT - Detecta nuevas mediciones
-- ================================================
DELIMITER $$

CREATE TRIGGER trg_after_insert_medicionenergia_actual
AFTER INSERT ON medicionenergia_actual
FOR EACH ROW
BEGIN
    -- Insertar notificación pendiente para envío WebSocket
    INSERT INTO websocket_notifications 
        (nombrenodo, accion, idregistro, volts, current, power, energy, fechameas, procesado)
    VALUES 
        (NEW.nombrenodo, 'INSERT', NEW.idregistro, NEW.volts, NEW.current, 
         NEW.power, NEW.energy, NEW.fechameas, FALSE);
END$$

-- ================================================
-- 4. Trigger AFTER UPDATE - Detecta actualizaciones
-- ================================================
CREATE TRIGGER trg_after_update_medicionenergia_actual
AFTER UPDATE ON medicionenergia_actual
FOR EACH ROW
BEGIN
    -- Solo crear notificación si hubo cambios significativos en los datos
    IF (OLD.volts <> NEW.volts OR 
        OLD.current <> NEW.current OR 
        OLD.power <> NEW.power OR 
        OLD.energy <> NEW.energy) THEN
        
        -- Insertar notificación pendiente para envío WebSocket
        INSERT INTO websocket_notifications 
            (nombrenodo, accion, idregistro, volts, current, power, energy, fechameas, procesado)
        VALUES 
            (NEW.nombrenodo, 'UPDATE', NEW.idregistro, NEW.volts, NEW.current, 
             NEW.power, NEW.energy, NEW.fechameas, FALSE);
    END IF;
END$$

DELIMITER ;

-- ================================================
-- 5. Verificar instalación
-- ================================================
SELECT 'Tabla creada correctamente:' AS status;
SELECT 
    TABLE_NAME, 
    TABLE_ROWS, 
    CREATE_TIME 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'db_springboot_cloud' 
AND TABLE_NAME = 'websocket_notifications';

SELECT 'Triggers creados:' AS status;
SELECT 
    TRIGGER_NAME, 
    EVENT_MANIPULATION, 
    EVENT_OBJECT_TABLE,
    ACTION_TIMING
FROM information_schema.TRIGGERS 
WHERE TRIGGER_SCHEMA = 'db_springboot_cloud' 
AND EVENT_OBJECT_TABLE = 'medicionenergia_actual'
AND TRIGGER_NAME LIKE 'trg_after_%';

-- ================================================
-- 6. Test opcional - Generar notificación de prueba
-- ================================================
-- Descomentar las siguientes líneas para probar:
/*
-- Ver contenido actual de medicionenergia_actual
SELECT * FROM medicionenergia_actual;

-- Forzar UPDATE para generar notificación
UPDATE medicionenergia_actual 
SET power = CONCAT(power, '.test')
WHERE nombrenodo IN (SELECT nombrenodo FROM medicionenergia_actual LIMIT 1);

-- Verificar que se creó la notificación pendiente
SELECT * FROM websocket_notifications WHERE procesado = FALSE;

-- En 1-2 segundos, verificar que se procesó (el servicio Java debe estar corriendo)
-- SELECT * FROM websocket_notifications ORDER BY id DESC LIMIT 5;
*/

-- ================================================
-- INSTALACIÓN COMPLETADA
-- ================================================
SELECT '✅ Instalación completada exitosamente!' AS status;
SELECT 'El servicio Java procesará notificaciones cada 1 segundo' AS info;
SELECT 'Usa: SELECT * FROM websocket_notifications WHERE procesado = FALSE; para ver pendientes' AS query_util;
