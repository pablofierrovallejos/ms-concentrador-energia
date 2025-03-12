package com.pablofierro.energia.controllers;

import java.util.List;

public class BodyResp {
	private String name;
	private List<Nodo> series;
	
	
	public BodyResp(String name, List<Nodo> series) {
		super();
		this.name = name;
		this.series = series;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Nodo> getSeries() {
		return series;
	}
	public void setSeries(List<Nodo> series) {
		this.series = series;
	}

}

