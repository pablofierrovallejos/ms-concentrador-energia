package com.pablofierro.energia.models.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicionenergia")
public class EstadisticaMulti implements Serializable {
	

	private static final long serialVersionUID = 495358581041644038L;

	@Id
	private String name;
	
	private String value1;
	
	private String value2;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}
	
	
}