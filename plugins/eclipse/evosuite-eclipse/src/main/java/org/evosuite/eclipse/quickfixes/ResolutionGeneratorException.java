package org.evosuite.eclipse.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class ResolutionGeneratorException implements IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		// TODO Auto-generated method stub
		return new IMarkerResolution[] {
				new ResolutionMarkerDeletion(),
				new ResolutionMarkerEvoIgnoreForClass(),
				new ResolutionMarkerEvoIgnoreForMethod(),
				new ResolutionMarkerThrowsException(),
				new ResolutionMarkerTryBlock()
		};
	}

}
