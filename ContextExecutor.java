package org.dei.perla.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private Map<String, StatementTask[] > queriesForContext;
    
    //TO DO Aggiungere creazione e gestione tabella per salvare lo stato di un contesto

	public ContextExecutor(IConflictDetector conflictDetector){
		this.conflictDetector = conflictDetector;
		system = new PerLaSystem(plugins);
		exec = new Executor(system);
		contexts = new ArrayList<Context>();
		activeContexts = new ArrayList<Context>();
		queriesForContext = new HashMap<String, StatementTask[]>();
	}
	
	public void addContextToExecute(Context c){
		contexts.add(c);
	}
	
	public void addActiveContext(Context c){
		activeContexts.add(c);
	}
	
	//when a context changes its status, it must execute its actions by means of the QueryExecutor
	@Override
	public void update(Observable o, Object arg) {
		Context ctx;
		if(o instanceof Context){
			ctx = (Context) o;
		}
		else
			return;
		if(ctx.isActive() && !conflictDetector.isInConflict(ctx, activeContexts)){
			StatementTask[] statTasks = new StatementTask[ctx.getEnable().size()];
			int i=0;
			for(Statement s: ctx.getEnable()) {
				try {
					statTasks[i] = exec.execute(s, new ContextHandler());
				} catch (QueryException e) {
					System.out.println("ERROR during the execution of a query in CONTEXT " + ctx.getName());
					e.printStackTrace();
				}
				i++;
			}
			queriesForContext.put(ctx.getName(), statTasks);	
		} else {
			StatementTask[] tasksToStop = queriesForContext.get(ctx.getName());
			for(int i=0; i < tasksToStop.length; i++) {
		//		tasksToStop.stop();   si spera che Guido lo implementi
			}
			queriesForContext.remove(ctx.getName());
		}
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


