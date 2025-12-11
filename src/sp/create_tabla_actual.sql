-- Tabla para mantener solo el último registro de cada nodo
CREATE TABLE IF NOT EXISTS `medicionenergia_actual` (
  `nombrenodo` varchar(10) NOT NULL,
  `idregistro` varchar(50) DEFAULT NULL,
  `uptime` varchar(50) DEFAULT NULL,
  `volts` varchar(50) DEFAULT NULL,
  `current` varchar(50) DEFAULT NULL,
  `power` varchar(50) DEFAULT NULL,
  `energy` varchar(50) DEFAULT NULL,
  `fechameas` datetime DEFAULT NULL,
  `ultima_actualizacion` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`nombrenodo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índice para consultas ultrarrápidas
CREATE INDEX idx_ultima_actualizacion ON medicionenergia_actual(ultima_actualizacion DESC);
