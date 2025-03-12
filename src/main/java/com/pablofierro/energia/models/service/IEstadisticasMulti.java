package com.pablofierro.energia.models.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.pablofierro.energia.models.entity.Estadistica;
import com.pablofierro.energia.models.entity.EstadisticaMulti;

public interface IEstadisticasMulti extends JpaRepository<EstadisticaMulti, String>{
	
	
	@Query(value="{ call sp_estadisticaMeasMes2(:nodo, :fecha) }", nativeQuery = true)
	public List<EstadisticaMulti> consultarEstadisticaConsumoMesMulti(String nodo, String fecha);
	
}
