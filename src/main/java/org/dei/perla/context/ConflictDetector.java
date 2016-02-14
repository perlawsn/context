package org.dei.perla.context;

import java.util.List;

public class ConflictDetector implements IConflictDetector {

	@Override
	public boolean isInConflict(Context context, List<Context> activeContexts) {
		
		return false;
	}

}
