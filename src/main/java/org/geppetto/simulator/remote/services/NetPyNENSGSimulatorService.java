package org.geppetto.simulator.remote.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.ModelFormat;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private SimulatorConfig netPyNENSGSimulatorConfig;
	
	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NETPYNE")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}
	
	@Override
	public String getName()
	{
		return this.netPyNENSGSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.netPyNENSGSimulatorConfig.getSimulatorID();
	}


	/**
	 * @param neuronSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronSimulatorConfig(SimulatorConfig netPyNENSGSimulatorConfig)
	{
		this.netPyNENSGSimulatorConfig = netPyNENSGSimulatorConfig;
	}


	@Override
	public void processFailed(String message, Exception e) {
		this.getListener().externalProcessFailed(message, e);		
	}
	
}
