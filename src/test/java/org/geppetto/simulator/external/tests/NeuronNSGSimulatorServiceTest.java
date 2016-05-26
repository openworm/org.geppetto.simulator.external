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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.geppetto.core.beans.SimulatorConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.data.model.ResultsFormat;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.core.simulator.RemoteSimulatorConfig;
import org.geppetto.model.ExternalDomainModel;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.simulator.remote.services.NSGUtilities;
import org.geppetto.simulator.remote.services.NeuronNSGSimulatorService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ngbw.directclient.CiCipresException;

public class NeuronNSGSimulatorServiceTest implements ISimulatorCallbackListener
{

	//AQP Review all these libs
	private static String dirToExecute;
	private static String fileToExecute;
	private static NeuronNSGSimulatorService simulator;
	private static String resultsDir;
	private static boolean done = false;

	@BeforeClass
	public static void setup()
	{
//		File dir = new File(NeuronNSGSimulatorServiceTest.class.getResource("/neuronConvertedModel/").getFile());
//		dirToExecute = dir.getAbsolutePath();
//		fileToExecute = "/main_script.py";
//
//		simulator = new NeuronNSGSimulatorService();
//		simulator.registerGeppettoService();
//
//		RemoteSimulatorConfig remoteSimulatorConfig = new RemoteSimulatorConfig();
//		remoteSimulatorConfig.setSimulatorPath("https://nsgr.sdsc.edu:8444/cipresrest/v1");
//		remoteSimulatorConfig.setUsername("AdrianQuintana");
//		remoteSimulatorConfig.setPassword("");
//		Map<String, String> simulatorParameters = new HashMap<String, String>();
//		simulatorParameters.put("appId", "AdrianQuintana-7A60A2BDED7145F09AE1FCBFD938FBC0");
//		remoteSimulatorConfig.setSimulatorParameters(simulatorParameters);
//		Assert.assertNotNull(remoteSimulatorConfig.getSimulatorPath());
//		simulator.setNeuronNSGExternalSimulatorConfig(remoteSimulatorConfig);
//		SimulatorConfig simulatorConfig = new SimulatorConfig();
//		simulatorConfig.setSimulatorID("neuronNSGSimulator");
//		simulatorConfig.setSimulatorName("neuronNSGSimulator");
//		simulator.setNeuronSimulatorConfig(simulatorConfig);
	}

	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronExecution() throws Exception
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		simulator.initialize(model, null, null, this, null);
//		simulator.simulate();
//		Thread.sleep(6000);
//		Assert.assertTrue(done);
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * @throws GeppettoExecutionException 
	 * @throws GeppettoInitializationException 
	 * @throws CiCipresException 
	 * @throws InterruptedException 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronList() throws GeppettoInitializationException, GeppettoExecutionException, CiCipresException, InterruptedException
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		
//		simulator.initialize(model, null, null, this, null);
//		NSGUtilities.listJobs(simulator.getCiClient());
//		Thread.sleep(6000);
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronDeleteJobs() throws Exception
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		simulator.initialize(model, null, null, this, null);
//		simulator.deleteAllJobs();
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.external.services.NeuronSimulatorService}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNeuronProcessDone() throws Exception
	{
//		ExternalDomainModel model = GeppettoFactory.eINSTANCE.createExternalDomainModel();
//		model.setFormat(ServicesRegistry.getModelFormat("NEURON"));
//		model.setDomainModel(dirToExecute + fileToExecute);
//		simulator.initialize(model, null, null, this, null);
//		simulator.processDone();
	}
	
	@Override
	public void endOfSteps(IAspectConfiguration aspectConfiguration, Map<File, ResultsFormat> results)
	{

		
	}

	@AfterClass
	public static void doYourOneTimeTeardown() throws IOException
	{
	}

}
