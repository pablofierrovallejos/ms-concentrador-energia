package com.pablofierro.energia.services;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class TasmotaReaderService {

    private static final Logger logger = LoggerFactory.getLogger(TasmotaReaderService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${tasmota.timeout:5000}")
    private int timeout;

    public TasmotaReaderService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Lee los datos de energía de un dispositivo Tasmota vía HTTP
     * Endpoint: http://IP/cm?cmnd=Status%208
     */
    public EnergyDataDTO readDeviceData(String ipAddress) {
        EnergyDataDTO data = new EnergyDataDTO();
        data.setDeviceIp(ipAddress);
        
        try {
            // Construir URL del comando Tasmota
            String url = "http://" + ipAddress + "/cm?cmnd=Status+8";
            
            // Realizar petición HTTP
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parsear JSON de respuesta
                JSONObject json = new JSONObject(response.getBody());
                JSONObject statusSns = json.getJSONObject("StatusSNS");
                JSONObject energy = statusSns.getJSONObject("ENERGY");
                
                // Extraer datos de energía
                data.setVoltage(energy.optDouble("Voltage", 0.0));
                data.setFrequency(energy.optDouble("Frequency", 0.0));
                data.setCurrent(energy.optDouble("Current", 0.0));
                data.setActivePower(energy.optDouble("Power", 0.0));
                data.setApparentPower(energy.optDouble("ApparentPower", 0.0));
                data.setReactivePower(energy.optDouble("ReactivePower", 0.0));
                data.setPowerFactor(energy.optDouble("Factor", 0.0));
                data.setEnergyToday(energy.optDouble("Today", 0.0));
                data.setEnergyYesterday(energy.optDouble("Yesterday", 0.0));
                data.setEnergyTotal(energy.optDouble("Total", 0.0));
                data.setAddress(energy.optInt("Address", 1));
                
                logger.info("Datos leídos exitosamente de dispositivo Tasmota: {}", ipAddress);
            } else {
                logger.warn("Respuesta no válida de dispositivo {}", ipAddress);
            }
            
        } catch (Exception e) {
            logger.error("Error leyendo datos de dispositivo {}: {}", ipAddress, e.getMessage());
        }
        
        return data;
    }
    
    /**
     * Lee datos de múltiples dispositivos
     */
    public List<EnergyDataDTO> readMultipleDevices(List<String> ipAddresses) {
        List<EnergyDataDTO> dataList = new ArrayList<>();
        
        for (String ip : ipAddresses) {
            try {
                EnergyDataDTO data = readDeviceData(ip);
                if (data != null) {
                    dataList.add(data);
                }
            } catch (Exception e) {
                logger.error("Error procesando dispositivo {}: {}", ip, e.getMessage());
            }
        }
        
        return dataList;
    }
}
