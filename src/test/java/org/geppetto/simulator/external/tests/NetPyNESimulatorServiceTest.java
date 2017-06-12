/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011, 2013 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.simulator.external.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.data.model.local.LocalAspectConfiguration;
import org.geppetto.core.data.model.local.LocalExperiment;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.ExternalSimulatorConfig;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.simulator.external.services.NetPyNESimulatorService;
import org.geppetto.simulator.external.services.Utilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NetPyNESimulatorServiceTest implements ISimulatorCallbackListener
{

	private static String dirToExecute;
	private static String fileToExecute;
	private static NetPyNESimulatorService simulator;
	private static String resultsDir;
	private static boolean done = false;

	@BeforeClass
	public static void setup()
	{
		File dir = new File(NetPyNESimulatorServiceTest.class.getResource("/netpyneConvertedModel/").getFile());
		dirToExecute = dir.getAbsolutePath();
		fileToExecute = "/main_script.py";

		simulator = new NetPyNESimulatorService();
		simulator.registerGeppettoService();

		String neuron_home = System.getenv("NEURON_HOME");
		if (!neuron_home.endsWith("/bin/"))
			neuron_home +="/bin/";
		ExternalSimulatorConfig externalConfig = new ExternalSimulatorConfig();
		externalConfig.setSimulatorPath(neuron_home);
		Assert.assertNotNull(externalConfig.getSimulatorPath());
		simulator.setNetPyNEExternalSimulatorConfig(externalConfig);
		SimulatorConfig simulatorConfig = new SimulatorConfig();
		simulatorConfig.setSimulatorID("netpyneSimulator");
		simulatorConfig.setSimulatorName("netpyneSimulator");
		simulator.setNetPyNESimulatorConfig(simulatorConfig);
	}

	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NetPyNESimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNetPyNEExecution() throws Exception
	{
		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
		model.setFormat(ServicesRegistry.getModelFormat("NETPYNE"));
		model.setDomainModel(dirToExecute + fileToExecute);
		simulator.initialize(model, new LocalAspectConfiguration(1, "testModel", null, null, null), null, this, null);
		simulator.setProjectId(1);
		simulator.setExperiment(new LocalExperiment(1, null, null, null, null, null, null, null, null, null, null));
		simulator.simulate();
		Thread.sleep(6000);
		Assert.assertTrue(done);
	}

	@Override
	public void endOfSteps(IAspectConfiguration aspectConfiguration, Map<File, ResultsFormat> results)
	{

		resultsDir = dirToExecute + "results/";
		BufferedReader input = null;
		// will store values and variables found in DAT
		HashMap<String, List<Float>> dataValues = new HashMap<String, List<Float>>();
		try
		{
			// read DAT into a buffered reader
			File dir = new File(NetPyNESimulatorServiceTest.class.getResource("/netpyneConvertedModel/results/ex5_vars.dat").getFile());
            
			input = new BufferedReader(new FileReader(dir));

			// read rest of DAT file and extract values
			String line = input.readLine();
			String[] columns = line.split("\\s+");

			Assert.assertEquals(Float.valueOf(columns[0]), 0.0f);
			//Assert.assertEquals(Float.valueOf(columns[1]), 0.052932f);

			input.close();

			// read DAT into a buffered reader
			dir = new File(NetPyNESimulatorServiceTest.class.getResource("/netpyneConvertedModel/results/ex5_v.dat").getFile());
			input = new BufferedReader(new FileReader(dir));

			// read rest of DAT file and extract values
			line = input.readLine();
			columns = line.split("\\s+");

			Assert.assertEquals(Float.valueOf(columns[0]), 0.0f);
			Assert.assertEquals(Float.valueOf(columns[1]), -0.065000f);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		// handles End of file exception
		finally
		{
			try
			{
				input.close();
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}

		Assert.assertEquals(2, results.size());
		done = true;
	}

	@AfterClass
	public static void doYourOneTimeTeardown() throws IOException
	{
	}

	@Override
	public void externalProcessFailed(String message, Exception e) {
		// TODO Auto-generated method stub
		
	}

}
