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
@Table(name="websocket_notifications_temperatura")
public class WebSocketNotificationTemperatura implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="nombrenodo", nullable = false)
	private String nombrenodo;
	
	@Column(name="temperatura", nullable = false)
	private Double temperatura;
	
	@Column(name="fechameas", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechameas;
	
	@Column(name="device_ip")
	private String deviceIp;
	
	@Column(name="accion")
	private String accion = "INSERT";
	
	@Column(name="procesado")
	private Boolean procesado = false;
	
	@Column(name="fecha_creacion")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaCreacion;
	
	@Column(name="fecha_procesado")
	@Temporal(TemporalType.TIMESTAMP)
	private Date fechaProcesado;
	
	@Column(name="idregistro")
	private Long idregistro;

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

	public Date getFechameas() {
		return fechameas;
	}

	public void setFechameas(Date fechameas) {
		this.fechameas = fechameas;
	}

	public String getDeviceIp() {
		return deviceIp;
	}

	public void setDeviceIp(String deviceIp) {
		this.deviceIp = deviceIp;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public Boolean getProcesado() {
		return procesado;
	}

	public void setProcesado(Boolean procesado) {
		this.procesado = procesado;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Date getFechaProcesado() {
		return fechaProcesado;
	}

	public void setFechaProcesado(Date fechaProcesado) {
		this.fechaProcesado = fechaProcesado;
	}

	public Long getIdregistro() {
		return idregistro;
	}

	public void setIdregistro(Long idregistro) {
		this.idregistro = idregistro;
	}
}
