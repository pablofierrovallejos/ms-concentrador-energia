DELIMITER $$

-- Trigger: actualizar tabla actual cuando se inserta nueva medición
CREATE TRIGGER `trg_actualizar_medicion_actual`
AFTER INSERT ON `medicionenergia`
FOR EACH ROW
BEGIN
    -- Insertar o reemplazar el registro del nodo
    INSERT INTO medicionenergia_actual 
        (nombrenodo, idregistro, uptime, volts, current, power, energy, fechameas)
    VALUES 
        (NEW.nombrenodo, NEW.idregistro, NEW.uptime, NEW.volts, NEW.current, NEW.power, NEW.energy, NEW.fechameas)
    ON DUPLICATE KEY UPDATE
        idregistro = NEW.idregistro,
        uptime = NEW.uptime,
        volts = NEW.volts,
        current = NEW.current,
        power = NEW.power,
        energy = NEW.energy,
        fechameas = NEW.fechameas;
END$$

DELIMITER ;
