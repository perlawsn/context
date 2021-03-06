package contextTest;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.ComposerManager;
import org.dei.perla.context.ConflictDetector;
import org.dei.perla.context.Context;
import org.dei.perla.context.ContextManager;
import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.Plugin;
import org.dei.perla.core.channel.http.HttpChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.lang.parser.Parser;
import org.dei.perla.lang.parser.StatementParseException;
import org.dei.perla.lang.query.statement.IfEvery;
import org.dei.perla.lang.query.statement.Refresh;
import org.dei.perla.lang.query.statement.RefreshType;
import org.dei.perla.lang.query.statement.SamplingIfEvery;
import org.dei.perla.lang.query.statement.SelectionStatement;
import org.dei.perla.lang.query.statement.Statement;
import org.junit.Before;
import org.junit.Test;


public class ContextManagerTest {

	public final String cdt = new String(
			"CREATE DIMENSION Location " +
				"CREATE CONCEPT office WHEN location:string = 'office' " + 
				"CREATE CONCEPT meeting_room WHEN location:string = 'meeting_room' " +
			"CREATE DIMENSION Smoke " +
				"CREATE CONCEPT none WHEN smoke:float < 0.4 " +
					"EVALUATED ON 'EVERY 30 m SELECT smoke:float SAMPLING EVERY 10 m EXECUTE IF EXISTS (smoke)' " +
					"WITH ENABLE COMPONENT: 'EVERY 30 m SELECT smoke:float SAMPLING EVERY 15 m EXECUTE IF EXISTS (smoke)' " +
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
					"WITH REFRESH COMPONENT: 30 m " +
			"CREATE DIMENSION Humidity " +
				"CREATE CONCEPT h_level " +
					"WITH ENABLE COMPONENT: 'EVERY 10 m SELECT humidity:float SAMPLING EVERY 1 m' " +
					"CREATE ATTRIBUTE $h_value EVALUATED ON 'EVERY 30 m SELECT AVG(humidity:float, 10 m) " +
					"SAMPLING EVERY 10 m EXECUTE IF EXISTS(humidity) '"); 

	public final String normalString = new String(
			"CREATE CONTEXT normal " +
			"ACTIVE IF Env_Temp = mild AND Smoke = none "
			+ "AND Humidity.h_value <= 45");
	
	public final String fireString = new String(
			"CREATE CONTEXT fire " +
			"ACTIVE IF Env_Temp = hot AND Smoke = persistent ");
	
	public final String overheatMonitoringString = new String(
			"CREATE CONTEXT overheatMonitoring " +
			"ACTIVE IF Location = office AND Humidity.h_level > 65 AND Env_Temp = hot ");
	
	public final String ventilationMonitoringString = new String(
			"CREATE CONTEXT ventilationMonitoring " +
			"ACTIVE IF Location = office AND Env_Temp = cold "); 
	
	private CMSimulator ctxManager;
	private static final List<Plugin> plugins;
	  static {
	        List<Plugin> ps = new ArrayList<>();
	        ps.add(new HttpChannelPlugin());
	        ps.add(new SimulatorChannelPlugin());
	        plugins = Collections.unmodifiableList(ps);
	    }
	  PerLaSystem system = new PerLaSystem(plugins);
	  
	@Before
	public void init() throws ParseException{
		ctxManager = new CMSimulator(system, new ComposerManager(), new ConflictDetector());
		ctxManager.createCDT(cdt);
	}

