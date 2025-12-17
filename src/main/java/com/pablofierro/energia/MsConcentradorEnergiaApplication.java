package com.pablofierro.energia;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.pablofierro.energia.models.dto.EnergyDataDTO;
import com.pablofierro.energia.models.service.IMedicionService;
import com.pablofierro.energia.services.CloudSenderService;
import com.pablofierro.energia.services.TasmotaReaderService;

@SpringBootApplication
@EnableScheduling
public class MsConcentradorEnergiaApplication {

	@Autowired(required = false)
	private IMedicionService medicionService;
	
	@Autowired
	private TasmotaReaderService tasmotaReaderService;
	
	@Autowired
	private CloudSenderService cloudSenderService;
	
	@Value("${modbus.devices}")
	private String modbusDevicesConfig;
	
	@Value("${solis.inverter.ip:}")
	private String solisInverterIp;
	
	@Value("${modbus.polling.interval:15000}")
	private long pollingInterval;
	
	public static void main(String[] args) {
		org.slf4j.Logger logger = LoggerFactory.getLogger(MsConcentradorEnergiaApplication.class);
		logger.info("╔════════════════════════════════════════════════════════╗");
		logger.info("║                                                        ║");
		logger.info("║      MEDICION ENERGIA V2 - ULTRA FAST QUERIES         ║");
		logger.info("║      Tabla: medicionenergia_actual (4 registros)      ║");
		logger.info("║                                                        ║");
		logger.info("╚════════════════════════════════════════════════════════╝");
		SpringApplication.run(MsConcentradorEnergiaApplication.class, args);
	}
	
	/**
	 * Scheduler para leer dispositivos Modbus y enviar datos al cloud
	 * El intervalo se configura en application.properties (modbus.polling.interval)
	 */
	@Scheduled(fixedDelayString = "${modbus.polling.interval:15000}")
	public void readModbusDevicesAndSendToCloud() {
		org.slf4j.Logger logger = LoggerFactory.getLogger(MsConcentradorEnergiaApplication.class);
		
		try {
			// Obtener lista de dispositivos desde configuración
			List<String> deviceIps = Arrays.asList(modbusDevicesConfig.split(","));
			
			logger.info("=== Iniciando lectura de {} dispositivos Tasmota ===", deviceIps.size());
			
			// Leer datos de todos los dispositivos
			List<EnergyDataDTO> measurements = tasmotaReaderService.readMultipleDevices(deviceIps);
			
			// Leer datos del inversor Solis Datalogger si está configurado
			if (solisInverterIp != null && !solisInverterIp.trim().isEmpty()) {
				try {
					logger.info("Leyendo datos del inversor Solis Datalogger en {}", solisInverterIp);
					EnergyDataDTO solarData = tasmotaReaderService.readSolarInverterData(solisInverterIp);
					if (solarData != null) {
						// Agregar siempre, incluso si los datos son null (para debug)
						measurements.add(solarData);
						logger.info("Datos del inversor Solis agregados a la lista");
					}
				} catch (Exception e) {
					logger.error("Error leyendo inversor Solis, continuando con otros dispositivos: {}", e.getMessage());
				}
			}
			
			if (measurements.isEmpty()) {
				logger.warn("No se pudieron leer datos de ningún dispositivo");
				return;
			}
			
			logger.info("Datos leídos de {} dispositivos en total", measurements.size());
			
			// Enviar datos al endpoint en la nube
			boolean sent = cloudSenderService.sendMeasurements(measurements);
			
			if (sent) {
				logger.info("Mediciones enviadas exitosamente al cloud");
			} else {
				logger.error("Error al enviar mediciones al cloud");
			}
			
		} catch (Exception e) {
			logger.error("Error en el proceso de lectura y envío de datos: {}", e.getMessage(), e);
		}
	}
	
