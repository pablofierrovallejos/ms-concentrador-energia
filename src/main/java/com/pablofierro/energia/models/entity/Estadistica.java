package com.pablofierro.energia.models.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicionenergia")
public class Estadistica implements Serializable {
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private static final long serialVersionUID = 495358581041644038L;

	@Id
	private String name;
	
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
