package org.geppetto.simulator.external.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ExternalSimulatorConfig;
import org.geppetto.model.DomainModel;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.ModelFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Wrapper class for NEURON
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 * @author mcantarelli
 *
 */
@Service
public class NeuronSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected File filePath = null;

	private static Log logger = LogFactory.getLog(NeuronSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	@Autowired
	private ExternalSimulatorConfig neuronExternalSimulatorConfig;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess)
			throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);

		if(model instanceof ExternalDomainModel)
		{
			originalFileName = (String) model.getDomainModel();
		}
		else
		{
			throw new GeppettoExecutionException("Unexpected domain model inside NEURON Simulator service");
		}
		this.createCommands(this.originalFileName);
		
	}

	/**
	 * Creates command to be executed by an external process
	 * 
	 * @param originalFileName
	 * @param aspect
	 */
	public void createCommands(String originalFileName)
	{
		filePath = new File(originalFileName);

		logger.info("Creating command to run " + originalFileName);
		directoryToExecuteFrom = filePath.getParentFile().getAbsolutePath();
		outputFolder = directoryToExecuteFrom;

		if(Utilities.isWindows())
		{
			commands = new String[] { getSimulatorPath() + "rxvt.exe -e " + getSimulatorPath() + "sh " + getSimulatorPath().replace("/bin/", "/lib/") + "mknrndll.sh",
					getSimulatorPath() + "mkdir.exe results", getSimulatorPath() + "nrniv.exe -python " + filePath.getAbsolutePath() };
		}
		else
		{
			commands = new String[] { getSimulatorPath() + "nrnivmodl", "mkdir results", getSimulatorPath() + "nrniv -python " + filePath.getAbsolutePath() };
		}

		logger.info("Command to Execute: " + commands + " ...");
		logger.info("From directory : " + directoryToExecuteFrom);

	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEURON")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public String getName()
	{
		return this.neuronSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.neuronSimulatorConfig.getSimulatorID();
	}

	@Override
	public String getSimulatorPath()
	{
		return this.neuronExternalSimulatorConfig.getSimulatorPath();
	}

	/**
	 * @param neuronSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronSimulatorConfig(SimulatorConfig neuronSimulatorConfig)
	{
		this.neuronSimulatorConfig = neuronSimulatorConfig;
	}

	/**
	 * @param neuronExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronExternalSimulatorConfig(ExternalSimulatorConfig neuronExternalSimulatorConfig)
	{
		this.neuronExternalSimulatorConfig = neuronExternalSimulatorConfig;
	}

}
