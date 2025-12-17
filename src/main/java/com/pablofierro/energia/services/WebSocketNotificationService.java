package com.pablofierro.energia.services;

import com.pablofierro.energia.models.entity.Medicionenergia;
import com.pablofierro.energia.models.entity.MedicionenergiaActual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para enviar notificaciones en tiempo real a través de WebSocket
 * Notifica al frontend cuando hay cambios en las estadísticas de energía
 * Solo se activa en perfil cloud donde hay base de datos
 */
@Service
@Profile("cloud")
public class WebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Notifica al frontend sobre una nueva medición de energía
     * Los clientes suscritos a /topic/estadistica/{nodo} recibirán esta notificación
     * 
     * @param nodo Nombre del nodo (ej: T26)
     * @param medicion Datos de la medición actualizada
     */
    public void notificarNuevaMedicion(String nodo, MedicionenergiaActual medicion) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "NUEVA_MEDICION");
            notificacion.put("nodo", nodo);
            notificacion.put("timestamp", System.currentTimeMillis());
            notificacion.put("data", medicion);
            
            // Enviar a topic específico del nodo
            String destination = "/topic/estadistica/" + nodo;
            messagingTemplate.convertAndSend(destination, notificacion);
            
            logger.info("Notificación enviada vía WebSocket para nodo: {} a {}", nodo, destination);
        } catch (Exception e) {
            logger.error("Error enviando notificación WebSocket para nodo {}: {}", nodo, e.getMessage());
        }
    }
    
    /**
     * Notifica al frontend sobre cualquier cambio en las estadísticas
     * Los clientes suscritos a /topic/estadistica/all recibirán todas las notificaciones
     * 
     * @param nodo Nombre del nodo (ej: T26)
     * @param fecha Fecha de la estadística
     * @param mensaje Mensaje descriptivo del cambio
     */
    public void notificarCambioEstadistica(String nodo, String fecha, String mensaje) {
        try {
            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("tipo", "CAMBIO_ESTADISTICA");
            notificacion.put("nodo", nodo);
            notificacion.put("fecha", fecha);
            notificacion.put("mensaje", mensaje);
            notificacion.put("timestamp", System.currentTimeMillis());
            
            // Enviar a topic general
            messagingTemplate.convertAndSend("/topic/estadistica/all", notificacion);
            
            // Enviar también a topic específico del nodo
            String destination = "/topic/estadistica/" + nodo;
            messagingTemplate.convertAndSend(destination, notificacion);
            
            logger.info("Notificación de cambio enviada para nodo: {} fecha: {}", nodo, fecha);
        } catch (Exception e) {
            logger.error("Error enviando notificación de cambio: {}", e.getMessage());
        }
    }
}
