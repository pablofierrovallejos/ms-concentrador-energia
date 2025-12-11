package com.pablofierro.energia.services;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudSenderService {

    private static final Logger logger = LoggerFactory.getLogger(CloudSenderService.class);
    
    @Value("${cloud.endpoint.url}")
    private String cloudEndpointUrl;
    
    private final RestTemplate restTemplate;
    
    public CloudSenderService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Envía una lista de mediciones al endpoint en la nube
     */
    public boolean sendMeasurements(List<EnergyDataDTO> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            logger.warn("No hay mediciones para enviar");
            return false;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<List<EnergyDataDTO>> request = new HttpEntity<>(measurements, headers);
            
            logger.info("Enviando {} mediciones al endpoint: {}", measurements.size(), cloudEndpointUrl);
            
            ResponseEntity<String> response = restTemplate.exchange(
                cloudEndpointUrl,
                HttpMethod.POST,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Mediciones enviadas exitosamente. Response: {}", response.getBody());
                return true;
            } else {
                logger.error("Error al enviar mediciones. Status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al enviar mediciones al endpoint: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Envía una sola medición al endpoint en la nube
     */
    public boolean sendSingleMeasurement(EnergyDataDTO measurement) {
        if (measurement == null) {
            logger.warn("La medición es nula");
            return false;
        }
        
        return sendMeasurements(List.of(measurement));
    }
}
