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
package com.threewks.thundr.route.controller;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.threewks.thundr.bind.BinderRegistry;
import com.threewks.thundr.bind.DeepJavaBean;
import com.threewks.thundr.bind.JavaBean;
import com.threewks.thundr.bind.TestBindTo;
import com.threewks.thundr.bind.parameter.ParameterBinderRegistry;
import com.threewks.thundr.request.Request;
import com.threewks.thundr.transformer.TransformerManager;

public class ControllerResolverBindingTest {

	private static Object nullObject = null;
	private Map<String, String> emptyMap = Collections.emptyMap();

	private Request request;
	private BinderRegistry binderRegistry;
	private ControllerRouteResolver resolver;

	@Before
	public void before() {
		TransformerManager transformerManager = TransformerManager.createWithDefaults();
		ParameterBinderRegistry parameterBinderRegistry = new ParameterBinderRegistry(transformerManager);
		ParameterBinderRegistry.addDefaultBinders(parameterBinderRegistry);

		binderRegistry = new BinderRegistry();
		BinderRegistry.registerDefaultBinders(binderRegistry, parameterBinderRegistry, transformerManager);

		resolver = new ControllerRouteResolver(null, null, binderRegistry);
		request = mock(Request.class);
		when(request.getAllParameters()).thenReturn(Collections.<String, List<String>> emptyMap());
	}

