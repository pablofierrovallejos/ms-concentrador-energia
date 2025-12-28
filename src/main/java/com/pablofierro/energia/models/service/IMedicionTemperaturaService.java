package com.pablofierro.energia.models.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pablofierro.energia.models.entity.MedicionTemperatura;

import jakarta.transaction.Transactional;

public interface IMedicionTemperaturaService extends JpaRepository<MedicionTemperatura, Long> {

	/**
	 * Inserta una medición de temperatura usando stored procedure
	 */
	@Modifying
	@Query(value="{ call sp_insertarMedicionTemperatura(:nombrenodo, :temperatura, :deviceIp) }", nativeQuery = true)
	@Transactional
	Object agregarMedicionTemperatura(
		@Param("nombrenodo") String nombrenodo, 
		@Param("temperatura") Double temperatura,
		@Param("deviceIp") String deviceIp
	);
	
	/**
	 * Obtiene las últimas N mediciones de un nodo específico
	 */
	@Query(value="SELECT * FROM medicion_temperatura WHERE nombrenodo = :nodo ORDER BY fechahora DESC LIMIT :limit", nativeQuery = true)
	List<MedicionTemperatura> obtenerUltimasMediciones(
		@Param("nodo") String nodo, 
		@Param("limit") int limit
	);
	
	/**
	 * Obtiene todas las mediciones de un nodo en un rango de fechas
	 */
	@Query(value="SELECT * FROM medicion_temperatura WHERE nombrenodo = :nodo AND DATE(fechahora) = :fecha ORDER BY fechahora DESC", nativeQuery = true)
	List<MedicionTemperatura> obtenerMedicionesPorFecha(
		@Param("nodo") String nodo, 
		@Param("fecha") String fecha
	);
}
