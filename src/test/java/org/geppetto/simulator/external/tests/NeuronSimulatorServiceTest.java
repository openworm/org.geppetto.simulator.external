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
import java.util.UUID;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.model.values.Pointer;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.geppetto.simulator.external.services.Utilities;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * IT FIXME: There is no simulate method in this test, am I missing something? Not bothering fixing it until proven useful.
 * Btw this is the test that is commented out in the POM because the injecting of the spring config is not working still.
 * Test for the Neuron Simulator Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/spring/app-config.xml" })
public class NeuronSimulatorServiceTest implements ISimulatorCallbackListener
{

	@Resource
	NeuronSimulatorService simulator = new NeuronSimulatorService();

	private static String dirToExecute;
	private static String fileToExecute;

	@BeforeClass
	public static void setup()
	{
		dirToExecute = "./src/test/resources/neuronConvertedModel/";
		fileToExecute = "main_script.py";
		
		NeuronSimulatorService neuronSimulatorService = new NeuronSimulatorService();
		neuronSimulatorService.registerGeppettoService();
	}

	@AfterClass
	public static void tearDown()
	{
		File f = new File("/jhdf5.dll");
	}

	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronExecution() throws Exception
	{
		if(simulator.getSimulatorPath() != null && !simulator.getSimulatorPath().equals(""))
		{

			ModelWrapper model = new ModelWrapper(UUID.randomUUID().toString());
			model.wrapModel(ServicesRegistry.getModelFormat("NEURON"), dirToExecute + fileToExecute);
			simulator.initialize(model,null,null, this);

		}
	}

	@Override
	public void endOfSteps(Pointer pointer, Map<File,ResultsFormat> results)
	{

		String resultsDir = dirToExecute + "results/";
		BufferedReader input = null;
		// will store values and variables found in DAT
		HashMap<String, List<Float>> dataValues = new HashMap<String, List<Float>>();
		try
		{
			// read DAT into a buffered reader
			input = new BufferedReader(new FileReader(resultsDir + "ex5_vars.dat"));

			// read rest of DAT file and extract values
			String line = input.readLine();
			String[] columns = line.split("\\s+");

			Assert.assertEquals(Float.valueOf(columns[0]), 0.0f);
			Assert.assertEquals(Float.valueOf(columns[1]), 0.052932f);
			Assert.assertEquals(Float.valueOf(columns[2]), 0.596121f);
			Assert.assertEquals(Float.valueOf(columns[3]), 0.317677f);

			input.close();

			// read DAT into a buffered reader
			input = new BufferedReader(new FileReader(resultsDir + "ex5_v.dat"));

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

		Assert.assertEquals("Process for " + dirToExecute + fileToExecute + " is done executing", pointer.getInstancePath());

		// Delete files
		try
		{
			Utilities.delete(new File(resultsDir));
			Utilities.delete(new File(dirToExecute + "x86_64/"));
			new File(dirToExecute + "time.dat").delete();
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void stepped(Pointer pointer) throws GeppettoExecutionException
	{
	
	}
}
