package com.pablofierro.energia.services;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    /**
     * Lee los datos de energía del inversor solar específico en 192.168.2.72
     * mediante scraping web con autenticación básica
     * Extrae: Current power, Yield today, Total yield desde /status.html
     */
    public EnergyDataDTO readSolarInverterData(String ipAddress) {
        EnergyDataDTO data = new EnergyDataDTO();
        data.setDeviceIp(ipAddress);
        
        try {
            // Construir URL - usar status.html que tiene las variables JavaScript
            String url = "http://" + ipAddress + "/status.html";
            
            // Configurar autenticación básica
            String auth = "admin:admin";
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = "Basic " + new String(encodedAuth);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Realizar petición HTTP con autenticación
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String html = response.getBody();
                
                // Extraer "Current power" desde la variable JavaScript webdata_now_p
                // Formato: var webdata_now_p = "2050";
                Pattern currentPowerPattern = Pattern.compile("var\\s+webdata_now_p\\s*=\\s*\"([\\d.]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher currentPowerMatcher = currentPowerPattern.matcher(html);
                if (currentPowerMatcher.find()) {
                    String value = currentPowerMatcher.group(1);
                    data.setActivePower(Double.parseDouble(value));
                    logger.info("Current power encontrado: {} W", value);
                } else {
                    logger.warn("No se encontró variable 'webdata_now_p' en el HTML del inversor");
                }
                
                // Extraer "Yield today" desde la variable JavaScript webdata_today_e
                // Formato: var webdata_today_e = "23.20";
                Pattern yieldTodayPattern = Pattern.compile("var\\s+webdata_today_e\\s*=\\s*\"([\\d.]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher yieldTodayMatcher = yieldTodayPattern.matcher(html);
                if (yieldTodayMatcher.find()) {
                    String value = yieldTodayMatcher.group(1);
                    data.setEnergyToday(Double.parseDouble(value));
                    logger.info("Yield today encontrado: {} kWh", value);
                } else {
                    logger.warn("No se encontró variable 'webdata_today_e' en el HTML del inversor");
                }
                
                // Extraer "Total yield" desde la variable JavaScript webdata_total_e
                // Formato: var webdata_total_e = "12108.0";
                Pattern totalYieldPattern = Pattern.compile("var\\s+webdata_total_e\\s*=\\s*\"([\\d.]+)\"", Pattern.CASE_INSENSITIVE);
                Matcher totalYieldMatcher = totalYieldPattern.matcher(html);
                if (totalYieldMatcher.find()) {
                    String value = totalYieldMatcher.group(1);
                    data.setEnergyTotal(Double.parseDouble(value));
                    logger.info("Total yield encontrado: {} kWh", value);
                } else {
                    logger.warn("No se encontró variable 'webdata_total_e' en el HTML del inversor");
                }
                
                logger.info("Datos leídos del inversor solar: {} - Power: {}W, Today: {}kWh, Total: {}kWh", 
                    ipAddress, data.getActivePower(), data.getEnergyToday(), data.getEnergyTotal());
            } else {
                logger.warn("Respuesta no válida del inversor solar {}", ipAddress);
            }
            
        } catch (Exception e) {
            logger.error("Error leyendo datos del inversor solar {}: {}", ipAddress, e.getMessage(), e);
        }
        
        return data;
    }
}