	// Método desactivado - ya no se usa
	// @Scheduled(fixedRate = 15000)
	public void getProductObjects_OLD() {
		// Este método ya no se ejecuta
		/*
		String concentra_Url1 = "http://192.168.18.205";
		String concentra_Url2 = "http://192.168.18.206";
		
		Document doc1;
		Document doc2;
		
		try {
			
			 SimpleDateFormat formatter = new SimpleDateFormat("yyHHmmss");  
			 Date date = new Date(); 
			 
			System.out.println(formatter.format(date));  
			    
			    
			System.out.println("Concentrdor 1  ------------>");
			doc1 = Jsoup.connect(concentra_Url1).get();
			Elements links1 = doc1.select("p");
			links1.forEach(el1 -> System.out.println("p: " + el1));
			
			String stempv = replacetag(links1.get(1).toString());
			System.out.println("stempv: " + stempv);
			
			if(stempv.equals("0.0")) {
				System.out.println("Measure 0.0 not registered.");
			}else {			
				medicionService.agregarMedicion(formatter.format(date), "Meas1",  replacetag(links1.get(0).toString()),  replacetag(links1.get(1).toString()),  replacetag(links1.get(2).toString()), replacetag(links1.get(3).toString()),  replacetag(links1.get(4).toString()));
			}
			System.out.println("END------------>");
			
			
			
			System.out.println("Concentrador 2 ----------->");
			doc2 = Jsoup.connect(concentra_Url2).get();
			Elements links2 = doc2.select("p");
			links2.forEach(el2 -> System.out.println("p: " + el2));
			
			String stempv2 = replacetag(links2.get(1).toString());
			System.out.println("stempv2: " + stempv2);
			
			if(stempv2.equals("0.0")) {
				System.out.println("Measure 0.0 not registered.");
			}else {			
				medicionService.agregarMedicion(formatter.format(date), "Meas2",  replacetag(links2.get(0).toString()),  replacetag(links2.get(1).toString()),  replacetag(links2.get(2).toString()), replacetag(links2.get(3).toString()),  replacetag(links2.get(4).toString()));
			}
			System.out.println("END------------>");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		
	    Gson gson = new Gson();
	    EweLink eweLink = new EweLink("us", "pablofierrovallejos@gmail.com", "96552333Aa", "+263",60);

	    try {
	        eweLink.login();

	        List<Thing> things = eweLink.getThings();

	        org.slf4j.Logger logger = LoggerFactory.getLogger(MsConcentradorEnergiaApplication.class);
	        
	        logger.info("PRINT DEVICE_ID, NAME, ONLINE, SWITCH, VOLTAGE");
	        for (Thing thing : things) {
	            logger.info("{}, {}, {}, {}, {}",
	                    thing.getItemData().getDeviceid() ,
	                    thing.getItemData().getName() ,
	                    thing.getItemData().getOnline(),
	                    thing.getItemData().getParams().getSwitch(),
	                    thing.getItemData().getParams().getVoltage());
	        }
	        logger.info("PRINT JSON OBJECTS");
	        for (Thing thing : things) {
	            logger.info("{} ",gson.toJson(thing));
	        }

	        eweLink.getWebSocket(new WssResponse() {

	            @Override
	            public void onMessage(String s) {
	                //if you want the raw json data
	                System.out.println("on message in test raw:" + s);

	            }

	            @Override
	            public void onMessageParsed(WssRspMsg rsp) {

	                if (rsp.getError() == null) {

	                    //normal scenario
	                    StringBuilder sb = new StringBuilder();
	                    sb.append("Device:").append(rsp.getDeviceid()).append(" - ");
	                    if (rsp.getParams() != null) {
	                        sb.append("Switch:").append(rsp.getParams().getSwitch()).append(" - ");
	                        sb.append("Voltage:").append(rsp.getParams().getVoltage()).append(" - ");
	                        sb.append("Power:").append(rsp.getParams().getPower()).append(" - ");
	                        sb.append("Current:").append(rsp.getParams().getCurrent()).append(" - ");
	                    }

	                    System.out.println(sb.toString());

	                } else if (rsp.getError() == 0) {
	                    //this is from a login response
	                    System.out.println("login success");
	                } else if (rsp.getError() > 0) {
	                    System.out.println("login error:" + rsp.toString());
	                }
	            }

	            public void onError(String error) {
	                System.out.println("onError in test, this should never be called");
	                System.out.println(error);

	            }
	        });


	        Thread.sleep(10000);
	        System.out.println(eweLink.setDeviceStatus("1000f40d35", "on"));
	        Thread.sleep(5000);
	        System.out.println(eweLink.setDeviceStatus("1000f40d35", "off"));




	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    */
		

	}
	
}
