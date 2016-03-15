package org.dei.perla.context;

import java.util.List;

public interface IConflictDetector {

	boolean isInConflict(Context context, List<Context> activeContexts);
	
	boolean isInConflict(Context a, Context b);
	
}
