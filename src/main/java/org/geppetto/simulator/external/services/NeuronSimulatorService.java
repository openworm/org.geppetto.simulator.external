package org.geppetto.simulator.external.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.geppetto.core.services.IModelFormat;
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

	private static Log _logger = LogFactory.getLog(NeuronSimulatorService.class);

	@Autowired
	private SimulatorConfig neuronSimulatorConfig;

	@Autowired
	private ExternalSimulatorConfig neuronExternalSimulatorConfig;

	private List<String> _variableNames;

	private ConvertDATToRecording _datConverter;

	private boolean _processDone = false;
	private boolean _updateInProgress = false;

	@Override
	public void initialize(List<IModel> models, ISimulatorCallbackListener listener) throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(models, listener);

		this.addFeature(new AVariableWatchFeature());
		/**
		 * Creates command from model wrapper's neuron script
		 */
		for(IModel m : models)
		{
			ModelWrapper wrapper = (ModelWrapper) m;
			this.processCommand(wrapper.getModel(ModelFormat.NEURON).toString());
		}
	}

	@Override
	public void simulate(IAspectConfiguration aspectConfiguration, AspectNode aspect) throws GeppettoExecutionException
	{
		if(_updateInProgress)
		{
			this.getListener().endOfSteps(null,this._datConverter.getRecordingsFile());
			this._updateInProgress = false;
		}
		if(_processDone)
		{
			this.updateWatchTree(aspect);
			this._updateInProgress = true;
		}
		notifyStateTreeUpdated();
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
	public void processCommand(String originalFileName)
	{
		_logger.info("Creating command to run " + originalFileName);

		try
		{
			File filePath = new File(originalFileName);
			String[] commands = null;

			String directoryToExecuteFrom = filePath.getCanonicalPath();

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

				_logger.info("File with extension " + extension + " detected");

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

				_logger.info("Command to Execute: " + commands + " ...");
				_logger.info("From directory : " + directoryToExecuteFrom);
			}

			// send command, directory where execution is happening, and path
			// to original file script to exceture
			this.runExternalProcess(commands, directoryToExecuteFrom, originalFileName);
		}
		catch(IOException e)
		{

		}
	}

	@Override
	public void registerGeppettoService()
	{
		List<IModelFormat> modelFormatList = new ArrayList<IModelFormat>();
		modelFormatList.add(ModelFormat.NEURON);
		ServicesRegistry.registerSimulatorService(this, modelFormatList);
	}

	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException
	{
		super.processDone(processCommand);
		ExternalProcess process = this.getExternalProccesses().get(processCommand);

		List<String> variableNames = new ArrayList<String>();
		try
		{
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			ConvertDATToRecording datConverter = new ConvertDATToRecording("results-" + timeStamp + ".h5");

			File mappingResultsFile = new File(process.getExecutionDirectoryPath() + "/outputMapping.dat");
			BufferedReader input = new BufferedReader(new FileReader(mappingResultsFile));
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
			this._datConverter = datConverter;
			this._variableNames = variableNames;
			this._processDone = true;
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException(e);
		}

	}

	private void updateWatchTree(AspectNode aspect) throws GeppettoExecutionException
	{
		AspectSubTreeNode watchTree = (AspectSubTreeNode) aspect.getSubTree(AspectTreeType.SIMULATION_TREE);
		watchTree.setModified(true);
		aspect.setModified(true);
		aspect.getParentEntity().setModified(true);

		this.readRecording(_datConverter.getRecordingsFile(), this._variableNames, watchTree, true);
		_logger.info("Finished populating Simulation Tree " + watchTree.getInstancePath() + "with recordings");
		this._processDone = false;
	}
}
