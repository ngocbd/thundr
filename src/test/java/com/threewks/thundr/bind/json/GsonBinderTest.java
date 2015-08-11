/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://3wks.github.io/thundr/
 * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
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
package com.threewks.thundr.bind.json;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.http.ContentType;
import com.threewks.thundr.introspection.MethodIntrospector;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.request.mock.MockRequest;
import com.threewks.thundr.request.mock.MockResponse;
import com.threewks.thundr.test.TestSupport;

public class GsonBinderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private GsonBinder gsonBinder = new GsonBinder();
	private MockRequest req = new MockRequest().withContentType(ContentType.ApplicationJson);
	private MockResponse resp = new MockResponse();
	private Map<String, String> pathVariables = Collections.emptyMap();

	@Test
	public void shouldMakeGsonBuilderAvailable() {
		GsonBuilder gsonBuilder = gsonBinder.getGsonBuilder();
		GsonBuilder internal = TestSupport.getField(gsonBinder, "gsonBuilder");
		assertThat(gsonBuilder, is(sameInstance(internal)));
	}

	@Test
	public void shouldReturnTrueForCanBindToJsonContentType() {
		assertThat(gsonBinder.canBind(ContentType.ApplicationJson), is(true));
		assertThat(gsonBinder.canBind(ContentType.TextPlain), is(false));
		assertThat(gsonBinder.canBind(null), is(false));
	}

	@Test
	public void shouldBindJsonToPojoControllerParameter() {
		ParameterDescription parameterDescription = new ParameterDescription("pojo", TestPojo.class);
		Map<ParameterDescription, Object> bindings = map(parameterDescription, null);
		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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

		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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
		ParameterDescription requestParameterDescription = new ParameterDescription("req", Request.class);
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(requestParameterDescription, intParameterDescription, stringParameterDescription).to(null, null, null, null);
		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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
		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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
		req.withContentType(ContentType.ApplicationJson);

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

		// @formatter:off
		MockRequest req = new MockRequest()
			.withContentType(ContentType.ApplicationJson);
		// @formatter:on

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(parameterDescription);
		assertThat(boundValue, is(nullValue()));
	}

	@Test
	public void shouldBindJsonToExplodedParametersIgnoringAnyAlreadyBoundValues() {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).<Object> to(null, "existing name");

		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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

		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

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
	public void shouldBindJsonToCollectionParameter() throws NoSuchMethodException, SecurityException {
		assertBindToCollection("collectionMethod", Collection.class);
		assertBindToCollection("listMethod", List.class);
		assertBindToCollection("linkedListMethod", LinkedList.class);
		assertBindToCollection("arrayListMethod", ArrayList.class);
	}

	@SuppressWarnings("unchecked")
	private <T extends Collection<?>> void assertBindToCollection(String methodName, Class<T> parameterType) throws NoSuchMethodException, SecurityException {
		Method method = this.getClass().getDeclaredMethod(methodName, parameterType);
		MethodIntrospector methodIntrospector = new MethodIntrospector(method);
		List<ParameterDescription> parameterDescriptions = methodIntrospector.getParameterDescriptions();

		Map<ParameterDescription, Object> bindings = new LinkedHashMap<ParameterDescription, Object>();
		ParameterDescription parameterDescription = parameterDescriptions.get(0);
		bindings.put(parameterDescription, null);

		req.withBody("[{\"name\": \"pojo name\", \"value\": 5 }]");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object value = bindings.get(parameterDescription);
		assertThat(value, is(notNullValue()));
		assertThat(value, instanceOf(parameterType));
		T list = (T) value;
		assertThat(list.size(), is(1));
		TestPojo testPojo = (TestPojo) list.iterator().next();
		assertThat(testPojo.name, is("pojo name"));
		assertThat(testPojo.value, is(5));
	}

	@Test
	public void shouldThrowBindExceptionIfBindingFailsForSinglePojo() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind parameter 'pojo' as class com.threewks.thundr.bind.json.TestPojo using JSON: java.io.EOFException: End of input at");

		ParameterDescription pojoParameterDescription = new ParameterDescription("pojo", TestPojo.class);
		Map<ParameterDescription, Object> bindings = map(pojoParameterDescription, null);

		req.withBody("{ \"name\": \"pojo name\"");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);
	}

	@Test
	public void shouldThrowBindExceptionIfBindingFailsForAValueWhenBindingMultipleValues() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind parameter 'value' as int using JSON: java.lang.NumberFormatException: For input string: \"not a number\"");

		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).to(null, null);

		req.withBody("{ \"name\": \"pojo name\", \"value\": \"not a number\" }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);
	}

	@Test
	public void shouldThrowBindExceptionIfBindingFailsForMultipleValues() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Failed to bind JSON: java.io.EOFException: End of input at line 1 column 38");

		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).to(null, null);

		req.withBody("{ \"name\": \"pojo name\", \"value\": \"5.0\"");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);
	}

	@Test
	public void shouldOnlyBindIfThereAreUnboundVariablesLeavingReaderAndInputStreamUnconsumed() throws IOException {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = mapKeys(intParameterDescription, stringParameterDescription).<Object> to(15, "existing");

		req.withBody("{ \"name\": \"pojo name\", \"value\": 12 }");

		req = spy(req);
		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object intValue = bindings.get(intParameterDescription);
		Object stringValue = bindings.get(stringParameterDescription);
		assertThat(intValue, is((Object) 15));
		assertThat(stringValue, is((Object) "existing"));

		verify(req, never()).getReader();
	}

	@Test
	public void shouldNotBindAndLeaveReaderAndInputStreamUnconsumedWhenNoVariablesPresent() throws IOException {
		Map<ParameterDescription, Object> bindings = Collections.emptyMap();

		req.withBody("{ \"name\": \"pojo name\", \"value\": 12 }");

		req = spy(req);
		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		verify(req, never()).getReader();
	}

	@Test
	public void shouldNotBindAndLeaveReaderAndInputStreamUnconsumedWhenNotABindableContentType() throws IOException {
		ParameterDescription stringParameterDescription = new ParameterDescription("name", String.class);
		ParameterDescription intParameterDescription = new ParameterDescription("value", int.class);
		Map<ParameterDescription, Object> bindings = map(stringParameterDescription, null, intParameterDescription, null);

		req.withBody("{ \"name\": \"pojo name\", \"value\": 12 }");
		req.withContentType(ContentType.ApplicationFormUrlEncoded);

		req = spy(req);
		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		verify(req, never()).getReader();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindToMap() {
		assertCanBindToMap(Map.class, LinkedTreeMap.class);
		assertCanBindToMap(LinkedHashMap.class, LinkedHashMap.class);
		assertCanBindToMap(TreeMap.class, TreeMap.class);
		assertCanBindToMap(HashMap.class, HashMap.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldBindToCollectionImplementations() {
		assertCanBindToCollection(Set.class, LinkedHashSet.class);
		assertCanBindToCollection(LinkedHashSet.class, LinkedHashSet.class);
		assertCanBindToCollection(SortedSet.class, TreeSet.class);

		assertCanBindToCollection(List.class, ArrayList.class);
		assertCanBindToCollection(LinkedList.class, LinkedList.class);
		assertCanBindToCollection(ArrayList.class, ArrayList.class);

		assertCanBindToCollection(Collection.class, ArrayList.class);
	}

	@SuppressWarnings("unchecked")
	private <I extends Collection<String>, T extends I> void assertCanBindToCollection(Class<I> parameterType, Class<T> concreteType) {
		ParameterDescription parameterDescription = new ParameterDescription("bind", parameterType);
		Map<ParameterDescription, Object> bindings = map(parameterDescription, null);
		req.withBody("[\"first\", \"second\"]");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(parameterDescription);
		assertThat(boundValue, is(notNullValue()));
		assertThat(boundValue.getClass() == concreteType, is(true));
		I collection = (I) boundValue;
		Iterator<String> iterator = collection.iterator();
		assertThat(iterator.next(), is("first"));
		assertThat(iterator.next(), is("second"));
		assertThat(iterator.hasNext(), is(false));
	}

	@SuppressWarnings("unchecked")
	private <I extends Map<String, Object>, T extends I> void assertCanBindToMap(Class<I> parameterType, Class<T> concreteType) {
		ParameterDescription parameterDescription = new ParameterDescription("bind", parameterType);
		Map<ParameterDescription, Object> bindings = map(parameterDescription, null);
		req.withBody("{ \"name\": \"pojo name\", \"value\": 5 }");

		gsonBinder.bindAll(bindings, req, resp, pathVariables);

		Object boundValue = bindings.get(parameterDescription);
		assertThat(boundValue, is(notNullValue()));
		assertThat(boundValue, instanceOf(concreteType));
		I map = (I) boundValue;
		assertThat(map.get("name"), is((Object) "pojo name"));
		assertThat(map.get("value"), is((Object) 5d));
	}

	@SuppressWarnings("unused")
	private void listMethod(List<TestPojo> pojos) {
	}

	@SuppressWarnings("unused")
	private void arrayListMethod(ArrayList<TestPojo> pojos) {
	}

	@SuppressWarnings("unused")
	private void linkedListMethod(LinkedList<TestPojo> pojos) {
	}

	@SuppressWarnings("unused")
	private void collectionMethod(Collection<TestPojo> pojos) {
	}

}
