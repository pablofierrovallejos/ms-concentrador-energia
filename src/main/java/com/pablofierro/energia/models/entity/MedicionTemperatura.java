package com.pablofierro.energia.models.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name="medicion_temperatura")
public class MedicionTemperatura implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="nombrenodo", nullable = false, length = 50)
	private String nombrenodo;
	
	@Column(name="temperatura", nullable = false)
	private Double temperatura;
	
	@Column(name="fechahora")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechahora;
	
	@Column(name="device_ip", length = 15)
	private String deviceIp;

	// Getters y Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Date getFechahora() {
		return fechahora;
	}

	public void setFechahora(Date fechahora) {
		this.fechahora = fechahora;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	@Override
	public String toString() {
		return "MedicionTemperatura [id=" + id + ", nombrenodo=" + nombrenodo + ", temperatura=" + temperatura
				+ ", fechahora=" + fechahora + ", deviceIp=" + deviceIp + "]";
	}
}
