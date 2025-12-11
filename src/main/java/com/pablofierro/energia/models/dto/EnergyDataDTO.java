package com.pablofierro.energia.models.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EnergyDataDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String deviceIp;
    private Double voltage;
    private Double frequency;
    private Double current;
    private Double activePower;
    private Double apparentPower;
    private Double reactivePower;
    private Double powerFactor;
    private Double energyToday;
    private Double energyYesterday;
    private Double energyTotal;
    private Integer address;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Santiago")
    private Date timestamp;

    public EnergyDataDTO() {
        this.timestamp = new Date();
    }

    // Getters y Setters
    public String getDeviceIp() {
        return deviceIp;
    }

    public void setDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Double getActivePower() {
        return activePower;
    }

    public void setActivePower(Double activePower) {
        this.activePower = activePower;
    }

    public Double getApparentPower() {
        return apparentPower;
    }

    public void setApparentPower(Double apparentPower) {
        this.apparentPower = apparentPower;
    }

    public Double getReactivePower() {
        return reactivePower;
    }

    public void setReactivePower(Double reactivePower) {
        this.reactivePower = reactivePower;
    }

    public Double getPowerFactor() {
        return powerFactor;
    }

    public void setPowerFactor(Double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public Double getEnergyToday() {
        return energyToday;
    }

    public void setEnergyToday(Double energyToday) {
        this.energyToday = energyToday;
    }

    public Double getEnergyYesterday() {
        return energyYesterday;
    }

    public void setEnergyYesterday(Double energyYesterday) {
        this.energyYesterday = energyYesterday;
    }

    public Double getEnergyTotal() {
        return energyTotal;
    }

    public void setEnergyTotal(Double energyTotal) {
        this.energyTotal = energyTotal;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EnergyDataDTO{" +
                "deviceIp='" + deviceIp + '\'' +
                ", voltage=" + voltage +
                ", frequency=" + frequency +
                ", current=" + current +
                ", activePower=" + activePower +
                ", apparentPower=" + apparentPower +
                ", reactivePower=" + reactivePower +
                ", powerFactor=" + powerFactor +
                ", energyToday=" + energyToday +
                ", energyYesterday=" + energyYesterday +
                ", energyTotal=" + energyTotal +
                ", address=" + address +
                ", timestamp=" + timestamp +
                '}';
    }
}