	@Test
	public void shouldInvokeNone() throws Exception {
		Controller method = controller("methodNone");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(0));
		assertThat(resolver.bindArguments(method, request("argument1", "value1"), null, null).size(), is(0));
	}

	private Controller controller(String method) throws ClassNotFoundException {
		return new Controller(TestBindTo.class, method);
	}

	@Test
	public void shouldInvokeSingleString() throws Exception {
		Controller method = controller("methodSingleString");
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1", "value1"), null, emptyMap), Matchers.<Object> contains("value1"));
	}

	@Test
	public void shouldtestname() {

	}

	@Test
	public void shouldInvokeDoubleString() throws Exception {

		Controller method = controller("methodDoubleString");
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject, nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1", "value1", "argument2", "value2"), null, emptyMap), Matchers.<Object> contains("value1", "value2"));
		assertThat(resolver.bindArguments(method, request("argument1", "value1"), null, emptyMap), Matchers.<Object> contains("value1", null));
		assertThat(resolver.bindArguments(method, request("argument2", "value2"), null, emptyMap), Matchers.<Object> contains(null, "value2"));
	}

	@Test
	public void shouldInvokeStringList() throws Exception {
		Controller method = controller("methodStringList");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1[0]", "value1"), null, emptyMap), Matchers.<Object> contains(list("value1")));
		assertThat(resolver.bindArguments(method, request("argument1[0]", "value1", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap),
				Matchers.<Object> contains(list("value1", "value2", null, "value3")));
		assertThat(resolver.bindArguments(method, request("argument1[0]", "", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap),
				Matchers.<Object> contains(list("", "value2", null, "value3")));
		assertThat(resolver.bindArguments(method, request("argument1", new String[] { "value1", "value2" }), null, emptyMap), Matchers.<Object> contains(list("value1", "value2")));
		assertThat(resolver.bindArguments(method, request("argument1", null), null, emptyMap), Matchers.<Object> contains(nullObject));
		assertThat(resolver.bindArguments(method, request("argument1", ""), null, emptyMap), Matchers.<Object> contains(nullObject));
	}

	@Test
	public void shouldInvokeStringMap() throws Exception {
		Controller method = controller("methodMap");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1[0]", "value1"), null, emptyMap), Matchers.<Object> contains(map("0", list("value1"))));
		assertThat(resolver.bindArguments(method, request("argument1[first]", "value1", "argument1[second]", "value2", "argument1[third]", "value3"), null, emptyMap),
				Matchers.<Object> contains(map("first", list("value1"), "second", list("value2"), "third", list("value3"))));
		assertThat(resolver.bindArguments(method, request("argument1[first]", "", "argument1[second_second]", "value2", "argument1[THIRD]", "value3", "argument1[fourth]", null), null, emptyMap),
				Matchers.<Object> contains(map("first", null, "second_second", list("value2"), "THIRD", list("value3"), "fourth", null)));
		// TODO - Implicit map - what would an unindexed map posted look like?
		// assertThat(resolver.bindArguments(method("methodMap"), request("argument1", new String[] { "value1", "value2" }), null, emptyMap), is(list(map("value1", "value2"))));
	}

	@Test
	public void shouldInvokeArray() throws ClassNotFoundException {
		Controller method = controller("methodStringArray");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", ""), null, emptyMap).get(0), is(array("")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "value1"), null, emptyMap).get(0), is(array("value1")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "value1", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap).get(0),
				is(array("value1", "value2", null, "value3")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap).get(0),
				is(array("", "value2", null, "value3")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1", new String[] { "value1", "value2" }), null, emptyMap).get(0), is(array("value1", "value2")));

		assertThat(resolver.bindArguments(method, request(), null, emptyMap).get(0), is(nullValue()));
		assertThat(resolver.bindArguments(method, request("argument1", null), null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1", ""), null, emptyMap), is(list(nullObject)));

	}

	@Test
	public void shouldInvokeGenericArray() throws ClassNotFoundException {
		Controller method = controller("methodGenericArray");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "value1"), null, emptyMap).get(0), is(array("value1")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "value1", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap).get(0),
				is(array("value1", "value2", null, "value3")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1[0]", "", "argument1[1]", "value2", "argument1[3]", "value3"), null, emptyMap).get(0),
				is(array("", "value2", null, "value3")));
		assertThat((String[]) resolver.bindArguments(method, request("argument1", new String[] { "value1", "value2" }), null, emptyMap).get(0), is(array("value1", "value2")));

		assertThat(resolver.bindArguments(method, request(), null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1", null), null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1", ""), null, emptyMap), is(list(nullObject)));
	}

	@Test
	public void shouldInvokeJavaBean() throws ClassNotFoundException {
		Controller method = controller("methodJavaBean");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname"), null, emptyMap), Matchers.<Object> contains(new JavaBean("myname", null)));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname", "argument1.value", "my value"), null, emptyMap), Matchers.<Object> contains(new JavaBean("myname", "my value")));
		assertThat(resolver.bindArguments(method, request("argument1.value", "my value"), null, emptyMap), Matchers.<Object> contains(new JavaBean(null, "my value")));
		assertThat(resolver.bindArguments(method, request("argument1.name", null), null, emptyMap), Matchers.<Object> contains(new JavaBean(null, null)));
		assertThat(resolver.bindArguments(method, request("argument1.name", ""), null, emptyMap), Matchers.<Object> contains(new JavaBean("", null)));
		assertThat(resolver.bindArguments(method, request("argument1.name", "multiline\ncontent"), null, emptyMap), Matchers.<Object> contains(new JavaBean("multiline\ncontent", null)));
		assertThat(resolver.bindArguments(method, request("argument1", null), null, emptyMap), Matchers.<Object> contains(nullObject));
		assertThat(resolver.bindArguments(method, request("argument1", ""), null, emptyMap), Matchers.<Object> contains(nullObject));
		assertThat(resolver.bindArguments(method, request("argument1.name", new String[] { "value1", "value2" }), null, emptyMap), Matchers.<Object> contains(new JavaBean(null, null)));
	}

	@Test
	public void shouldInvokeDeepJavaBean() throws ClassNotFoundException {
		Controller method = controller("methodDeepJavaBean");
		assertThat(resolver.bindArguments(method, request, null, emptyMap).size(), is(1));
		assertThat(resolver.bindArguments(method, request, null, emptyMap), is(list(nullObject)));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname"), null, emptyMap), Matchers.<Object> contains(new DeepJavaBean("myname", null)));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname", "argument1.beans[0].name", "some name"), null, emptyMap),
				Matchers.<Object> contains(new DeepJavaBean("myname", list(new JavaBean("some name", null)))));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname", "argument1.beans[0].name", "some name", "argument1.beans[1].name", "some other"), null, emptyMap),
				Matchers.<Object> contains(new DeepJavaBean("myname", list(new JavaBean("some name", null), new JavaBean("some other", null)))));
		assertThat(resolver.bindArguments(method, request("argument1.name", "myname", "argument1.beans[1].name", "some name"), null, emptyMap),
				Matchers.<Object> contains(new DeepJavaBean("myname", list(null, new JavaBean("some name", null)))));
		assertThat(resolver.bindArguments(method, request("argument1", null), null, emptyMap), Matchers.<Object> contains(nullObject));
		assertThat(resolver.bindArguments(method, request("argument1", ""), null, emptyMap), Matchers.<Object> contains(nullObject));
	}

	private Request request(String... args) {
		Map<String, List<String>> map = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			map.put(args[i], list(args[i + 1]));
		}
		when(request.getAllParameters()).thenReturn(map);
		return request;
	}

	private Request request(String name, String[] values) {
		Map<String, List<String>> map = new HashMap<>();
		map.put(name, values == null ? null : list(values));
		when(request.getAllParameters()).thenReturn(map);
		return request;
	}

	@SafeVarargs
	private final <T> List<T> list(T... content) {
		List<T> result = new ArrayList<>();
		for (T t : content) {
			result.add(t);
		}
		return result;
	}
}
