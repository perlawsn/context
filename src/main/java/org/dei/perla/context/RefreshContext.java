package org.dei.perla.context;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.dei.perla.cdt.CDT;
import org.dei.perla.cdt.CreateAttr;
import org.dei.perla.cdt.Dimension;
import org.dei.perla.lang.query.expression.Expression;
import org.dei.perla.lang.query.expression.LogicValue;

public class RefreshContext {
	
	private final CDT cdt = CDT.getCDT();
	private final ScheduledExecutorService scheduler;
	private final Context context;
	private ScheduledFuture<?> timer;
	
    private static final int STOPPED = 0;
    private static final int RUNNING = 1;
    
    private int status;
	
	public RefreshContext(Context context){
		this.context = context;
		scheduler = Executors.newScheduledThreadPool(3);
		status = STOPPED;
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
        			ContextElemSimple simple = (ContextElemSimple) element;
        			Dimension d = cdt.getDimByName(simple.getDimension());
        			if(d.getCorrentValue()!=simple.getValue()){
        				isActive = false;
        				break;
        			}
        		}
        		else if(element instanceof ContextElemAtt){
        			ContextElemAtt attElem = (ContextElemAtt) element;
        			Dimension d = cdt.getDimByName(attElem.getDimension());
        		    Expression e = attElem.getExpression();
     	            LogicValue v = (LogicValue) e.run(attElem.getSample(), null);
     	            CreateAttr att = cdt.getAttributeOfDim(d.getName(), attElem.getAttribute());
     	            if (v.toBoolean()) {
     	                att.setCurrentValue(true); 
     	            } else {
     	            	att.setCurrentValue(false);
     	            	isActive = false;
     	            	break;
     	            }
        		}
        	}
        	if((context.isActive() && !isActive) || (!context.isActive() && isActive))
        		context.setActive(isActive);
        }

    }
    
}
