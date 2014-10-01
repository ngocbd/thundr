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
package com.threewks.thundr.transformer.discrete;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.threewks.thundr.route.HttpMethod;

public class ObjectToEnumTest {

	@Test
	public void shouldConvertObjectToEnum() {
		ObjectToEnum<Object, HttpMethod> transformer = new ObjectToEnum<Object, HttpMethod>(HttpMethod.class);

		assertThat(transformer.from(null), is(nullValue()));
		assertThat(transformer.from(""), is(nullValue()));
		assertThat(transformer.from("junk"), is(nullValue()));
		assertThat(transformer.from(1), is(nullValue()));

		assertThat(transformer.from("get"), is(HttpMethod.GET));
		assertThat(transformer.from("GET"), is(HttpMethod.GET));
		assertThat(transformer.from("gEt"), is(HttpMethod.GET));
		assertThat(transformer.from(" get "), is(HttpMethod.GET));

		assertThat(transformer.from("post"), is(HttpMethod.POST));
		assertThat(transformer.from("POST"), is(HttpMethod.POST));
		assertThat(transformer.from("pOsT"), is(HttpMethod.POST));
		assertThat(transformer.from(" post "), is(HttpMethod.POST));

		assertThat(transformer.from(HttpMethod.POST), is(HttpMethod.POST));
	}

}
