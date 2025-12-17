package com.pablofierro.energia.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pablofierro.energia.services.WebSocketNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


import com.pablofierro.energia.models.entity.Estadistica;
import com.pablofierro.energia.models.entity.EstadisticaMulti;
import com.pablofierro.energia.models.entity.Medicionenergia;
import com.pablofierro.energia.models.entity.MedicionenergiaActual;
import com.pablofierro.energia.models.service.IEstadisticas;
import com.pablofierro.energia.models.service.IEstadisticasMulti;
import com.pablofierro.energia.models.service.IMedicionService;
import com.pablofierro.energia.models.service.IMedicionActualService;


@RestController
public class MeasController {
	@Autowired
	private IMedicionService medicionService;
	@Autowired
	private IMedicionActualService medicionActualService;
	@Autowired
	private IEstadisticas estadisticameasService;
	@Autowired
	private IEstadisticasMulti estadisticaServiceMulti;
	@Autowired
	private WebSocketNotificationService notificationService;
	
	@GetMapping("/consultar-measures/{snodo}/{sfecha}")
	public ResponseEntity<List<Medicionenergia>> consultarMeasuresEnergy(@PathVariable String snodo, @PathVariable String sfecha){
		System.out.println("/consultarMeasuresEnergy");
		return ResponseEntity.ok(medicionService.consultarMeasEnergia(snodo, sfecha));
	}
	
	@GetMapping("/consultar-estadistica/{snodo}/{sfecha}")
	public ResponseEntity<Medicionenergia> consultarEstadisticaEnergy(@PathVariable String snodo, @PathVariable String sfecha){
		System.out.println("[V2 ULTRA FAST + WEBSOCKET] /consultar-estadistica - Última medición nodo: " + snodo);
		
		// Query ULTRA RÁPIDA desde tabla con solo 4 registros (busca por PK)
		MedicionenergiaActual actual = medicionActualService.findById(snodo).orElse(null);
		if (actual == null) {
			return ResponseEntity.notFound().build();
		}
		
		// Mapear a la entidad original para compatibilidad con frontend
		Medicionenergia medicion = new Medicionenergia();
		medicion.setIdregistro(actual.getIdregistro());
		medicion.setNombrenodo(actual.getNombrenodo());
		medicion.setUptime(actual.getUptime());
		medicion.setVolts(actual.getVolts());
		medicion.setCurrent(actual.getCurrent());
		medicion.setPower(actual.getPower());
		medicion.setEnergy(actual.getEnergy());
		medicion.setFechameas(actual.getFechameas());
		
		// NUEVO: Enviar notificación WebSocket al frontend
		// Los clientes suscritos recibirán esta actualización automáticamente
		notificationService.notificarCambioEstadistica(snodo, sfecha, "Consulta de estadística realizada");
		
		return ResponseEntity.ok(medicion);
	}
	
	
	@GetMapping("/consultar-consumo-mes/{snodo}/{sfecha}")
	public ResponseEntity<List<Estadistica>> consultarEstadisticaEnergyMes(@PathVariable String snodo, @PathVariable String sfecha){
		System.out.println("/consultar-estadistica");
		return ResponseEntity.ok(estadisticameasService.consultarEstadisticaConsumoMes(snodo, sfecha));
	}
	
	@GetMapping("/consultar-consumo-mes2/{snodo}/{sfecha}")
	public ResponseEntity<List<BodyResp>> consultarEstadisticaEnergyMes2(@PathVariable String snodo, @PathVariable String sfecha){
		    System.out.println("/consultar-estadistica");
				   

		    List<EstadisticaMulti> lmeas1 = estadisticaServiceMulti.consultarEstadisticaConsumoMesMulti("Meas1", sfecha);	//Lista con resultado de la base de datos
	
		    List<BodyResp> respf = new ArrayList<BodyResp>();												//Lista a llenar con respuesta

		    for(EstadisticaMulti estadistica: lmeas1) {
			    List<Nodo> ln0  = new ArrayList<Nodo>(); 
			    BodyResp br;
			    
			    if(estadistica.getValue1() == null) {
			    	ln0.add(new Nodo("Meas1",  "0"  ));
			    }else {
			    	ln0.add(new Nodo("Meas1",estadistica.getValue1())); 
			    }
		    	
			    if(estadistica.getValue2() == null) {
			    	ln0.add(new Nodo("Meas2","0"));
			    }else {
			    	ln0.add(new Nodo("Meas2",estadistica.getValue2()));
			    }

		    	
		    	br  = new BodyResp(estadistica.getName(), ln0 );
		    	respf.add(br);
		    	
		    	System.out.println(estadistica.getName());
		    	System.out.println(estadistica.getValue1());
		    	System.out.println(estadistica.getValue2());
		    		
		    }

		    /*
		    List<Nodo> ln1 = new ArrayList<Nodo>(); 
		    ln1.add(new Nodo("Meas1","100")); ln1.add(new Nodo("Meas2","110"));
		    
		    List<Nodo> ln2 = new ArrayList<Nodo>(); 
		    ln2.add(new Nodo("Meas1","120")); ln2.add(new Nodo("Meas2","130"));
		    
		    List<Nodo> ln3 = new ArrayList<Nodo>(); 
		    ln3.add(new Nodo("Meas1","140")); ln3.add(new Nodo("Meas2","150"));
		    
		    BodyResp br  = new BodyResp("2023-10-01", ln1 );
		    BodyResp br1  = new BodyResp("2023-10-02", ln2 );
		    BodyResp br2  = new BodyResp("2023-10-03", ln3 );
		    
	
		    
		    respf.add(br);
		    respf.add(br1);
		    respf.add(br2);
		    
		    */
		    return new ResponseEntity<List<BodyResp>>(respf, HttpStatus.CREATED);
	}
	
	
	

	
	
}
