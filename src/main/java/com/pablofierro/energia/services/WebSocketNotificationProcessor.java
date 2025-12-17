package com.pablofierro.energia.services;

import com.pablofierro.energia.models.entity.MedicionenergiaActual;
import com.pablofierro.energia.models.entity.WebSocketNotification;
import com.pablofierro.energia.models.service.IWebSocketNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Servicio que procesa notificaciones pendientes cada segundo
 * Lee la tabla websocket_notifications y envía notificaciones WebSocket
 * a los clientes suscritos cuando detecta cambios en la base de datos
 * Solo se activa cuando hay DataSource configurado (perfil cloud)
 */
@Service
@Profile("cloud")
public class WebSocketNotificationProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationProcessor.class);
    
    @Autowired
    private IWebSocketNotificationRepository notificationRepository;
    
    @Autowired
    private WebSocketNotificationService webSocketService;
    
    /**
     * Se ejecuta cada 1 segundo para procesar notificaciones pendientes
     * Spring Boot debe tener @EnableScheduling en la clase principal
     */
    @Scheduled(fixedRate = 1000) // Cada 1000ms = 1 segundo
    @Transactional
    public void procesarNotificacionesPendientes() {
        try {
            List<WebSocketNotification> pendientes = notificationRepository.findPendingNotifications();
            
            if (!pendientes.isEmpty()) {
                logger.info("Procesando {} notificaciones pendientes", pendientes.size());
                
                for (WebSocketNotification notif : pendientes) {
                    procesarNotificacion(notif);
                }
            }
        } catch (Exception e) {
            logger.error("Error procesando notificaciones pendientes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Procesa una notificación individual
     */
    private void procesarNotificacion(WebSocketNotification notif) {
        try {
            // Convertir a MedicionenergiaActual para enviar
            MedicionenergiaActual medicion = new MedicionenergiaActual();
            medicion.setNombrenodo(notif.getNombrenodo());
            medicion.setIdregistro(notif.getIdregistro());
            medicion.setVolts(notif.getVolts());
            medicion.setCurrent(notif.getCurrent());
            medicion.setPower(notif.getPower());
            medicion.setEnergy(notif.getEnergy());
            medicion.setFechameas(notif.getFechameas());
            
            // Enviar notificación WebSocket
            webSocketService.notificarNuevaMedicion(notif.getNombrenodo(), medicion);
            
            // Eliminar inmediatamente después de procesar (no necesitamos histórico)
            notificationRepository.delete(notif);
            
            logger.debug("Notificación ID {} procesada y eliminada para nodo {}", 
                        notif.getId(), notif.getNombrenodo());
            
        } catch (Exception e) {
            logger.error("Error procesando notificación ID {}: {}", notif.getId(), e.getMessage());
        }
    }
    

}
