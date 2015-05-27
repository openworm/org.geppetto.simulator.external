package org.geppetto.simulator.external.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.externalprocesses.ExternalProcess;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.model.runtime.AspectNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode;
import org.geppetto.core.model.runtime.AspectSubTreeNode.AspectTreeType;
import org.geppetto.core.services.ModelFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.AExternalProcessSimulator;
import org.geppetto.core.simulator.AVariableWatchFeature;
import org.geppetto.simulator.external.converters.ConvertDATToRecording;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Neuron Simulator
 * 
 * @author Jesus R Martinez (jesus@metacell.us)
 *
 */
@Service
public class NeuronSimulatorService extends AExternalProcessSimulator
{

	protected File filePath = null;

	protected String[] commands = null;

	protected String directoryToExecuteFrom = null;

	private String originalFileName;

	private static Log logger = LogFactory.getLog(NeuronSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	@Autowired
	private ExternalSimulatorConfig neuronExternalSimulatorConfig;

	private List<String> variableNames;

	private ConvertDATToRecording datConverter;

	private boolean started = false;

	@Override
	public void initialize(List<IModel> models, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(models, listener);

		this.addFeature(new AVariableWatchFeature());

		/**
		 * Creates command from model wrapper's neuron script
		 */
		if(models.size() > 1)
		{
			throw new GeppettoInitializationException("More than one model in the NEURON simulator is currently not supported");
		}

		ModelWrapper wrapper = (ModelWrapper) models.get(0);
		originalFileName = wrapper.getModel(ServicesRegistry.registerModelFormat("NEURON")).toString();
		this.createCommands(originalFileName);
	}

	@Override
	public void simulate(IAspectConfiguration aspectConfiguration, AspectNode aspect) throws GeppettoExecutionException
	{
		// send command, directory where execution is happening, and path to original file script to execute
		if(!started)
		{
			this.runExternalProcess(commands, directoryToExecuteFrom, originalFileName);
			this.instancePath = aspect.getInstancePath();
			started = true;
		}
		else
		{
			throw new GeppettoExecutionException("Simulate has been called again");
		}
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
	 * Creates command to be executed by an external process
	 * 
	 * @param originalFileName
	 * @param aspect
	 */
	public void createCommands(String originalFileName)
	{
		filePath = new File(originalFileName);

		logger.info("Creating command to run " + originalFileName);

		if(filePath.isDirectory())
		{
			if(Utilities.isWindows())
			{
				commands = new String[] { getSimulatorPath() + "rxvt.exe -e " + getSimulatorPath() + "sh " + getSimulatorPath().replace("/bin/", "/lib/") + "mknrndll.sh" };
			}
			else
			{
				commands = new String[] { getSimulatorPath() + "nrnivmodl" };
			}

		}
		else
		{
			String extension = Utilities.extension(filePath);

			logger.info("File with extension " + extension + " detected");

			directoryToExecuteFrom = filePath.getParentFile().getAbsolutePath();
			if(extension.equals("hoc"))
			{
				commands = new String[] { getSimulatorPath() + "nrngui " + filePath.getAbsolutePath() };
			}
			else if(extension.equals("py"))
			{
				if(Utilities.isWindows())
				{
					commands = new String[] { getSimulatorPath() + "rxvt.exe -e " + getSimulatorPath() + "sh " + getSimulatorPath().replace("/bin/", "/lib/") + "mknrndll.sh",
							getSimulatorPath() + "mkdir.exe results", getSimulatorPath() + "nrniv.exe -python " + filePath.getAbsolutePath() };
				}
				else
				{
					commands = new String[] { getSimulatorPath() + "nrnivmodl", "mkdir results", getSimulatorPath() + "nrniv -python " + filePath.getAbsolutePath() };
				}
			}

			logger.info("Command to Execute: " + commands + " ...");
			logger.info("From directory : " + directoryToExecuteFrom);
		}

	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NEURON")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException
	{
		super.processDone(processCommand);
		try
		{
			ExternalProcess process = this.getExternalProccesses().get(processCommand);

			List<String> variableNames = new ArrayList<String>();

			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			ConvertDATToRecording datConverter = new ConvertDATToRecording("results-" + timeStamp + ".h5");

			File mappingResultsFile = new File(process.getExecutionDirectoryPath() + "/outputMapping.dat");
			BufferedReader input;

			input = new BufferedReader(new FileReader(mappingResultsFile));

			// read rest of DAT file and extract values
			String filePath = "";
			String line = "";
			while((line = input.readLine()) != null)
			{
				if(filePath.equals(""))
				{
					filePath = line;
				}
				else
				{
					String[] variables = line.split("\\s+");
					for(String s : variables)
					{
						variableNames.add(s);
					}
					datConverter.addDATFile(mappingResultsFile.getParent() + "/" + filePath, variables);

					filePath = "";
				}
			}
			input.close();
			datConverter.convert();

			this.datConverter = datConverter;
			this.variableNames = variableNames;

			this.getListener().endOfSteps(instancePath, this.datConverter.getRecordingsFile());
			// TODO The code below was commented out, we need to put things in the runtime tree only if the
			// user asks for it, not by default
			// this.updateWatchTree(aspect);
		}
		catch(Exception e)
		{
			//The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
	}

	private void updateWatchTree(AspectNode aspect) throws GeppettoExecutionException
	{
		AspectSubTreeNode watchTree = (AspectSubTreeNode) aspect.getSubTree(AspectTreeType.SIMULATION_TREE);
		watchTree.setModified(true);
		aspect.setModified(true);
		aspect.getParentEntity().setModified(true);

		this.readRecording(datConverter.getRecordingsFile(), this.variableNames, watchTree, true);
		logger.info("Finished populating Simulation Tree " + watchTree.getInstancePath() + "with recordings");
		// this.processDone = false;
	}
}
