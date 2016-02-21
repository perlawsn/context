package org.dei.perla.context;

import java.util.List;

public interface IComposerManager {
	
	public void composeBlock(Context c);

	public void addPossibleContext(Context ctx);

	public Context getPossibleContext(String ctxName);

	public void removePossibleContext(Context toDetect);

	public List<Context> getPossibleContexts();

}
