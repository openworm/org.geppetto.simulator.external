/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
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
package org.geppetto.simulator.external.converters;

import java.util.HashMap;
import java.util.List;

import org.geppetto.core.model.runtime.CompositeNode;
import org.geppetto.core.model.runtime.VariableNode;
import org.geppetto.core.model.state.visitors.RuntimeTreeVisitor;
import org.geppetto.core.recordings.GeppettoRecordingCreator;
import org.geppetto.core.recordings.GeppettoRecordingCreator.MetaType;

/**
 * @author Adrian Quintana (adrian.perez@ucl.ac.uk)
 * 
 *         This visitor adds the variable watched during the simulation to the Geppetto Recording object
 */
public class GeppettoRecordingVisitor extends RuntimeTreeVisitor
{

	private HashMap<String, List<Float>> dataValues;
	private GeppettoRecordingCreator recordingCreator;
	
	public GeppettoRecordingVisitor()
	{
		super();
	}

	public GeppettoRecordingVisitor(HashMap<String, List<Float>> dataValues, GeppettoRecordingCreator recordingCreator)
	{
		super();
		this.dataValues = dataValues;
		this.recordingCreator = recordingCreator;

		// Add time to recording value, won't be doing in visitVariableNode method
		// below since time isn't a variable node inside simulation tree
		List<Float> floatValues = this.dataValues.get("time");
		float[] target = new float[floatValues.size()];
		for(int i = 0; i < target.length; i++)
		{
			target[i] = floatValues.get(i);
		}

		recordingCreator.addValues("time", target, "s", MetaType.Variable_Node, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.state.visitors.DefaultStateVisitor#inAspectNode (org.geppetto.core.model.runtime.AspectNode)
	 */
	@Override
	public boolean inCompositeNode(CompositeNode node)
	{
		// we only visit the nodes which belong to the same aspect
		return super.inCompositeNode(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.state.visitors.DefaultStateVisitor#visitVariableNode (org.geppetto.core.model.runtime.VariableNode)
	 */
	@Override
	public boolean visitVariableNode(VariableNode node)
	{
		if(this.dataValues.containsKey(node.getLocalInstancePath()))
		{
			List<Float> floatValues = this.dataValues.get(node.getLocalInstancePath());
			float[] target = new float[floatValues.size()];
			for(int i = 0; i < target.length; i++)
			{
				target[i] = floatValues.get(i);
			}

			recordingCreator.addValues(node.getLocalInstancePath(), target, node.getUnit().toString(), MetaType.Variable_Node, false);
		}

		return super.visitVariableNode(node);
	}

}
