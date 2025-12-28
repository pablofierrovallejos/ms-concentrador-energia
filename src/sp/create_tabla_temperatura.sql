-- Tabla para almacenar mediciones de temperatura
-- Diseñada específicamente para sensores de temperatura como el nodo T110

CREATE TABLE IF NOT EXISTS medicion_temperatura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombrenodo VARCHAR(50) NOT NULL COMMENT 'Nombre del nodo de temperatura (ej: T110)',
    temperatura DECIMAL(10,2) NOT NULL COMMENT 'Temperatura en grados Celsius',
    fechahora DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de la medición',
    device_ip VARCHAR(15) COMMENT 'Dirección IP del dispositivo (ej: 192.168.2.110)',
    
    INDEX idx_nodo_fecha (nombrenodo, fechahora),
    INDEX idx_fechahora (fechahora),
    INDEX idx_nombrenodo (nombrenodo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Almacena mediciones de temperatura de sensores';

-- Stored procedure para insertar mediciones de temperatura
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS sp_insertarMedicionTemperatura(
    IN p_nombrenodo VARCHAR(50),
    IN p_temperatura DECIMAL(10,2),
    IN p_device_ip VARCHAR(15)
)
BEGIN
    INSERT INTO medicion_temperatura (nombrenodo, temperatura, device_ip, fechahora)
    VALUES (p_nombrenodo, p_temperatura, p_device_ip, NOW());
END$$

DELIMITER ;

-- Tabla para notificaciones WebSocket de temperatura (similar a la de energía)
CREATE TABLE IF NOT EXISTS websocket_notifications_temperatura (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombrenodo VARCHAR(50) NOT NULL,
    temperatura DECIMAL(10,2) NOT NULL,
    fechameas DATETIME NOT NULL,
    device_ip VARCHAR(15),
    accion VARCHAR(50) DEFAULT 'INSERT',
    procesado BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_procesado TIMESTAMP NULL,
    idregistro BIGINT,
    
    INDEX idx_procesado (procesado),
    INDEX idx_fecha_creacion (fecha_creacion)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cola de notificaciones WebSocket para mediciones de temperatura';

-- Trigger para crear notificaciones WebSocket automáticamente al insertar temperatura
DELIMITER $$

CREATE TRIGGER IF NOT EXISTS trigger_websocket_notification_temperatura
AFTER INSERT ON medicion_temperatura
FOR EACH ROW
BEGIN
    INSERT INTO websocket_notifications_temperatura 
        (nombrenodo, temperatura, fechameas, device_ip, idregistro)
    VALUES 
        (NEW.nombrenodo, NEW.temperatura, NEW.fechahora, NEW.device_ip, NEW.id);
END$$

DELIMITER ;
