/*
 * Jess Balint
 *
 * Copyright (C) 2020-2020 by Jess Balint and the contributors
 *
 * Complete list of developers available at our web site:
 *
 * https://github.com/jbalint/rapidminer-stardog
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 */

package jbalint.rapidminer.stardog.stardog;

import java.util.List;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.stardog.stark.Values;
import com.stardog.stark.util.GraphBuilder;
import com.stardog.stark.util.ResourceBuilder;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSetWriter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * TODO :
 */
public class StardogExampleSetWriter extends AbstractExampleSetWriter {

	public static final String PARAMETER_URL = "url";

	public static final String PARAMETER_USERNAME = "username";

	public static final String PARAMETER_PASSWORD = "password";

	public static final String PARAMETER_GRAPH_NAME = "graph name";

	// TODO : something
	public static final String TEMP_NS = "http://example.com/";

	public StardogExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeString(
				PARAMETER_URL,
				"Specify the URL of the Stardog database.",
				"http://localhost:5820/myDb",
				false));

		types.add(new ParameterTypeString(
				PARAMETER_USERNAME,
				"Specify the username to use to authenticate to the Stardog database.",
				"admin",
				false));

		types.add(new ParameterTypeString(
				PARAMETER_PASSWORD,
				"Specify the password to use to authenticate to the Stardog database.",
				"admin",
				false));

		types.add(new ParameterTypeString(
				PARAMETER_GRAPH_NAME,
				"Specify the named graph in which to write the example set.",
				"http://example.com/rapidminer",
				false));

		return types;
	}

	/**
	 * TODO :
	 */
	private static ResourceBuilder newExampleResource(GraphBuilder graphBuilder, Attribute labelAttr, Example example) {
		ResourceBuilder statements;
		if (labelAttr == null) {
			statements = graphBuilder.bnode();
		}
		else {
			String label;
			if (labelAttr.isNominal()) {
				// TODO : probably unnecessary
				label = example.getNominalValue(labelAttr);
			}
			else {
				label = example.getValueAsString(labelAttr);
			}
			// TODO : do i need percent encoding here?
			statements = graphBuilder.iri(Values.iri(TEMP_NS, label));
		}
		return statements;
	}

	/**
	 * TODO : something
	 */
	private static void addExampleToGraph(Example example, ResourceBuilder statements, Attributes attrs) {
		for (Attribute attr : attrs) {
			if (attr.isNominal()) {
				statements.addProperty(Values.iri(TEMP_NS, attr.getName()),
				                       Values.literal(example.getValueAsString(attr)));
			}
			else if (attr.isDateTime()) {
				statements.addProperty(Values.iri(TEMP_NS, attr.getName()),
				                       Values.literal(example.getDateValue(attr)));
			}
			else {
				statements.addProperty(Values.iri(TEMP_NS, attr.getName()),
				                       Values.literal(example.getValue(attr)));
			}
		}
	}

	@Override
	public ExampleSet write(ExampleSet ioobject) throws OperatorException {
		try (Connection conn = ConnectionConfiguration.from(getParameter(PARAMETER_URL))
		                                              .credentials(getParameter(PARAMETER_USERNAME),
		                                                           getParameter(PARAMETER_PASSWORD))
		                                              .connect()) {
			conn.begin();
			GraphBuilder graphBuilder = new GraphBuilder();
			Attributes attrs = ioobject.getAttributes();
			Attribute labelAttr = attrs.getLabel();
			for (Example example : ioobject) {
				ResourceBuilder resourceBuilder = newExampleResource(graphBuilder, labelAttr, example);
				addExampleToGraph(example, resourceBuilder, attrs);
				conn.add().graph(graphBuilder.graph(), Values.iri(getParameter(PARAMETER_GRAPH_NAME)));
			}
			conn.commit();
		}
		return ioobject;
	}
}
