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
package com.threewks.thundr.collection.factory;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.threewks.thundr.exception.BaseException;

@SuppressWarnings("rawtypes")
public class SimpleMapFactoryTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldCreateMapOfSpecifiedType() {
		SimpleMapFactory<Map> factory = new SimpleMapFactory<Map>(Map.class, HashMap.class);
		Map map = factory.create();
		assertThat(map, is(notNullValue()));
		assertThat(map instanceof HashMap, is(true));
	}

	@Test
	public void shouldHandleAllTheseTypes() {
		assertThat(new SimpleMapFactory<Map>(Map.class, HashMap.class).create() instanceof HashMap, is(true));
		assertThat(new SimpleMapFactory<HashMap>(HashMap.class, HashMap.class).create() instanceof HashMap, is(true));
		assertThat(new SimpleMapFactory<HashMap>(HashMap.class, LinkedHashMap.class).create() instanceof LinkedHashMap, is(true));
		assertThat(new SimpleMapFactory<Map>(Map.class, LinkedHashMap.class).create() instanceof LinkedHashMap, is(true));
		assertThat(new SimpleMapFactory<Map>(Map.class, TreeMap.class).create() instanceof TreeMap, is(true));
		assertThat(new SimpleMapFactory<TreeMap>(TreeMap.class, TreeMap.class).create() instanceof TreeMap, is(true));
		assertThat(new SimpleMapFactory<SortedMap>(SortedMap.class, TreeMap.class).create() instanceof TreeMap, is(true));
	}

	@Test
	public void shouldThrowBaseExceptionWhenMapCannotBeCreated() {
		thrown.expect(BaseException.class);
		thrown.expectMessage("Failed to instantiate a map of type NoDefaultCtorMap: ");
		SimpleMapFactory<Map> factory = new SimpleMapFactory<Map>(Map.class, NoDefaultCtorMap.class);
		factory.create();

	}

	@SuppressWarnings("unused")
	private static class NoDefaultCtorMap<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = 0l;

		public NoDefaultCtorMap(String requiredArg) {

		}
	}
}
