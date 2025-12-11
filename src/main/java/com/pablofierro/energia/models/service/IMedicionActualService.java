package com.pablofierro.energia.models.service;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pablofierro.energia.models.entity.MedicionenergiaActual;

public interface IMedicionActualService extends JpaRepository<MedicionenergiaActual, String> {
	// Spring Data JPA genera automáticamente el método findById que busca por nombrenodo (la PK)
}
