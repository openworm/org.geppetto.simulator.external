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
 * Wrapper class for NetPyNE
 * 
 * @author Padraig Gleeson
 * @author Jesus R Martinez (jesus@metacell.us)
 * @author mcantarelli
 *
 */
@Service
public class NetPyNESimulatorService extends AExternalProcessNeuronalSimulator
{

	protected File filePath = null;

    private static Log logger = LogFactory.getLog(NetPyNESimulatorService.class);

	@Autowired
	private SimulatorConfig netpyneSimulatorConfig;

	@Autowired
    private ExternalSimulatorConfig netpyneExternalSimulatorConfig;
    
    private int numberProcessors = 1;

	@Override
	public void initialize(DomainModel model, IAspectConfiguration aspectConfiguration, ExperimentState experimentState, ISimulatorCallbackListener listener, GeppettoModelAccess modelAccess)
			throws GeppettoInitializationException, GeppettoExecutionException
	{
		super.initialize(model, aspectConfiguration, experimentState, listener, modelAccess);

        if (aspectConfiguration.getSimulatorConfiguration()!=null)
        {
            if (aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors")!=null &&
                aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors").length()>0)
            {
                numberProcessors = Integer.parseInt(aspectConfiguration.getSimulatorConfiguration().getParameters().get("numberProcessors"));
            }
        }

		if(model instanceof ExternalDomainModel)
		{
			originalFileName = (String) model.getDomainModel();
		}
		else
		{
            throw new GeppettoExecutionException("Unexpected domain model inside NetPyNE Simulator service: \n"
                +model.getClass().getCanonicalName()+"\n"
            +model.getDomainModel()+"\n"+model.getFormat());
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
            //Might work...?
			commands = new String[] { getSimulatorPath() + "rxvt.exe -e " + getSimulatorPath() + "sh " + getSimulatorPath().replace("/bin/", "/lib/") + "mknrndll.sh",
					getSimulatorPath() + "mkdir.exe results", getSimulatorPath() + "nrniv.exe -python " + filePath.getAbsolutePath() };
		}
		else
		{
            if (numberProcessors==1)
            {
                commands = new String[] { getSimulatorPath() + "nrnivmodl", "mkdir results", getSimulatorPath() + "nrniv -python " + filePath.getAbsolutePath()+" -nogui"};
            }
            else
            {
                commands = new String[] { getSimulatorPath() + "nrnivmodl", "mkdir results", "mpiexec -np "+numberProcessors+" "+getSimulatorPath() + "nrniv -mpi " + filePath.getAbsolutePath()+"" };
            }
            
		}

		String info = "Commands to execute from directory: " + directoryToExecuteFrom+":\n";
		for (String c: commands)
		{
			info+="   >>  " + c+"\n";
		}
		logger.info(info);

	}

	@Override
	public void registerGeppettoService()
	{
        List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NetPyNE")));
		ServicesRegistry.registerSimulatorService(this, modelFormats);
	}

	@Override
	public String getName()
	{
		return this.netpyneSimulatorConfig.getSimulatorName();
	}

	@Override
	public String getId()
	{
		return this.netpyneSimulatorConfig.getSimulatorID();
	}

	@Override
	public String getSimulatorPath()
	{
        return this.netpyneExternalSimulatorConfig.getSimulatorPath();
	}

	/**
	 * @param netpyneSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
    public void setNetPyNESimulatorConfig(SimulatorConfig netpyneSimulatorConfig)
	{
		this.netpyneSimulatorConfig = netpyneSimulatorConfig;
	}

	/**
     * @param netpyneExternalSimulatorConfig
	 * @deprecated for test purposes only, the configuration is autowired
	 */
    public void setNetPyNEExternalSimulatorConfig(ExternalSimulatorConfig netpyneExternalSimulatorConfig)
	{
        this.netpyneExternalSimulatorConfig = netpyneExternalSimulatorConfig;
	}

}
