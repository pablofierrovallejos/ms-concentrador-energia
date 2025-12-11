package com.pablofierro.energia.services;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Clase de prueba para verificar la lectura de dispositivos Tasmota
 * Solo se ejecuta si se activa el perfil "test-tasmota"
 * 
 * Para ejecutar: mvn spring-boot:run -Dspring-boot.run.profiles=test-tasmota
 */
@Component
@Profile("test-tasmota")
public class TasmotaTestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TasmotaTestRunner.class);
    
    @Autowired
    private TasmotaReaderService tasmotaReaderService;

    @Override
    public void run(String... args) throws Exception {
        logger.info("========================================");
        logger.info("INICIANDO PRUEBA DE LECTURA TASMOTA");
        logger.info("========================================");
        
        // Dispositivos a probar
        List<String> testDevices = Arrays.asList(
            "192.168.2.221",
            "192.168.2.77",
            "192.168.2.26",
            "192.168.2.163"
        );
        
        logger.info("Probando {} dispositivos...", testDevices.size());
        
        for (String deviceIp : testDevices) {
            logger.info("\n--- Probando dispositivo: {} ---", deviceIp);
            
            try {
                EnergyDataDTO data = tasmotaReaderService.readDeviceData(deviceIp);
                
                if (data != null) {
                    logger.info("✓ Lectura exitosa!");
                    logger.info("  Voltage: {} V", data.getVoltage());
                    logger.info("  Frequency: {} Hz", data.getFrequency());
                    logger.info("  Current: {} A", data.getCurrent());
                    logger.info("  Active Power: {} W", data.getActivePower());
                    logger.info("  Apparent Power: {} VA", data.getApparentPower());
                    logger.info("  Reactive Power: {} var", data.getReactivePower());
                    logger.info("  Power Factor: {}", data.getPowerFactor());
                    logger.info("  Energy Today: {} kWh", data.getEnergyToday());
                    logger.info("  Energy Yesterday: {} kWh", data.getEnergyYesterday());
                    logger.info("  Energy Total: {} kWh", data.getEnergyTotal());
                    logger.info("  Address: {}", data.getAddress());
                } else {
                    logger.error("✗ No se pudo leer el dispositivo");
                }
                
            } catch (Exception e) {
                logger.error("✗ Error al leer dispositivo: {}", e.getMessage());
            }
            
            // Pausa entre lecturas
            Thread.sleep(1000);
        }
        
        logger.info("\n========================================");
        logger.info("PRUEBA FINALIZADA");
        logger.info("========================================");
        
        // Terminar la aplicación después de la prueba
        System.exit(0);
    }
}
