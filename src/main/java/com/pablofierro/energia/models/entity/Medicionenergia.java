package com.pablofierro.energia.models.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name="medicionenergia")
public class Medicionenergia implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idregistro;
	
	private String nombrenodo;
	
	private String uptime;
	private String volts;
	private String current;
	private String power;
	private String energy;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechameas;


	public Long getIdregistro() {
		return idregistro;
	}
	public Date getFechameas() {
		return fechameas;
	}
	public void setFechameas(Date fechameas) {
		this.fechameas = fechameas;
	}
	public void setIdregistro(Long idregistro) {
		this.idregistro = idregistro;
	}
	public String getNombrenodo() {
		return nombrenodo;
	}
	public void setNombrenodo(String nombrenodo) {
		this.nombrenodo = nombrenodo;
	}
	public String getUptime() {
		return uptime;
	}
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}
	public String getVolts() {
		return volts;
	}
	public void setVolts(String volts) {
		this.volts = volts;
	}
	public String getCurrent() {
		return current;
	}
	public void setCurrent(String current) {
		this.current = current;
	}
	public String getPower() {
		return power;
	}
	public void setPower(String power) {
		this.power = power;
	}
	public String getEnergy() {
		return energy;
	}
	public void setEnergy(String energy) {
		this.energy = energy;
	}
	
	
	

}
