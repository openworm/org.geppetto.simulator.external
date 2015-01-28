package org.geppetto.simulator.external.services;

import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.simulator.AExternalProcessSimulator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Neuron Simulator 
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
public class NeuronSimulator extends AExternalProcessSimulator{

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;
	
	@Override
	public String getName() {
		return this.neuronSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId() {
		return this.neuronSimulatorConfig.getSimulatorID();
	}
}
