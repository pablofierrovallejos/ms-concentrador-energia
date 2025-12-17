package com.pablofierro.energia.controllers;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import com.pablofierro.energia.models.entity.Medicionenergia;
import com.pablofierro.energia.models.service.IMedicionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energia")
@Profile("cloud")
public class EnergyController {

    private static final Logger logger = LoggerFactory.getLogger(EnergyController.class);
    
    @Autowired
    private IMedicionService medicionService;
    
    /**
     * Endpoint para recibir mediciones desde la instancia local
     * Este endpoint se ejecutará en la instancia cloud
     */
    @PostMapping("/recibir-mediciones")
    public ResponseEntity<Map<String, Object>> recibirMediciones(@RequestBody List<EnergyDataDTO> mediciones) {
        Map<String, Object> response = new HashMap<>();
        List<String> registrosInsertados = new ArrayList<>();
        List<String> errores = new ArrayList<>();
        
        logger.info("Recibiendo {} mediciones para insertar en BD", mediciones.size());
        
        for (EnergyDataDTO medicion : mediciones) {
            try {
                // Convertir DTO a entidad y guardar en BD
                insertarMedicionEnBD(medicion);
                registrosInsertados.add(medicion.getDeviceIp());
                logger.debug("Medición insertada: {}", medicion.getDeviceIp());
            } catch (Exception e) {
                logger.error("Error insertando medición de {}: {}", medicion.getDeviceIp(), e.getMessage());
                errores.add(medicion.getDeviceIp() + ": " + e.getMessage());
            }
        }
        
        response.put("total_recibidas", mediciones.size());
        response.put("total_insertadas", registrosInsertados.size());
        response.put("registros_insertados", registrosInsertados);
        
        if (!errores.isEmpty()) {
            response.put("errores", errores);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Inserta una medición en la base de datos
     */
    private void insertarMedicionEnBD(EnergyDataDTO dto) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyHHmmss");
        Date fecha = dto.getTimestamp() != null ? dto.getTimestamp() : new Date();
        
        String uptime = formatter.format(fecha);
        // Extraer último octeto de la IP para nombre de nodo: 192.168.2.221 -> T221
        String lastOctet = dto.getDeviceIp().substring(dto.getDeviceIp().lastIndexOf('.') + 1);
        String nombrenodo = "T" + lastOctet;
        String volts = String.valueOf(dto.getVoltage());
        String current = String.valueOf(dto.getCurrent());
        String power = String.valueOf(dto.getActivePower());
        String energy = String.valueOf(dto.getEnergyTotal());
        
        // Usar el servicio existente para agregar medición
        medicionService.agregarMedicion(uptime, nombrenodo, uptime, volts, current, power, energy);
    }
    
    /**
     * Endpoint de prueba para verificar que el servicio está activo
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para ver el último registro de un dispositivo
     */
    @GetMapping("/ultimo-registro/{deviceIp}")
    public ResponseEntity<List<Medicionenergia>> obtenerUltimoRegistro(@PathVariable String deviceIp) {
        String nodoName = deviceIp.replace(".", "_");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = sdf.format(new Date());
        
        List<Medicionenergia> mediciones = medicionService.consultarMeasEnergia(nodoName, fecha);
        return ResponseEntity.ok(mediciones);
    }
}
