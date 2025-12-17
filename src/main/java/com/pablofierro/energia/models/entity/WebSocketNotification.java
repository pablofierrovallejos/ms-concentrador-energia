package com.pablofierro.energia.models.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "websocket_notifications")
public class WebSocketNotification implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombrenodo", nullable = false, length = 50)
    private String nombrenodo;
    
    @Column(name = "accion", nullable = false, length = 20)
    private String accion;
    
    @Column(name = "idregistro")
    private Long idregistro;
    
    @Column(name = "volts", length = 50)
    private String volts;
    
    @Column(name = "current", length = 50)
    private String current;
    
    @Column(name = "power", length = 50)
    private String power;
    
    @Column(name = "energy", length = 50)
    private String energy;
    
    @Column(name = "fechameas")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechameas;
    
    @Column(name = "procesado", nullable = false)
    private Boolean procesado = false;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    
    @Column(name = "fecha_procesado")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaProcesado;
    
    // Constructors
    public WebSocketNotification() {
    }
    
    // Getters and Setters
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
    
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        this.accion = accion;
    }
    
    public Long getIdregistro() {
        return idregistro;
    }
    
    public void setIdregistro(Long idregistro) {
        this.idregistro = idregistro;
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
    
    public Date getFechameas() {
        return fechameas;
    }
    
    public void setFechameas(Date fechameas) {
        this.fechameas = fechameas;
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
}
