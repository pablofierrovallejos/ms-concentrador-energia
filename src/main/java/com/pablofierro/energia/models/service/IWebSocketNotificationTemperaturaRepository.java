package com.pablofierro.energia.models.service;

import com.pablofierro.energia.models.entity.WebSocketNotificationTemperatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IWebSocketNotificationTemperaturaRepository extends JpaRepository<WebSocketNotificationTemperatura, Long> {
    
    /**
     * Obtiene todas las notificaciones pendientes de temperatura
     * ordenadas por fecha de creación
     */
    @Query("SELECT n FROM WebSocketNotificationTemperatura n WHERE n.procesado = false ORDER BY n.fechaCreacion ASC")
    List<WebSocketNotificationTemperatura> findPendingNotifications();
}
