CREATE DEFINER=`root`@`%` PROCEDURE `sp_estadisticaMeas`(
    IN nodo varchar(10),
	IN fecventain varchar(10)
)
BEGIN

  DECLARE dfecventain date;
  SET dfecventain = (SELECT STR_TO_DATE(fecventain, '%Y-%m-%d'));

#SELECT  power, HOUR(fechameas) , minute(fechameas), second(fechameas)
SELECT   CONCAT( HOUR(fechameas) , ':', minute(fechameas), ':' ,second(fechameas))  as name, power as value
FROM medicionenergia 
	where YEAR(fechameas) = YEAR(dfecventain)
	and  MONTH(fechameas) = MONTH(dfecventain)
    and  DAY(fechameas) = DAY(dfecventain)
    and nombrenodo = nodo
    and power < 10000
	#group by power, HOUR(fechameas) , minute(fechameas) , second(fechameas) order by HOUR(fechameas) asc, minute(fechameas) asc, second(fechameas) asc;
     order by fechameas;
END