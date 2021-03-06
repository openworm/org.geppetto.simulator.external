package org.geppetto.simulator.external.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geppetto.core.beans.PathConfiguration;
import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.conversion.AConversion;
import org.geppetto.core.conversion.ConversionException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.externalprocesses.ExternalProcess;
import org.geppetto.core.manager.Scope;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.recordings.ConvertDATToRecording;
import org.geppetto.core.services.ServiceCreator;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ExternalSimulatorConfig;
import org.geppetto.model.DomainModel;
import org.geppetto.model.ExperimentState;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.ModelFormat;
import org.lemsml.jlems.core.sim.ContentError;
import org.lemsml.jlems.core.type.Component;
import org.lemsml.jlems.core.type.Lems;
import org.lemsml.jlems.io.xmlio.XMLSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Wrapper class for LEMS
 * 
 * @author mcantarelli
 *
 */
@Service
public class LEMSSimulatorService extends AExternalProcessNeuronalSimulator
{

	protected File filePath = null;

	private static Log logger = LogFactory.getLog(LEMSSimulatorService.class);

	private ModelFormat lemsFormat = ServicesRegistry.getModelFormat("LEMS");

	private Lems lems;

	@Autowired
	private SimulatorConfig lemsSimulatorConfig;

	@Autowired
	private ExternalSimulatorConfig lemsExternalSimulatorConfig;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess) throws GeppettoInitializationException,
			GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);
		lems = (Lems) model.getDomainModel();

	}

	@Override
	public void simulate() throws GeppettoExecutionException
	{
		try
		{
			AConversion conversion = (AConversion) ServiceCreator.getNewServiceInstance("lemsConversion");
			conversion.setScope(Scope.RUN);
			conversion.setConvertModel(false);
			DomainModel model = conversion.convert(this.model, lemsFormat, aspectConfiguration, this.geppettoModelAccess);
			if(model instanceof ExternalDomainModel)
			{
				outputFolder = (String) model.getDomainModel();	
			}
			else
			{
				throw new GeppettoExecutionException("Unexpected domain model inside LEMS Simulator service");
			}
			String serialisedModel = XMLSerializer.serialize(lems);
			originalFileName = outputFolder + "lems.xml";
			PrintWriter printWriter = new PrintWriter(originalFileName);
			printWriter.print(serialisedModel);
			printWriter.close();
		}
		catch(ConversionException e)
		{
			throw new GeppettoExecutionException(e);
		}
		catch(GeppettoInitializationException e)
		{
			throw new GeppettoExecutionException(e);
		}
		catch(FileNotFoundException e)
		{
			throw new GeppettoExecutionException(e);
		}
		catch(ContentError e)
		{
			throw new GeppettoExecutionException(e);
		}
		this.createCommands(originalFileName);
		super.simulate();

	}
    

	@Override
	public void processDone(String[] processCommand) throws GeppettoExecutionException
	{
		try
		{
			ExternalProcess process = this.getExternalProccesses().get(processCommand);

			List<String> variableNames = new ArrayList<String>();

			ConvertDATToRecording datConverter = new ConvertDATToRecording(PathConfiguration.createExperimentTmpPath(Scope.RUN, projectId, getExperiment().getId(), aspectConfiguration.getInstance(), PathConfiguration.getName("results", true)+ ".h5"),this.geppettoModelAccess);

			Map<File, ResultsFormat> results = new HashMap<File, ResultsFormat>();

			File mappingResultsFile = new File(process.getOutputFolder() + "/outputMapping.dat");
			results.put(mappingResultsFile, ResultsFormat.RAW);

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
					String fileName = getSimulatorPath() + filePath;
					datConverter.addDATFile(fileName, variables);
					results.put(new File(fileName), ResultsFormat.RAW);
					filePath = "";
				}
			}
			input.close();
			datConverter.convert(experimentState);

			results.put(datConverter.getRecordingsFile(), ResultsFormat.GEPPETTO_RECORDING);

			this.getListener().endOfSteps(this.aspectConfiguration, results);
		}
		catch(Exception e)
		{
			// The HDF5 library throws a generic Exception :/
			throw new GeppettoExecutionException(e);
		}
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
		File outputPath = new File(outputFolder);
		outputFolder = outputPath.getAbsolutePath();
		directoryToExecuteFrom = getSimulatorPath();

		if(Utilities.isWindows())
		{
			commands = new String[] { "mkdir results", File.separator + "jnml.bat " + filePath.getAbsolutePath() };
		}
		else
		{
			commands = new String[] { "mkdir results", directoryToExecuteFrom + "jnml " + filePath.getAbsolutePath(), "mkdir " + outputFolder + "/results" };
		}

		logger.info("Command to Execute: " + commands + " ...");

	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("LEMS")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public String getName()
	{
		return this.lemsSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.lemsSimulatorConfig.getSimulatorID();
	}

	@Override
	public String getSimulatorPath()
	{
		return this.lemsExternalSimulatorConfig.getSimulatorPath();
	}
    
    
	/**
	 * @param lemsSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setLEMSSimulatorConfig(SimulatorConfig lemsSimulatorConfig)
	{
		this.lemsSimulatorConfig = lemsSimulatorConfig;
	}

	/**
	 * @param lemsExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
	public void setLEMSExternalSimulatorConfig(ExternalSimulatorConfig lemsExternalSimulatorConfig)
	{
		this.lemsExternalSimulatorConfig = lemsExternalSimulatorConfig;
	}

}
