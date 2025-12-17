package com.pablofierro.energia.models.service;

import com.pablofierro.energia.models.entity.WebSocketNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWebSocketNotificationRepository extends JpaRepository<WebSocketNotification, Long> {
    
    /**
     * Busca todas las notificaciones pendientes (no procesadas)
     * ordenadas por fecha de creación
     */
    @Query("SELECT w FROM WebSocketNotification w WHERE w.procesado = false ORDER BY w.fechaCreacion ASC")
    List<WebSocketNotification> findPendingNotifications();
    
    /**
     * Busca notificaciones pendientes de un nodo específico
     */
    @Query("SELECT w FROM WebSocketNotification w WHERE w.procesado = false AND w.nombrenodo = :nodo ORDER BY w.fechaCreacion ASC")
    List<WebSocketNotification> findPendingNotificationsByNode(String nodo);
    
    /**
     * Elimina notificaciones procesadas de más de 24 horas
     */
    @Modifying
    @Query(value = "DELETE FROM websocket_notifications WHERE procesado = true AND fecha_procesado < DATE_SUB(NOW(), INTERVAL 24 HOUR)", nativeQuery = true)
    int deleteProcessedOlderThan24Hours();
}
