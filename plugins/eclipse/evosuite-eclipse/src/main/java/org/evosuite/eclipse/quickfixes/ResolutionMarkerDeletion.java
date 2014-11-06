package org.evosuite.eclipse.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

public class ResolutionMarkerDeletion implements IMarkerResolution {

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return "Delete Marker";
	}

	@Override
	public void run(IMarker marker) {
		// TODO Auto-generated method stub
		try {
			marker.delete();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
