/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.action.method.bind.json;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.action.method.bind.BindException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;

public class GsonBinderTest {

	@Rule public ExpectedException thrown = ExpectedException.none();
	private GsonBinder gsonBinder = new GsonBinder();
	private MockHttpServletRequest req = new MockHttpServletRequest().contentType(ContentType.ApplicationJson);
	private HttpServletResponse resp = new MockHttpServletResponse();
	private Map<String, String> pathVariables = Collections.emptyMap();

	@Test
	public void shouldReturnTrueForCanBindToJsonContentType() {
		assertThat(gsonBinder.canBind(ContentType.ApplicationJson.value()), is(true));
		assertThat(gsonBinder.canBind("APPLICATION/JSON"), is(true));
		assertThat(gsonBinder.canBind(ContentType.TextPlain.value()), is(false));
		assertThat(gsonBinder.canBind(null), is(false));
	}

	@Test
	public void shouldBindJsonToPojoControllerParameter() {
		ParameterDescription parameterDescription = new ParameterDescription("pojo", TestPojo.class);
		Map<ParameterDescription, Object> bindings = map(parameterDescription, null);
		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(parameterDescription);
		assertThat(boundValue, is(notNullValue()));
		assertThat(boundValue instanceof TestPojo, is(true));
		TestPojo testPojo = (TestPojo) boundValue;
		assertThat(testPojo.name, is("pojo name"));
		assertThat(testPojo.value, is(5));
	}

	@Test
	public void shouldBindJsonToFirstPojoControllerParameterIgnoringOtherParameters() {
		ParameterDescription pojoParameterDescription = new ParameterDescription("pojo", TestPojo.class);
		ParameterDescription pojo2ParameterDescription = new ParameterDescription("pojo2", TestPojo.class);
		ParameterDescription requestParameterDescription = new ParameterDescription("req", HttpServletRequest.class);
		ParameterDescription intParameterDescription = new ParameterDescription("integer", int.class);
		ParameterDescription stringParameterDescription = new ParameterDescription("string", String.class);
		Map<ParameterDescription, Object> bindings = new LinkedHashMap<ParameterDescription, Object>();
		bindings.put(pojoParameterDescription, null);
		bindings.put(pojo2ParameterDescription, null);
		bindings.put(requestParameterDescription, null);
		bindings.put(intParameterDescription, null);
		bindings.put(stringParameterDescription, null);

		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(pojoParameterDescription);
		assertThat(boundValue, is(notNullValue()));
		assertThat(boundValue instanceof TestPojo, is(true));
		TestPojo testPojo = (TestPojo) boundValue;
		assertThat(testPojo.name, is("pojo name"));
		assertThat(testPojo.value, is(5));

		assertThat(bindings.get(pojo2ParameterDescription), is(nullValue()));
		assertThat(bindings.get(requestParameterDescription), is(nullValue()));
		assertThat(bindings.get(intParameterDescription), is(nullValue()));
		assertThat(bindings.get(stringParameterDescription), is(nullValue()));
	}

	@Test
	public void shouldNotBindJsonToExplodedParametersWhenNoPojoAvailableAndAHttpRequestIsPresent() {
		ParameterDescription requestParameterDescription = new ParameterDescription("req", HttpServletRequest.class);
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(requestParameterDescription, intParameterDescription, stringParameterDescription).to(null, null, null, null);
		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object stringValue = bindings.get(stringParameterDescription);
		Object intValue = bindings.get(intParameterDescription);
		assertThat(stringValue, is(nullValue()));
		assertThat(intValue, is(nullValue()));

		assertThat(bindings.get(requestParameterDescription), is(nullValue()));
	}

