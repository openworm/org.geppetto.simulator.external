package org.geppetto.simulator.remote.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.ModelFormat;
import org.springframework.stereotype.Service;

/**
 * Wrapper class for NetPyNE at Neuroscience Gateway
 * 
 * @author Adrian Quintana (adrianquintana@gmail.com)
 *
 */
@Service
public class NetPyNENSGSimulatorService extends NSGSimulatorService
{

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NetPyNE")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

}
