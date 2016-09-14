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
 * Wrapper class for NEURON at Neuroscience Gateway
 * 
 * @author Adrian Quintana (adrianquintana@gmail.com)
 *
 */
@Service
public class NeuronNSGSimulatorService extends NSGSimulatorService
{

	@Autowired
	private SimulatorConfig neuronNSGSimulatorConfig;
	
	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEURON")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}
	
	@Override
	public String getName()
	{
		return this.neuronNSGSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.neuronNSGSimulatorConfig.getSimulatorID();
	}


	/**
	 * @param neuronSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronSimulatorConfig(SimulatorConfig neuronSimulatorConfig)
	{
		this.neuronNSGSimulatorConfig = neuronSimulatorConfig;
	}

	@Override
	public void processFailed(String message, Exception e) {
		this.getListener().externalProcessFailed(message, e);		
	}

}
