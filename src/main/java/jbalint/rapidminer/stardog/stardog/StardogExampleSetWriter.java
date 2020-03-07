/*
 * Copyright 2020 Jess Balint
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jbalint.rapidminer.stardog.stardog;

import java.util.List;

import com.complexible.stardog.PercentEncoding;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.stardog.stark.IRI;
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
import com.rapidminer.parameter.UndefinedParameterError;

/**
 * Write an {@link ExampleSet} to a Stardog database
 */
public class StardogExampleSetWriter extends AbstractExampleSetWriter {

	/**
	 * URL parameter
	 */
	public static final String PARAMETER_URL = "url";


	/**
	 * Username parameter
	 */
	public static final String PARAMETER_USERNAME = "username";


	/**
	 * Password parameter
	 */
	public static final String PARAMETER_PASSWORD = "password";


	/**
	 * Graph name parameter
	 */
	public static final String PARAMETER_GRAPH_NAME = "graph name";

	/**
	 * // TODO : namespace used for all IRIs. allow setting this as a parameter
	 */
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
	 * Create a new resource (subject) for an observation in the example set. It will be added to the {@code graphBuilder} and the returned {@code
	 * ResourceBuilder} can be used to add statements to the graph. The value for {@code labelAttr} will be used to generate the subject resource, if
	 * present. Otherwise, a bnode will be created.
	 */
	private static ResourceBuilder newExampleResource(GraphBuilder graphBuilder, Attribute labelAttr, Example example) {
		ResourceBuilder statements;
		if (labelAttr == null) {
			statements = graphBuilder.bnode();
		}
		else {
			String label;
			if (labelAttr.isNominal()) {
				label = example.getNominalValue(labelAttr);
			}
			else {
				label = example.getValueAsString(labelAttr);
			}
			statements = graphBuilder.iri(Values.iri(TEMP_NS, PercentEncoding.encodeIri(label)));
		}
		return statements;
	}

	/**
	 * Add {@code example} to the {@code statements} graph
	 */
	private static void addExampleToGraph(Example example, ResourceBuilder statements, Attributes attrs) {
		for (Attribute attr : attrs) {
			IRI property = Values.iri(TEMP_NS, PercentEncoding.encodeIri(attr.getName()));
			if (attr.isNominal()) {
				statements.addProperty(property,
				                       Values.literal(example.getValueAsString(attr)));
			}
			else if (attr.isDateTime()) {
				statements.addProperty(property,
				                       Values.literal(example.getDateValue(attr)));
			}
			else {
				statements.addProperty(property,
				                       Values.literal(example.getValue(attr)));
			}
		}
	}

	private Connection connect() throws UndefinedParameterError {
		return ConnectionConfiguration.from(getParameter(PARAMETER_URL))
		                              .credentials(getParameter(PARAMETER_USERNAME),
		                                           getParameter(PARAMETER_PASSWORD))
		                              .connect();
	}

	@Override
	public ExampleSet write(ExampleSet ioobject) throws OperatorException {
		try (Connection conn = connect()) {
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
