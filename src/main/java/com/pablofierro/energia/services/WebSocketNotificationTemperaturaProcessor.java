package com.pablofierro.energia.services;

import com.pablofierro.energia.models.entity.MedicionTemperatura;
import com.pablofierro.energia.models.entity.WebSocketNotificationTemperatura;
import com.pablofierro.energia.models.service.IWebSocketNotificationTemperaturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio que procesa notificaciones de temperatura pendientes cada segundo
 * Lee la tabla websocket_notifications_temperatura y envía notificaciones WebSocket
 * Solo se activa en perfil cloud
 */
@Service
@Profile("cloud")
public class WebSocketNotificationTemperaturaProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationTemperaturaProcessor.class);
    
    @Autowired
    private IWebSocketNotificationTemperaturaRepository notificationRepository;
    
    @Autowired
    private WebSocketNotificationService webSocketService;
    
    /**
     * Se ejecuta cada 1 segundo para procesar notificaciones pendientes de temperatura
     */
    @Scheduled(fixedRate = 1000) // Cada 1000ms = 1 segundo
    @Transactional
    public void procesarNotificacionesPendientes() {
        try {
            List<WebSocketNotificationTemperatura> pendientes = notificationRepository.findPendingNotifications();
            
            if (!pendientes.isEmpty()) {
                logger.info("Procesando {} notificaciones de temperatura pendientes", pendientes.size());
                
                for (WebSocketNotificationTemperatura notif : pendientes) {
                    procesarNotificacion(notif);
                }
            }
        } catch (Exception e) {
            logger.error("Error procesando notificaciones de temperatura pendientes: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Procesa una notificación de temperatura individual
     */
    private void procesarNotificacion(WebSocketNotificationTemperatura notif) {
        try {
            // Convertir a MedicionTemperatura para enviar
            MedicionTemperatura medicion = new MedicionTemperatura();
            medicion.setId(notif.getIdregistro());
            medicion.setNombrenodo(notif.getNombrenodo());
            medicion.setTemperatura(notif.getTemperatura());
            medicion.setFechahora(notif.getFechameas());
            medicion.setDeviceIp(notif.getDeviceIp());
            
            // Enviar notificación WebSocket
            webSocketService.notificarNuevaTemperatura(notif.getNombrenodo(), medicion);
            
            // Eliminar inmediatamente después de procesar
            notificationRepository.delete(notif);
            
            logger.debug("Notificación de temperatura ID {} procesada y eliminada para nodo {}", 
                        notif.getId(), notif.getNombrenodo());
            
        } catch (Exception e) {
            logger.error("Error procesando notificación de temperatura ID {}: {}", 
                        notif.getId(), e.getMessage(), e);
        }
    }
}
