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
package com.threewks.thundr.bind.parameter;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.bind.BindException;
import com.threewks.thundr.collection.factory.MapFactory;
import com.threewks.thundr.collection.factory.SimpleMapFactory;
import com.threewks.thundr.introspection.MethodIntrospector;
import com.threewks.thundr.introspection.ParameterDescription;
import com.threewks.thundr.transformer.TransformerManager;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapParameterBinderTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	private MapFactory<Map<Object, Object>> mapFactory = new SimpleMapFactory(HashMap.class, HashMap.class);
	private MapParameterBinder<Map<Object, Object>> binder = new MapParameterBinder<>(mapFactory);
	private TransformerManager transformerManager = TransformerManager.createWithDefaults();
	private ParameterBinderRegistry binders = new ParameterBinderRegistry(transformerManager);

	@Before
	public void before() {
		ParameterBinderRegistry.addDefaultBinders(binders);
	}

	@Test
	public void shouldBindToStringStringMap() throws NoSuchMethodException, SecurityException {
		ParameterDescription parameterDescription = getParameterFor("stringToString");

		Map<String, List<String>> input = mapKeys("map[entry1]", "map[entry2]").to(values("value 1"), values("value 2"));
		RequestDataMap pathMap = new RequestDataMap(input);
		Map<Object, Object> result = binder.bind(binders, parameterDescription, pathMap, transformerManager);
		assertThat(result, hasEntry((Object) "entry1", (Object) "value 1"));
		assertThat(result, hasEntry((Object) "entry2", (Object) "value 2"));
		assertThat(result, instanceOf(HashMap.class));
	}

	@Test
	public void shouldBindToStringStringArrayMap() {
		ParameterDescription parameterDescription = getParameterFor("stringToStringArray");

		Map<String, List<String>> input = mapKeys("map[entry1]", "map[entry2]").to(values("value 1", "value 2"), values("value 2"));
		RequestDataMap pathMap = new RequestDataMap(input);
		Map<Object, Object> result = binder.bind(binders, parameterDescription, pathMap, transformerManager);
		assertThat(result, hasEntry((Object) "entry1", (Object) Arrays.asList("value 1", "value 2" )));
		assertThat(result, hasEntry((Object) "entry2", (Object) Arrays.asList("value 2" )));
		assertThat(result, instanceOf(HashMap.class));
	}

	@Test
	public void shouldBindToLinkedHashMap() {
		MapFactory<Map<Object, Object>> mapFactory = new SimpleMapFactory(Map.class, LinkedHashMap.class);
		MapParameterBinder<Map<Object, Object>> binder = new MapParameterBinder<>(mapFactory);

		ParameterDescription parameterDescription = getParameterFor("stringToStringArray");

		Map<String, List<String>> input = mapKeys("map[entry1]", "map[entry2]").to(values("value 1", "value 2"), values("value 2"));
		RequestDataMap pathMap = new RequestDataMap(input);
		Map<Object, Object> result = binder.bind(binders, parameterDescription, pathMap, transformerManager);
		assertThat(result, hasEntry((Object) "entry1", (Object) Arrays.asList( "value 1", "value 2" )));
		assertThat(result, hasEntry((Object) "entry2", (Object) Arrays.asList( "value 2" )));
		assertThat(result, instanceOf(LinkedHashMap.class));
	}

	@Test
	public void shouldBindToTreeMap() {
		MapFactory<Map<Object, Object>> mapFactory = new SimpleMapFactory(Map.class, TreeMap.class);
		MapParameterBinder<Map<Object, Object>> binder = new MapParameterBinder<>(mapFactory);

		ParameterDescription parameterDescription = getParameterFor("stringToStringArray");

		Map<String, List<String>> input = mapKeys("map[entry1]", "map[entry2]").to(values("value 1", "value 2"), values("value 2"));
		RequestDataMap pathMap = new RequestDataMap(input);
		Map<Object, Object> result = binder.bind(binders, parameterDescription, pathMap, transformerManager);
		assertThat(result, hasEntry((Object) "entry1", (Object) Arrays.asList( "value 1", "value 2" )));
		assertThat(result, hasEntry((Object) "entry2", (Object) Arrays.asList( "value 2" )));
		assertThat(result, instanceOf(TreeMap.class));
	}

	@Test
	public void shouldThrowExceptionWhenUnableToInterpretMapKey() {
		thrown.expect(BindException.class);
		thrown.expectMessage("Cannot bind map[key - not a valid map key");

		ParameterDescription parameterDescription = getParameterFor("stringToString");

		Map<String, List<String>> input = map("map[key", values("value 1"));
		RequestDataMap pathMap = new RequestDataMap(input);
		binder.bind(binders, parameterDescription, pathMap, transformerManager);
	}

	@Test
	public void shouldReturnNullOnEmptyInput() {
		ParameterDescription parameterDescription = getParameterFor("stringToString");

		Map<String, List<String>> input = map("map", values());
		assertThat(binder.bind(binders, parameterDescription, new RequestDataMap(input), transformerManager), is(nullValue()));
		assertThat(binder.bind(binders, parameterDescription, new RequestDataMap(emptyMap()), transformerManager), is(nullValue()));
	}

	@Test
	public void shouldReturnTrueForWillBindWhenFactoryTypeMatches() {
		assertThat(binder.willBind(getParameterFor("stringToString"), transformerManager), is(true));
		assertThat(binder.willBind(getParameterFor("linkedHashMap"), transformerManager), is(false));
	}

	private Map<String, List<String>> emptyMap() {
		return Collections.emptyMap();
	}

	private List<String> values(String... values) {
		return Arrays.asList(values);
	}

	private ParameterDescription getParameterFor(String methodName) {
		try {
			Method[] declaredMethods = this.getClass().getDeclaredMethods();
			for (Method method : declaredMethods) {
				if (method.getName() == methodName) {
					List<ParameterDescription> parameterDescriptions = new MethodIntrospector(method).getParameterDescriptions();
					ParameterDescription parameterDescription = parameterDescriptions.get(0);
					return parameterDescription;
				}
			}
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public void stringToString(Map<String, String> map) {

	}

	public void stringToStringArray(Map<String, List<String>> map) {

	}

	public void linkedHashMap(LinkedHashMap<String, List<String>> map) {

	}
}
