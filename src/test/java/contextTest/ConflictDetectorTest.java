package contextTest;

public class ConflictDetectorTest {

	public final String cdt = new String(
			"CREATE DIMENSION Location " +
				"CREATE CONCEPT office WHEN location:string = 'office' " + 
				"CREATE CONCEPT meeting_room WHEN location:string = 'meeting_room' " +
			"CREATE DIMENSION Smoke " +
				"CREATE CONCEPT none WHEN smoke:float < 0.4 " +
					"EVALUATED ON 'EVERY 30 m SELECT smoke:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (smoke)' " +
					"WITH ENABLE COMPONENT: 'SET alarm = false ON 123' " +
					"WITH REFRESH COMPONENT: 20 m " +
				"CREATE CONCEPT little WHEN smoke:float >= 0.4 AND smoke:float <= 1 " + 
					"EVALUATED ON 'EVERY 5 m SELECT smoke:float SAMPLING EVERY 5 m EXECUTE IF EXISTS (smoke)' " +
					"WITH ENABLE COMPONENT: 'EVERY 30 m SELECT smoke:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (smoke)' " +
					"WITH REFRESH COMPONENT: 10 m " +
				"CREATE CONCEPT persistent WHEN smoke:float > 1 " + 
					"EVALUATED ON 'EVERY 1 m SELECT smoke:float SAMPLING EVERY 30 s EXECUTE IF EXISTS (smoke)' " +
					"WITH ENABLE COMPONENT: 'SET alarm = TRUE on 10' " +
					"WITH DISABLE COMPONENT: 'SET alarm = FALSE on 20' " +
					"WITH REFRESH COMPONENT: 5 m " +
			"CREATE DIMENSION Env_Temp " +
				"CREATE CONCEPT cold WHEN temperature:float < 18 " +
					"EVALUATED ON 'EVERY 30 m SELECT temperature:float SAMPLING EVERY 20 m EXECUTE IF EXISTS (temperature)' " +
					"WITH ENABLE COMPONENT: 'EVERY 10 m SELECT temperature:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (temperature)' " +
					"WITH REFRESH COMPONENT: 30 m " +
				"CREATE CONCEPT mild WHEN temperature:float >= 0.4 AND temperature:float >= 1 " + 
					"EVALUATED ON 'EVERY 30 m SELECT temperature:float SAMPLING EVERY 40 m EXECUTE IF EXISTS (temperature)' " +
					"WITH ENABLE COMPONENT: 'EVERY 10 m SELECT temperature:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (temperature)' " +
					"WITH REFRESH COMPONENT: 30 m " +
				"CREATE CONCEPT hot WHEN temperature:float >= 24 " +
					"EVALUATED ON 'EVERY 30 m SELECT temperature:float SAMPLING EVERY 20 m EXECUTE IF EXISTS (temperature)' " +
					"WITH ENABLE COMPONENT: 'EVERY 10 m SELECT temperature:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (temperature)' " +
					"WITH REFRESH COMPONENT: 30 m "); 

	public final String normalString = new String(
			"CREATE CONTEXT normal " +
			"ACTIVE IF Env_Temp = mild AND Smoke = none "
			+ "AND Humidity.h_value <= 45");
	
	public final String fireString = new String(
			"CREATE CONTEXT fire " +
			"ACTIVE IF Env_Temp = hot AND Smoke = persistent ");
	
	
}
