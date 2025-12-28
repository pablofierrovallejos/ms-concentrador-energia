package com.pablofierro.energia.models.dto;

import java.util.Date;

/**
 * DTO para transferencia de datos de temperatura
 * Usado para comunicación entre microservicios local y cloud
 */
public class TemperaturaDataDTO {
    
    private String deviceIp;
    private String nombrenodo;
    private Double temperatura;
    private Date timestamp;
    
    public TemperaturaDataDTO() {
        this.timestamp = new Date();
    }
    
    public TemperaturaDataDTO(String deviceIp, String nombrenodo, Double temperatura) {
        this.deviceIp = deviceIp;
        this.nombrenodo = nombrenodo;
        this.temperatura = temperatura;
        this.timestamp = new Date();
    }

    // Getters y Setters
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public String getNombrenodo() {
        return nombrenodo;
    }

    public void setNombrenodo(String nombrenodo) {
        this.nombrenodo = nombrenodo;
    }

    public Double getTemperatura() {
        return temperatura;
    }

    public void setTemperatura(Double temperatura) {
        this.temperatura = temperatura;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TemperaturaDataDTO [deviceIp=" + deviceIp + ", nombrenodo=" + nombrenodo + ", temperatura="
                + temperatura + ", timestamp=" + timestamp + "]";
    }
}
