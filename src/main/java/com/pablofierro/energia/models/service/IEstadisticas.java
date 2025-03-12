package com.pablofierro.energia.models.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.pablofierro.energia.models.entity.Estadistica;
import com.pablofierro.energia.models.entity.EstadisticaMulti;

public interface IEstadisticas extends JpaRepository<Estadistica, String>{
	
	@Query(value="{ call sp_estadisticaMeas(:nodo, :fecha) }", nativeQuery = true)
	public List<Estadistica> consultarEstadistica(String nodo, String fecha);
	
	@Query(value="{ call sp_estadisticaMeasMes(:nodo, :fecha) }", nativeQuery = true)
	public List<Estadistica> consultarEstadisticaConsumoMes(String nodo, String fecha);
	
	
}
