/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2015 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.bind.path;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.Response;
import com.threewks.thundr.route.HttpMethod;
import com.threewks.thundr.route.Route;
import com.threewks.thundr.route.controller.TestAnnotation;
import com.threewks.thundr.transformer.TransformerManager;

public class PathVariableBinderTest {

	private PathVariableBinder pathVariableBinder;
	private Map<ParameterDescription, Object> parameterDescriptions;
	private HashMap<String, String> pathVariables;
	private Request request;
	private Response response;
	private Route route;

	@Before
	public void before() {
		pathVariableBinder = new PathVariableBinder(TransformerManager.createWithDefaults());
		parameterDescriptions = new LinkedHashMap<ParameterDescription, Object>();
		pathVariables = new HashMap<String, String>();
		request = mock(Request.class);
		response = mock(Response.class);
		route = mock(Route.class);
		when(route.getPathVars(anyString())).thenReturn(pathVariables);
		when(request.getRoute()).thenReturn(route);
	}

	@Test
	public void shouldHandleCoreTypeParamBindings() {
		ParameterDescription param1 = new ParameterDescription("param1", String.class);
		ParameterDescription param2 = new ParameterDescription("param2", int.class);
		ParameterDescription param3 = new ParameterDescription("param3", Integer.class);
		ParameterDescription param4 = new ParameterDescription("param4", double.class);
		ParameterDescription param5 = new ParameterDescription("param5", Double.class);
		ParameterDescription param6 = new ParameterDescription("param6", short.class);
		ParameterDescription param7 = new ParameterDescription("param7", Short.class);
		ParameterDescription param8 = new ParameterDescription("param8", float.class);
		ParameterDescription param9 = new ParameterDescription("param9", Float.class);
		ParameterDescription param10 = new ParameterDescription("param10", long.class);
		ParameterDescription param11 = new ParameterDescription("param11", Long.class);
		ParameterDescription param12 = new ParameterDescription("param12", BigDecimal.class);
		ParameterDescription param13 = new ParameterDescription("param13", BigInteger.class);
		ParameterDescription param14 = new ParameterDescription("param14", UUID.class);
		ParameterDescription param15 = new ParameterDescription("param15", DateTime.class);
		ParameterDescription param16 = new ParameterDescription("param16", HttpMethod.class); // arbitrary enum, not specifically the route type

		parameterDescriptions.put(param1, null);
		parameterDescriptions.put(param2, null);
		parameterDescriptions.put(param3, null);
		parameterDescriptions.put(param4, null);
		parameterDescriptions.put(param5, null);
		parameterDescriptions.put(param6, null);
		parameterDescriptions.put(param7, null);
		parameterDescriptions.put(param8, null);
		parameterDescriptions.put(param9, null);
		parameterDescriptions.put(param10, null);
		parameterDescriptions.put(param11, null);
		parameterDescriptions.put(param12, null);
		parameterDescriptions.put(param13, null);
		parameterDescriptions.put(param14, null);
		parameterDescriptions.put(param15, null);
		parameterDescriptions.put(param16, null);

		String uuidString = UUID.randomUUID().toString();
		DateTime dateTime = new DateTime();
		pathVariables.put("param1", "string-value");
		pathVariables.put("param2", "2");
		pathVariables.put("param3", "3");
		pathVariables.put("param4", "4.0");
		pathVariables.put("param5", "5.0");
		pathVariables.put("param6", "6");
		pathVariables.put("param7", "7");
		pathVariables.put("param8", "8.8");
		pathVariables.put("param9", "9.9");
		pathVariables.put("param10", "10");
		pathVariables.put("param11", "11");
		pathVariables.put("param12", "12.00");
		pathVariables.put("param13", "13");
		pathVariables.put("param14", uuidString);
		pathVariables.put("param15", dateTime.toString());
		pathVariables.put("param16", "POST");

		pathVariableBinder.bindAll(parameterDescriptions, request, response);

		assertThat(parameterDescriptions.get(param1), is((Object) "string-value"));
		assertThat(parameterDescriptions.get(param2), is((Object) 2));
		assertThat(parameterDescriptions.get(param3), is((Object) 3));
		assertThat(parameterDescriptions.get(param4), is((Object) 4.0));
		assertThat(parameterDescriptions.get(param5), is((Object) 5.0));
		assertThat(parameterDescriptions.get(param6), is((Object) (short) 6));
		assertThat(parameterDescriptions.get(param7), is((Object) (short) 7));
		assertThat(parameterDescriptions.get(param8), is((Object) 8.8f));
		assertThat(parameterDescriptions.get(param9), is((Object) 9.9f));
		assertThat(parameterDescriptions.get(param10), is((Object) 10L));
		assertThat(parameterDescriptions.get(param11), is((Object) 11L));
		assertThat(parameterDescriptions.get(param12), is((Object) new BigDecimal("12.00")));
		assertThat(parameterDescriptions.get(param13), is((Object) BigInteger.valueOf(13)));
		assertThat(parameterDescriptions.get(param14), is((Object) UUID.fromString(uuidString)));
		// Timezones will differ as parsing maintains the timezone exactly (i.e. Australia/Sydney becomes +10:00)
		assertThat(((DateTime) parameterDescriptions.get(param15)).compareTo(dateTime), is(0));
		assertThat(parameterDescriptions.get(param16), is((Object) HttpMethod.POST));
	}

	@Test
	public void shouldLeaveUnbindableValuesNull() {
		ParameterDescription param1 = new ParameterDescription("param1", String.class);
		ParameterDescription param2 = new ParameterDescription("param2", Color.class);
		ParameterDescription param3 = new ParameterDescription("param3", Object.class);
		ParameterDescription param4 = new ParameterDescription("param4", TestAnnotation.class);

		parameterDescriptions.put(param1, null);
		parameterDescriptions.put(param2, null);
		parameterDescriptions.put(param3, null);
		parameterDescriptions.put(param4, null);

		pathVariables.put("param1", "string-value");
		pathVariables.put("param2", Color.BLACK.toString());
		pathVariables.put("param3", "3");
		pathVariables.put("param4", "something");

		pathVariableBinder.bindAll(parameterDescriptions, request, response);

		assertThat(parameterDescriptions.get(param1), is((Object) "string-value"));
		assertThat(parameterDescriptions.get(param2), is(nullValue()));
		assertThat(parameterDescriptions.get(param3), is((Object) "3"));
		assertThat(parameterDescriptions.get(param4), is(nullValue()));
	}
}
