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
package com.threewks.thundr.bind;

import static com.atomicleopard.expressive.Expressive.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.threewks.thundr.bind.parameter.RequestDataMap;

public class HttpPostDataMapTest {
	@Test
	public void shouldNotSplitWhenSimple() {
		Map<String, List<String>> map = map("key", Arrays.asList( "value" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		assertThat(pathMap.get(list("key")), is(list("value")));
	}

	@Test
	public void shouldSplitForNestedPath() {
		Map<String, List<String>> map = map("one.two.three", Arrays.asList( "value" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		assertThat(pathMap.get(list("one", "two", "three")), is(list("value")));
	}

	@Test
	public void shouldSplitForNestedListPath() {
		Map<String, List<String>> map = map("one[two].three", Arrays.asList( "value" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		assertThat(pathMap.get(list("one", "[two]", "three")), is(list("value")));
	}

	@Test
	public void shouldCreateANewPathMapForNestedPath() {
		Map<String, List<String>> map = map("one[two].three", Arrays.asList( "value" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		RequestDataMap newPathMap = pathMap.pathMapFor("one");
		assertThat(newPathMap.get(list("[two]", "three")), is(list("value")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldCreateANewPathMapForNestedPathRemovingUnrelatedPaths() {
		Map<String, List<String>> map = mapKeys("one[two].three", "one[one].two", "other.thing").to(Arrays.asList( "value" ), Arrays.asList( "value2" ), Arrays.asList( "value3" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		RequestDataMap newPathMap = pathMap.pathMapFor("one");
		assertThat(newPathMap.size(), is(2));
		assertThat(newPathMap.get(list("[two]", "three")), is(list("value")));
		assertThat(newPathMap.get(list("[one]", "two")), is(list("value2")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldRemoveAllDashesFromPathElementsToEnableBetterBindingBetweenParametersAndJavaVariableNames() {
		Map<String, List<String>> map = mapKeys("one-One[two-Two-].-three-Three", "one-One[one].two").to(Arrays.asList( "value-value" ), Arrays.asList( "value2" ));
		RequestDataMap pathMap = new RequestDataMap(map);
		RequestDataMap newPathMap = pathMap.pathMapFor("oneOne");
		assertThat(newPathMap.size(), is(2));
		assertThat(newPathMap.get(list("[twoTwo]", "threeThree")), is(list("value-value")));
		assertThat(newPathMap.get(list("[one]", "two")), is(list("value2")));
	}
}
