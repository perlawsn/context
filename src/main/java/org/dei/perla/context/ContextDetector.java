package org.dei.perla.context;



import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.dei.perla.cdt.CDTUtils;
import org.dei.perla.cdt.Concept;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.cdt.FunctionEvaluatedOn;
import org.dei.perla.cdt.QueryEvaluatedOn;
import org.dei.perla.lang.executor.QueryException;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.expression.LogicValue;
import org.dei.perla.lang.query.statement.Statement;

public class ContextDetector {
	
	private final ScheduledExecutorService scheduler;
	private final Context context;
	private ScheduledFuture<?> timer;
	
    private static final int STOPPED = 0;
    private static final int RUNNING = 1;
    
    private int status;
	private Map<String, Set<Object>> cache;
	
	public ContextDetector(Context context, Map<String, Set<Object>> cache){
		this.context = context;
		scheduler = Executors.newSingleThreadScheduledExecutor();
		status = STOPPED;
		this.cache = cache; 
	}
	
	public void checkActiveContext(){
		long period = context.getRefresh().getDuration().toMillis();
		timer = scheduler.scheduleAtFixedRate(new CheckCondition(), period, period, TimeUnit.MILLISECONDS);
	}
	
	public void start(){
		if(status != STOPPED)
			return;
		else {
			status = RUNNING;
			checkActiveContext();
		}		
	}
	
   public void stop() {
        if (status != RUNNING) {
            return;
        }

        status = STOPPED;
        stopExecution();
    }

    private void stopExecution() {
        if (timer != null) {
            timer.cancel(false);
            timer = null;
        }
        scheduler.shutdown();
    }	
	
    private final class CheckCondition implements Runnable {
    	
        public void run() {
        	boolean isActive = true;
        	for(ContextElement element: context.getContextElements()){
			if(element instanceof ContextElemSimple){
				ContextElemSimple ces = (ContextElemSimple) element;
				Set concepts = (Set) cache.get(ces.getDimension());
				synchronized(concepts) {
					if(!concepts.contains(ces.getValue())){
						isActive = false;
						break;
					}
				}
			} else if(element instanceof ContextElemAtt){
				ContextElemAtt cea = (ContextElemAtt) element;
				String dimAttName = cea.getDimension() + "." + cea.getAttribute();
				CreateAttr attribute = CDTUtils.getConceptAttribute(cea.getDimension(), cea.getAttribute());
			//gestione retrieval attributi
				
				Object currentValue = cache.get(dimAttName); //valore corrente 
    			Expression e = cea.getExpression();
    			LogicValue v = (LogicValue) e.run(new Object[] {currentValue}, null);
 	            if (!v.toBoolean()) {
 	            	isActive = false;
 	            	break;
 	            }
			}
        	if((context.isActive() && !isActive) || (!context.isActive() && isActive)){
        		System.out.println("Context " + context.getName() + " is active " + isActive);
        		context.setActive(isActive);
        	}
        	}
        }
        

    }
}

