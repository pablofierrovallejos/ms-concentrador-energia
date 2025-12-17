package com.pablofierro.energia.listeners;

import com.pablofierro.energia.models.entity.MedicionenergiaActual;
import com.pablofierro.energia.services.WebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

/**
 * Listener de JPA que detecta cambios en la tabla MedicionenergiaActual
 * y notifica al frontend vía WebSocket automáticamente
 * 
 * Este listener se ejecuta después de INSERT y UPDATE en la tabla
 */
@Component
public class MedicionActualEntityListener {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicionActualEntityListener.class);
    
    private static WebSocketNotificationService notificationService;
    
    @Autowired
    public void setNotificationService(WebSocketNotificationService service) {
        MedicionActualEntityListener.notificationService = service;
    }
    
    /**
     * Se ejecuta después de insertar un nuevo registro
     */
    @PostPersist
    public void afterInsert(MedicionenergiaActual medicion) {
        logger.info("Detectado INSERT en MedicionenergiaActual para nodo: {}", medicion.getNombrenodo());
        notificarCambio(medicion);
    }
    
    /**
     * Se ejecuta después de actualizar un registro existente
     */
    @PostUpdate
    public void afterUpdate(MedicionenergiaActual medicion) {
        logger.info("Detectado UPDATE en MedicionenergiaActual para nodo: {}", medicion.getNombrenodo());
        notificarCambio(medicion);
    }
    
    /**
     * Envía la notificación WebSocket al frontend
     */
    private void notificarCambio(MedicionenergiaActual medicion) {
        if (notificationService != null) {
            notificationService.notificarNuevaMedicion(medicion.getNombrenodo(), medicion);
        } else {
            logger.warn("NotificationService no está disponible en el listener");
        }
    }
}