	@Test
	public void addDimension(){
		CDT cdt = ctxManager.getCDT();
		ctxManager.addDimension("ADD DIMENSION Test1 " +
				"CREATE CONCEPT office WHEN location:string = 'office' " + 
				"CREATE CONCEPT meeting_room WHEN location:string = 'meeting_room' ");
		Dimension dim = cdt.getDimByName("Test1");
		assertTrue(dim.containsConcept("office"));
		assertTrue(dim.containsConcept("meeting_room"));
		assertTrue(ctxManager.getCache().containsKey("Test1"));
		Map<String, Integer> handlers = ctxManager.getHandlers();
		assertTrue(handlers.containsKey("Test1.office"));
		assertTrue(handlers.containsKey("Test1.meeting_room"));

		ctxManager.addDimension("ADD DIMENSION Test2 " +
				"CREATE ATTRIBUTE $attributo AS getIdCompagnia()");
		assertTrue(ctxManager.getCache().containsKey("Test2.attributo"));
		
		ctxManager.removeDimension("REMOVE DIMENSION Test2");
		assertNull(cdt.getDimByName("Test2"));
		assertFalse(ctxManager.getCache().containsKey("Test2"));
		assertFalse(ctxManager.getCache().containsKey("Test2.attributo"));
	}
	
	@Test
	public void composeBlockTest() throws org.dei.perla.context.parser.ParseException, StatementParseException {
		ctxManager.createContexts(normalString);
		CDT cdt = ctxManager.getCDT();
		assertThat(cdt.getDimensions().size(), equalTo(4));
		Context normal = ctxManager.getComposerManager().getPossibleContext("normal");

		assertTrue(normal.getDisable().isEmpty());
		Refresh refresh = normal.getRefresh();
		assertTrue(refresh.getType() == RefreshType.TIME);
		assertThat(refresh.getDuration(), equalTo(Duration.of(20, ChronoUnit.MINUTES)));
		SelectionStatement sel = (SelectionStatement) normal.getEnable().get(0);
		
		String enable = new String("EVERY 10 m SELECT humidity:float, temperature:float, smoke:float "
				+ "SAMPLING EVERY 1 m "
				+ "EXECUTE IF EXISTS(humidity) AND EXISTS(temperature) AND EXISTS(smoke)");
		Parser queryParser = new Parser(null);
		Statement result = queryParser.parser(enable);
		assertTrue(result instanceof SelectionStatement);
		SelectionStatement selResult = (SelectionStatement) result;
		assertThat(sel.getEvery(), equalTo(selResult.getEvery()));
		SamplingIfEvery selSampling = (SamplingIfEvery) sel.getSampling();
		SamplingIfEvery resultSampling = (SamplingIfEvery) selResult.getSampling();
		assertTrue(selSampling.getIfEvery().size() == 1);
		IfEvery selIfEvery = selSampling.getIfEvery().get(0);
		IfEvery resultIfEvery = resultSampling.getIfEvery().get(0);
				assertThat(selIfEvery.getValue(), equalTo(resultIfEvery.getValue()));
		assertThat(selIfEvery.getUnit(), equalTo(resultIfEvery.getUnit()));
		assertThat(sel.getSelect().getGroupBy(), equalTo(selResult.getSelect().getGroupBy()));
		assertThat(sel.getTerminate(), equalTo(selResult.getTerminate()));
		assertThat(sel.getWhere(), equalTo(selResult.getWhere()));
	}
		
	@Test
	public void initCache() {
		Map<String, Object> cache = ctxManager.getCache();
		assertTrue(cache.containsKey("Location"));
		assertTrue(cache.containsKey("Smoke"));
		assertTrue(cache.containsKey("Env_Temp"));
		Map<String, Integer> handlers = ctxManager.getHandlers();
		assertTrue(handlers.containsKey("Location.office"));
		assertTrue(handlers.containsKey("Location.meeting_room"));
		
		//aggiungo dei valori in cache
		//ctxManager.addConceptToCache("Location", "Pista1");
	}
	
	@Test
	public void detectContext() throws org.dei.perla.context.parser.ParseException {
		ctxManager.createContexts(ventilationMonitoringString);
		Context ventilationMonitoring = ctxManager.getComposerManager().getPossibleContext("ventilationMonitoring");
		ctxManager.startDetectingContext(ventilationMonitoring);
	}
	
	@Test 
	public void livigno(){
		
	}
}
