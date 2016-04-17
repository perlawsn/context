package org.dei.perla.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.parser.ParseException;
import org.dei.perla.context.ComposerManager;
import org.dei.perla.context.ConflictDetector;
import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.Plugin;
import org.dei.perla.core.channel.http.HttpChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.core.fpc.Attribute;
import org.dei.perla.core.fpc.Fpc;
import org.dei.perla.core.fpc.FpcCreationException;
import org.dei.perla.core.message.json.JsonMapperFactory;
import org.dei.perla.core.message.urlencoded.UrlEncodedMapperFactory;
import org.dei.perla.lang.executor.SimulatorFpc;

import contextTest.CMSimulator;

public class Livigno { 
	
	public static final String fileCDT = "src/test/java/resources/livigno.txt";
	private static final String SLOPE_1_SENSOR ="src/test/java/resources/slope_sensor.xml";
	private static final String SLOPE_2_SENSOR ="src/test/java/resources/slope_sensor2.xml";
	private static final String WEATHER_SENSOR ="src/test/java/resources/weather_livigno.xml";
	private static final String CONTEXTS ="src/test/java/resources/contexts.txt";
    private static final List<Plugin> plugins;
    static {
        List<Plugin> ps = new ArrayList<>();
        ps.add(new JsonMapperFactory());
        ps.add(new UrlEncodedMapperFactory());
        ps.add(new SimulatorMapperFactory());
        ps.add(new HttpChannelPlugin());
        ps.add(new SimulatorChannelPlugin());
        plugins = Collections.unmodifiableList(ps);
    }
	private static CMSimulator ctxManager;
	
   private static Map<Attribute, Object> createDefaultValues() {
	        Map<Attribute, Object> values = new HashMap<>();
	        values.put(CommonAttributes.TEMP_INT, 24);
	        values.put(CommonAttributes.HUM_INT, 12);
	        values.put(Attribute.TIMESTAMP, Instant.now());

	        return values;
	    }
	    private static  SimulatorFpc fpc;
	    private static PerLaSystem system;
	    private static QueryMenager qm;
	    
    public static void main( String[] args ){
    	initSystem();
    	ctxManager = new CMSimulator(system, new ComposerManager(), new ConflictDetector());
    	try {
			ctxManager.createCDTFromFile(fileCDT);
			System.out.println(ctxManager.getCDT());
			
			ctxManager.createContextsFromFile(CONTEXTS);
			ctxManager.startDetectingContext("IcySlope1");
			ctxManager.startDetectingContext("LittleVisibility");
			ctxManager.startDetectingContext("Autumn");
			ctxManager.populateCDT();
	    } catch (ParseException | org.dei.perla.context.parser.ParseException e) {
			e.printStackTrace();
		}
	}
	    
    private static void initSystem(){
    	system= new PerLaSystem(plugins);
        qm = new QueryMenager(system);
    	
       try {
			system.injectDescriptor(new FileInputStream(SLOPE_1_SENSOR));
			system.injectDescriptor(new FileInputStream(SLOPE_2_SENSOR)); 
			system.injectDescriptor(new FileInputStream(WEATHER_SENSOR)); 
		} catch (FileNotFoundException | FpcCreationException e) {
			e.printStackTrace();
		}
        
        for(Fpc f : system.getRegistry().getAll()) {
        	System.out.println("id: "+f.getId()+"\n");
        	for(Attribute a: f.getAttributes())
        		System.out.println("id: "+a.getId()+" type:"+a.getType()+"\n");
        }
    }
}
