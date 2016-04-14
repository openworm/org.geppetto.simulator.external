package org.geppetto.simulator.external.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.RemoteSimulatorConfig;
import org.geppetto.core.utilities.Zipper;
import org.geppetto.model.DomainModel;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.ExternalDomainModel;
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
public class NeuronNSGSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected File filePath = null;

	private static Log logger = LogFactory.getLog(NeuronNSGSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	@Autowired
	private RemoteSimulatorConfig neuronNSGExternalSimulatorConfig;

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
			throw new GeppettoExecutionException("Unexpected domain model inside NEURON NSG Simulator service");
		}
		
		try
		{
			this.createCommands(this.originalFileName);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw new GeppettoExecutionException("Error creating commands for Neuron NSG Simulator service");
		}
	}

	/**
	 * Creates command to be executed by an external process
	 * 
	 * @param originalFileName
	 * @param aspect
	 * @throws IOException 
	 */
	public void createCommands(String originalFileName) throws IOException
	{
		logger.info("Creating command to run " + originalFileName);
		
		File originalFilePath = new File(originalFileName);
		directoryToExecuteFrom = originalFilePath.getParentFile().getAbsolutePath();
		outputFolder = directoryToExecuteFrom;
		
		File renamedfilePath = new File(directoryToExecuteFrom + "/init.py");
		Files.copy(originalFilePath.toPath(), renamedfilePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
		
		// Zip folder
		Zipper zipper = new Zipper(directoryToExecuteFrom + "/input.zip");
		Path filePath = zipper.getZipFromDirectory(new File(directoryToExecuteFrom));
		

		if(Utilities.isWindows())
		{
			//commands = new String[] { getSimulatorPath() + "rxvt.exe -e " + getSimulatorPath() + "sh " + getSimulatorPath().replace("/bin/", "/lib/") + "mknrndll.sh",
				//	getSimulatorPath() + "mkdir.exe results", getSimulatorPath() + "nrniv.exe -python " + filePath.getAbsolutePath() };
		}
		else
		{
			commands = new String[] { getSimulatorPath() + " " + this.neuronNSGExternalSimulatorConfig.getUsername() + " " + this.neuronNSGExternalSimulatorConfig.getPassword() + " " + this.neuronNSGExternalSimulatorConfig.getSimulatorParameters().get("appId") + " " + this.neuronNSGExternalSimulatorConfig.getUrl() + " "  + filePath.toString() };
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
		return this.neuronNSGExternalSimulatorConfig.getSimulatorPath();
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
	 * @param neuronNSGExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setNeuronNSGExternalSimulatorConfig(RemoteSimulatorConfig neuronNSGExternalSimulatorConfig)
	{
		this.neuronNSGExternalSimulatorConfig = neuronNSGExternalSimulatorConfig;
	}

}
