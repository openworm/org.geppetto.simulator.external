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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.model.IModel;
import org.geppetto.core.model.ModelWrapper;
import org.geppetto.core.simulation.ISimulatorCallbackListener;
import org.geppetto.simulator.external.services.NeuronSimulatorService;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the Neuron Simulator Service
 */
public class NeuronSimulatorServiceTest implements ISimulatorCallbackListener
{

	private static String fileToExecute;

	@BeforeClass
	public static void setup() throws Exception
	{
		fileToExecute = "/neuron_script.py";
	}
	
	/**
	 * Test method for {@link org.geppetto.simulator.jlems.JLEMSSimulatorService#getWatchableVariables()}.
	 * @throws Exception 
	 */
	@Test
	public void testGetWatchableVariables() throws Exception
	{
		setup();
		NeuronSimulatorService simulator = new NeuronSimulatorService();
		List<IModel> models = new ArrayList<IModel>();
		ModelWrapper m = new ModelWrapper(UUID.randomUUID().toString());
		m.wrapModel("Neuron", fileToExecute);
		models.add(m);
		simulator.initialize(models, this);
	}

	@Override
	public void endOfSteps(String message) {
		Assert.assertEquals("Process for " + 
				fileToExecute+ " is done executing", message);
	}

	@Override
	public void stateTreeUpdated() throws GeppettoExecutionException {
		
	}
}
