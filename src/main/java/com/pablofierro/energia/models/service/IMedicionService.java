package com.pablofierro.energia.models.service;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.pablofierro.energia.models.entity.Medicionenergia;
import jakarta.transaction.Transactional;

public interface IMedicionService extends JpaRepository<Medicionenergia, String>{

	@Modifying
	@Query(value="{ call sp_insertarMedicionEnergia(:idmuestra, :nombrenodo, :uptime, :volts, :current, :power, :energy ) }", nativeQuery = true)
	@Transactional
	public Object agregarMedicion(String idmuestra, String nombrenodo, String uptime, String volts, String current,
			String power, String energy);
	
	@Query(value="{ call sp_consultarMedicionEnergia(:nodo, :fechareg) }", nativeQuery = true)
	public List<Medicionenergia> consultarMeasEnergia(String nodo, String fechareg);
	
	
}


