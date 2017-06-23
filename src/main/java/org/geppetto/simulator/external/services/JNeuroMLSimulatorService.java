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
 * Wrapper class for jNeuroML
 * 
 * @author Padraig Gleeson
 * @author Jesus R Martinez (jesus@metacell.us)
 * @author mcantarelli
 *
 */
@Service
public class JNeuroMLSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected File filePath = null;

	private static Log logger = LogFactory.getLog(JNeuroMLSimulatorService.class);

	@Autowired
	private SimulatorConfig jneuromlSimulatorConfig;

	@Autowired
	private ExternalSimulatorConfig jneuromlExternalSimulatorConfig;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess)
			throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);

		if(model instanceof ExternalDomainModel)
		{
			originalFileName = (String) model.getDomainModel();
            System.out.println("originalFileNameoriginalFileNameoriginalFileName "+originalFileName);
		}
		else
		{
			throw new GeppettoExecutionException("Unexpected domain model inside jNeuroML Simulator service");
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
			commands = new String[] { "mkdir results", File.separator + "jnml.bat " + filePath.getAbsolutePath() };
		}
		else
		{
			commands = new String[] { "mkdir results", "jnml " + filePath.getAbsolutePath()+" -nogui"};
		}

		logger.info("Command to Execute: " + commands + " ...");

	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("jNeuroML")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public String getName()
	{
		return this.jneuromlSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.jneuromlSimulatorConfig.getSimulatorID();
	}

	@Override
	public String getSimulatorPath()
	{
		return this.jneuromlExternalSimulatorConfig.getSimulatorPath();
	}

	/**
	 * @param jneuromlSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setJNeuroMLSimulatorConfig(SimulatorConfig jneuromlSimulatorConfig)
	{
		this.jneuromlSimulatorConfig = jneuromlSimulatorConfig;
	}

	/**
	 * @param jneuromlExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setJNeuroMLExternalSimulatorConfig(ExternalSimulatorConfig jneuromlExternalSimulatorConfig)
	{
		this.jneuromlExternalSimulatorConfig = jneuromlExternalSimulatorConfig;
	}

}