	@Test
	public void shouldBindJsonToExplodedParametersWhenNoPojoAvailableToBindTo() {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).to(null, null, null);
		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object stringValue = bindings.get(stringParameterDescription);
		assertThat(stringValue, is(notNullValue()));
		assertThat(stringValue, is((Object) "pojo name"));
		Object intValue = bindings.get(intParameterDescription);
		assertThat(intValue, is(notNullValue()));
		assertThat(intValue, is((Object) 5));
	}

	@Test
	public void shouldNotBindJsonToExplodedParametersWhenNoContentStreamAvailable() throws IOException {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).to(null, null, null);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getReader()).thenReturn(null);
		when(req.getContentType()).thenReturn(ContentType.ApplicationJson.value());

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object stringValue = bindings.get(stringParameterDescription);
		Object intValue = bindings.get(intParameterDescription);
		assertThat(stringValue, is(nullValue()));
		assertThat(intValue, is(nullValue()));
	}

	@Test
	public void shouldNotBindJsonToPojoWhenNoContentStreamAvailable() throws IOException {
		ParameterDescription parameterDescription = new ParameterDescription("pojo", TestPojo.class);
		Map<ParameterDescription, Object> bindings = map(parameterDescription, null);

		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getReader()).thenReturn(null);
		when(req.getContentType()).thenReturn(ContentType.ApplicationJson.value());

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(parameterDescription);
		assertThat(boundValue, is(nullValue()));
	}

	@Test
	public void shouldBindJsonToExplodedParametersIgnoringAnyAlreadyBoundValues() {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).<Object> to(null, "existing name");

		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object stringValue = bindings.get(stringParameterDescription);
		assertThat(stringValue, is(notNullValue()));
		assertThat(stringValue, is((Object) "existing name"));
		Object intValue = bindings.get(intParameterDescription);
		assertThat(intValue, is(notNullValue()));
		assertThat(intValue, is((Object) 5));
	}

	@Test
	public void shouldBindJsonToFirstUnboundPojoControllerParameterIgnoringOtherParameters() {
		ParameterDescription pojoParameterDescription = new ParameterDescription("pojo", TestPojo.class);
		ParameterDescription pojo2ParameterDescription = new ParameterDescription("pojo2", TestPojo.class);
		ParameterDescription requestParameterDescription = new ParameterDescription("req", HttpServletRequest.class);
		ParameterDescription intParameterDescription = new ParameterDescription("integer", int.class);
		ParameterDescription stringParameterDescription = new ParameterDescription("string", String.class);
		Map<ParameterDescription, Object> bindings = new LinkedHashMap<ParameterDescription, Object>();
		bindings.put(pojoParameterDescription, new TestPojo());
		bindings.put(pojo2ParameterDescription, null);
		bindings.put(requestParameterDescription, null);
		bindings.put(intParameterDescription, null);
		bindings.put(stringParameterDescription, null);

		req.content("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(pojo2ParameterDescription);
		assertThat(boundValue, is(notNullValue()));
		assertThat(boundValue instanceof TestPojo, is(true));
		TestPojo testPojo = (TestPojo) boundValue;
		assertThat(testPojo.name, is("pojo name"));
		assertThat(testPojo.value, is(5));

		assertThat(bindings.get(requestParameterDescription), is(nullValue()));
		assertThat(bindings.get(intParameterDescription), is(nullValue()));
		assertThat(bindings.get(stringParameterDescription), is(nullValue()));
	}

	@Test
	public void shouldThrowBindExceptionIfBindingFailsForSinglePojo() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind parameter 'pojo' as class com.threewks.thundr.action.method.bind.json.TestPojo using JSON: java.io.EOFException: End of input at");

		ParameterDescription pojoParameterDescription = new ParameterDescription("pojo", TestPojo.class);
		Map<ParameterDescription, Object> bindings = map(pojoParameterDescription, null);

		req.content("{ \"name\": \"pojo name\"");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);
	}

	@Test
	public void shouldThrowBindExceptionIfBindingFailsForMultipleValues() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind parameter 'value' as int using JSON: java.lang.NumberFormatException: For input string: \"not a number\"");

		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).to(null, null);

		req.content("{ \"name\": \"pojo name\", \"value\": \"not a number\" }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);
	}

	@Test
	public void shouldOnlyBindIfThereAreUnboundVariablesLeavingReaderAndInputStreamUnconsumed() throws IOException {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).<Object> to(15, "existing");

		req.content("{ \"name\": \"pojo name\", \"value\": 12 }");

		req = spy(req);
		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object intValue = bindings.get(intParameterDescription);
		Object stringValue = bindings.get(stringParameterDescription);
		assertThat(intValue, is((Object) 15));
		assertThat(stringValue, is((Object) "existing"));

		verify(req, never()).getInputStream();
	}
}
