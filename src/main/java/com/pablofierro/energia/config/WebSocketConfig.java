package com.pablofierro.energia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para notificaciones push al frontend
 * Permite comunicación bidireccional en tiempo real usando STOMP sobre WebSocket
 * Solo se activa en perfil cloud donde hay base de datos
 */
@Configuration
@EnableWebSocketMessageBroker
@Profile("cloud")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple de mensajes en memoria
        // Los clientes se suscriben a /topic/* para recibir notificaciones
        config.enableSimpleBroker("/topic");
        
        // Prefijo para mensajes que van al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint para que los clientes se conecten vía WebSocket
        // Accesible desde: ws://localhost:8080/ws-energia
        registry.addEndpoint("/ws-energia")
                .setAllowedOriginPatterns("*") // En producción, especificar dominios permitidos
                .withSockJS(); // Fallback a HTTP long-polling si WebSocket no está disponible
    }
}
