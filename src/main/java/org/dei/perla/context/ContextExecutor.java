package org.dei.perla.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.dei.perla.core.PerLaSystem;
import org.dei.perla.core.Plugin;
import org.dei.perla.core.channel.simulator.SimulatorChannelPlugin;
import org.dei.perla.core.channel.simulator.SimulatorMapperFactory;
import org.dei.perla.lang.Executor;
import org.dei.perla.lang.StatementHandler;
import org.dei.perla.lang.StatementTask;
import org.dei.perla.lang.executor.QueryException;
import org.dei.perla.lang.executor.Record;
import org.dei.perla.lang.query.statement.Statement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ContextExecutor implements Observer{

	private IConflictDetector conflictDetector;
	private List<Context> contexts;
	private List<Context> activeContexts;
	
	private final List<Plugin> plugins = Arrays.asList(
            new SimulatorMapperFactory(),
            new SimulatorChannelPlugin()
   	 );
  	private final PerLaSystem system;
    private final Executor exec;
    private Multimap<String, StatementTask> queriesForContext;
    
    //TO DO Aggiungere creazione e gestione tabella per salvare lo stato di un contesto

	public ContextExecutor(IConflictDetector conflictDetector){
		this.conflictDetector = conflictDetector;
		system = new PerLaSystem(plugins);
		exec = new Executor(system);
		contexts = new ArrayList<Context>();
		activeContexts = new ArrayList<Context>();
		queriesForContext = ArrayListMultimap.create();
	}
	
	public List<Context> getContexts(){
		return new ArrayList<Context>(contexts);
	}
	
	public List<Context> getActiveContext(){
		return new ArrayList<Context>(activeContexts);
	}
	
	public void addContextToExecute(Context c){
		contexts.add(c);
	}
	
	public void addActiveContext(Context c){
		activeContexts.add(c);
	}
	
	public void removeActiveContext(Context c){
		int index = getIndexActiveContext(c);
		if (index >= 0)
			activeContexts.remove(c);
	}
	
	private int getIndexActiveContext(Context ctx){
		int i = -1;
		if(ctx == null) 
			return i;
		for(Context c: activeContexts){
			i++;
			if(c.getName().equals(ctx.getName()))
				break;
		}
		return i;
	}
	
	//when a context changes its status, it must execute its actions by means of the QueryExecutor
	@Override
	public void update(Observable o, Object arg) {
		Context ctx;
		if(o instanceof Context){
			ctx = (Context) o;
			executeContextBehaviour(ctx);
		}
		else
			return;
	}
		
	private void executeContextBehaviour(Context ctx){
		for(Context c: activeContexts){
			if(!ctx.isActive()){ 
				stopContext(ctx);
				return;
			} 
			boolean isInConflict = conflictDetector.isInConflict(ctx, c);
			if(ctx.isActive() && !isInConflict){
				executeContext(ctx);
			}
			else if(ctx.isActive() && isInConflict) {
				stopContext(c);
				executeContext(c);
			}
		}	
		return;
	}

	private void executeContext(Context ctx){
		activeContexts.add(ctx);
		StatementTask statTask;
		for(Statement s: ctx.getEnable()) {
			try {
				statTask = exec.execute(s, new ContextHandler());
				queriesForContext.put(ctx.getName(), statTask);
			} catch (QueryException e) {
				System.out.println("ERROR during the execution of a query in CONTEXT " + ctx.getName());
				e.printStackTrace();
			}
		}	
	}

	private void stopContext(Context ctx){
		List<StatementTask> tasksToStop = (List<StatementTask>) queriesForContext.get(ctx.getName());
		for(StatementTask task: tasksToStop) {
			task.stop();
		}
		queriesForContext.removeAll(ctx.getName());
		removeActiveContext(ctx);
	}
	
	class ContextHandler implements StatementHandler {

		public void data(Statement s, Record r) {
			// TODO	
		}

		
		public void complete(Statement s) {
			// TODO Auto-generated method stub
			
		}

		
		public void error(Statement s, Throwable cause) {
			// TODO Auto-generated method stub
			
		}
	}
}


